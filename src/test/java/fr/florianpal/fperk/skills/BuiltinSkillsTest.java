package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.HandlerBinding;
import fr.florianpal.fperk.api.SkillContext;
import fr.florianpal.fperk.api.SkillHandler;
import fr.florianpal.fperk.api.SkillRegistry;
import fr.florianpal.fperk.objects.PlayerPerk;
import fr.florianpal.fperk.objects.Skill;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What each builtin skill actually does to a player, and the guard that keeps it from applying to
 * players who do not have it running.
 *
 * <p>Events are stubbed rather than built : their constructors change between Minecraft versions,
 * while the behaviour under test does not.</p>
 */
class BuiltinSkillsTest {

    private ServerMock server;

    private FPerk plugin;

    private SkillRegistry registry;

    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();

        plugin = mock(FPerk.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("FPerkTest"));
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getName()).thenReturn("FPerk");
        // The registry hands Listener handlers to Bukkit, which asks the plugin for its loader and
        // its server to build the listener list.
        when(plugin.getServer()).thenReturn(server);
        when(plugin.getPluginLoader()).thenReturn(MockBukkit.createMockPlugin("Host").getPluginLoader());

        registry = new SkillRegistry(plugin);
        when(plugin.getSkillRegistry()).thenReturn(registry);

        player = server.addPlayer("Tester");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Binds the handler and reports whether the skill is running for the player.
     */
    private <T extends SkillHandler> T prepare(T handler, boolean active) {
        HandlerBinding.bind(handler, plugin, plugin);
        if (active) {
            registry.markActive(player.getUniqueId(), handler.getId());
        }
        return handler;
    }

    private SkillContext context(String effect, float level) {
        Skill skill = new Skill("s", List.of("n"), "TYPE", effect, level);
        PlayerPerk playerPerk = new PlayerPerk(1, player.getUniqueId(), "p", new Date().getTime(), true);
        return new SkillContext(player, null, skill, playerPerk);
    }

    // --- skills driven by the perk being toggled ----------------------------------------------

    @Nested
    class Fly {

        @Test
        void enablingLetsThePlayerFly() {
            FlySkill skill = prepare(new FlySkill(), true);

            skill.onEnable(context("", 1));

            assertTrue(player.getAllowFlight());
            assertTrue(player.isFlying());
        }

        @Test
        void disablingGroundsThePlayer() {
            FlySkill skill = prepare(new FlySkill(), true);
            skill.onEnable(context("", 1));

            skill.onDisable(context("", 1));

            assertFalse(player.getAllowFlight());
            assertFalse(player.isFlying());
        }

        @Test
        @DisplayName("changing world re-applies flight, which the world change resets")
        void reappliesOnWorldChange() {
            FlySkill skill = prepare(new FlySkill(), true);
            player.setAllowFlight(false);

            skill.onChangedWorld(new org.bukkit.event.player.PlayerChangedWorldEvent(player, player.getWorld()));

            assertTrue(player.getAllowFlight());
        }

        @Test
        @DisplayName("a player without the skill is grounded on world change, not granted flight")
        void doesNotGrantFlightOnWorldChange() {
            FlySkill skill = prepare(new FlySkill(), false);
            player.setAllowFlight(true);

            skill.onChangedWorld(new org.bukkit.event.player.PlayerChangedWorldEvent(player, player.getWorld()));

            assertFalse(player.getAllowFlight());
        }
    }

    @Nested
    class FlySpeed {

        @Test
        void enablingSetsTheConfiguredSpeed() {
            FlySpeedSkill skill = prepare(new FlySpeedSkill(), true);

            skill.onEnable(context("", 0.5F));

            assertEquals(0.5F, player.getFlySpeed(), 0.0001F);
        }

        @Test
        @DisplayName("disabling restores the vanilla speed rather than zero")
        void disablingRestoresTheDefault() {
            FlySpeedSkill skill = prepare(new FlySpeedSkill(), true);
            skill.onEnable(context("", 0.8F));

            skill.onDisable(context("", 0.8F));

            assertEquals(0.1F, player.getFlySpeed(), 0.0001F);
        }
    }

    @Nested
    class Effect {

        @Test
        void enablingAppliesThePotionEffect() {
            EffectSkill skill = prepare(new EffectSkill(), true);

            skill.onEnable(context("night_vision", 1));

            PotionEffect applied = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
            assertNotNull(applied, "the effect named in skill.yml is applied");
            assertEquals(1, applied.getAmplifier());
        }

        @Test
        @DisplayName("level maps to the potion amplifier")
        void levelDrivesTheAmplifier() {
            EffectSkill skill = prepare(new EffectSkill(), true);

            skill.onEnable(context("regeneration", 2));

            assertEquals(2, player.getPotionEffect(PotionEffectType.REGENERATION).getAmplifier());
        }

        @Test
        void disablingRemovesIt() {
            EffectSkill skill = prepare(new EffectSkill(), true);
            skill.onEnable(context("night_vision", 1));

            skill.onDisable(context("night_vision", 1));

            assertFalse(player.hasPotionEffect(PotionEffectType.NIGHT_VISION));
        }

        @Test
        @DisplayName("an unknown or blank effect is ignored instead of throwing")
        void toleratesAnUnusableEffectName() {
            EffectSkill skill = prepare(new EffectSkill(), true);

            skill.onEnable(context("not_a_potion", 1));
            skill.onEnable(context("", 1));
            skill.onEnable(context(null, 1));
            skill.onDisable(context("not_a_potion", 1));

            assertTrue(player.getActivePotionEffects().isEmpty());
        }
    }

    @Nested
    class CureEffect {

        @Test
        void enablingClearsTheNegativeEffects() {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0));

            prepare(new CureEffectSkill(), true).onEnable(context("", 1));

            assertFalse(player.hasPotionEffect(PotionEffectType.POISON));
            assertFalse(player.hasPotionEffect(PotionEffectType.BLINDNESS));
            assertTrue(player.hasPotionEffect(PotionEffectType.NIGHT_VISION),
                    "a beneficial effect is left in place");
        }

        @Test
        @DisplayName("moving keeps clearing them, so a new debuff does not stick")
        void clearsAgainOnMove() {
            CureEffectSkill skill = prepare(new CureEffectSkill(), true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));

            skill.onMove(new PlayerMoveEvent(player, player.getLocation(), player.getLocation()));

            assertFalse(player.hasPotionEffect(PotionEffectType.POISON));
        }

        @Test
        void leavesOtherPlayersAlone() {
            CureEffectSkill skill = prepare(new CureEffectSkill(), false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));

            skill.onMove(new PlayerMoveEvent(player, player.getLocation(), player.getLocation()));

            assertTrue(player.hasPotionEffect(PotionEffectType.POISON));
        }
    }

    // --- skills driven by events ---------------------------------------------------------------

    private PlayerDeathEvent deathEvent(boolean cancelled) {
        PlayerDeathEvent event = mock(PlayerDeathEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getEntity()).thenReturn(player);
        when(event.isCancelled()).thenReturn(cancelled);
        when(event.getDrops()).thenReturn(new ArrayList<ItemStack>());
        return event;
    }

    @Nested
    class KeepInventory {

        @Test
        void keepsTheInventoryOnDeath() {
            PlayerDeathEvent event = deathEvent(false);

            prepare(new KeepInventorySkill(), true).onDeath(event);

            verify(event).setKeepInventory(true);
        }

        @Test
        void leavesPlayersWithoutTheSkillAlone() {
            PlayerDeathEvent event = deathEvent(false);

            prepare(new KeepInventorySkill(), false).onDeath(event);

            verify(event, never()).setKeepInventory(true);
        }

        @Test
        @DisplayName("a death cancelled by the second chance is not handled")
        void skipsACancelledDeath() {
            PlayerDeathEvent event = deathEvent(true);

            prepare(new KeepInventorySkill(), true).onDeath(event);

            verify(event, never()).setKeepInventory(true);
        }
    }

    @Nested
    class KeepExperience {

        @Test
        void keepsTheLevelsOnDeath() {
            PlayerDeathEvent event = deathEvent(false);

            prepare(new KeepExperienceSkill(), true).onDeath(event);

            verify(event).setKeepLevel(true);
            verify(event).setShouldDropExperience(false);
        }

        @Test
        void leavesPlayersWithoutTheSkillAlone() {
            PlayerDeathEvent event = deathEvent(false);

            prepare(new KeepExperienceSkill(), false).onDeath(event);

            verify(event, never()).setKeepLevel(true);
        }

        @Test
        void skipsACancelledDeath() {
            PlayerDeathEvent event = deathEvent(true);

            prepare(new KeepExperienceSkill(), true).onDeath(event);

            verify(event, never()).setKeepLevel(true);
        }
    }

    @Nested
    class SecondChance {

        @Test
        @DisplayName("death is cancelled, the player is healed, and the perk is consumed")
        void cancelsDeathAndConsumesItself() {
            fr.florianpal.fperk.managers.SkillService service =
                    mock(fr.florianpal.fperk.managers.SkillService.class);
            when(plugin.getSkillService()).thenReturn(service);

            SecondChanceSkill skill = prepare(new SecondChanceSkill(), true);
            PlayerDeathEvent event = deathEvent(false);

            skill.onDeath(event);

            verify(event).setCancelled(true);
            assertEquals(10, player.getHealth(), 0.001);
            assertTrue(player.hasPotionEffect(PotionEffectType.REGENERATION));
            assertFalse(registry.isActive(player.getUniqueId(), BuiltinSkills.SECOND_CHANCE),
                    "the skill marks itself inactive right away");
            verify(service).consumeSkill(player, BuiltinSkills.SECOND_CHANCE);
        }

        @Test
        void leavesPlayersWithoutTheSkillAlone() {
            SecondChanceSkill skill = prepare(new SecondChanceSkill(), false);
            PlayerDeathEvent event = deathEvent(false);

            skill.onDeath(event);

            verify(event, never()).setCancelled(true);
        }
    }

    private EntityDamageEvent damageEvent(Entity entity, EntityDamageEvent.DamageCause cause) {
        EntityDamageEvent event = mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(entity);
        when(event.getCause()).thenReturn(cause);
        return event;
    }

    @Nested
    class BrokenFall {

        @Test
        void cancelsFallDamage() {
            EntityDamageEvent event = damageEvent(player, EntityDamageEvent.DamageCause.FALL);

            prepare(new BrokenFallSkill(), true).onDamage(event);

            verify(event).setDamage(0);
        }

        @Test
        @DisplayName("only fall damage is removed, the rest still hurts")
        void leavesOtherDamageAlone() {
            EntityDamageEvent event = damageEvent(player, EntityDamageEvent.DamageCause.FIRE);

            prepare(new BrokenFallSkill(), true).onDamage(event);

            verify(event, never()).setDamage(0);
        }

        @Test
        void leavesPlayersWithoutTheSkillAlone() {
            EntityDamageEvent event = damageEvent(player, EntityDamageEvent.DamageCause.FALL);

            prepare(new BrokenFallSkill(), false).onDamage(event);

            verify(event, never()).setDamage(0);
        }

        @Test
        void ignoresNonPlayers() {
            Entity zombie = mock(Entity.class);
            when(zombie.getType()).thenReturn(EntityType.ZOMBIE);
            EntityDamageEvent event = damageEvent(zombie, EntityDamageEvent.DamageCause.FALL);

            prepare(new BrokenFallSkill(), true).onDamage(event);

            verify(event, never()).setDamage(0);
        }
    }

    @Nested
    class Pacification {

        private EntityTargetEvent targetEvent(Entity target, EntityTargetEvent.TargetReason reason) {
            EntityTargetEvent event = mock(EntityTargetEvent.class);
            when(event.getTarget()).thenReturn(target);
            when(event.getReason()).thenReturn(reason);
            return event;
        }

        @Test
        void stopsMobsFromTargetingThePlayer() {
            EntityTargetEvent event = targetEvent(player, EntityTargetEvent.TargetReason.CLOSEST_PLAYER);

            prepare(new PacificationSkill(), true).onEntityTarget(event);

            verify(event).setCancelled(true);
        }

        @Test
        @DisplayName("a mob the player attacked still fights back")
        void doesNotCancelRetaliation() {
            EntityTargetEvent event = targetEvent(player, EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY);

            prepare(new PacificationSkill(), true).onEntityTarget(event);

            verify(event, never()).setCancelled(true);
        }

        @Test
        void leavesPlayersWithoutTheSkillAlone() {
            EntityTargetEvent event = targetEvent(player, EntityTargetEvent.TargetReason.CLOSEST_PLAYER);

            prepare(new PacificationSkill(), false).onEntityTarget(event);

            verify(event, never()).setCancelled(true);
        }

        @Test
        void ignoresNonPlayerTargets() {
            EntityTargetEvent event = targetEvent(mock(Entity.class), EntityTargetEvent.TargetReason.CLOSEST_ENTITY);

            prepare(new PacificationSkill(), true).onEntityTarget(event);

            verify(event, never()).setCancelled(true);
        }
    }

    @Nested
    class AntiKnockback {

        @Test
        void zeroesTheVelocity() {
            EntityDamageEvent event = damageEvent(player, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
            player.setVelocity(new org.bukkit.util.Vector(1, 1, 1));

            prepare(new AntiKnockbackSkill(), true).onDamage(event);

            assertEquals(0, player.getVelocity().length(), 0.0001);
        }

        @Test
        void leavesPlayersWithoutTheSkillAlone() {
            EntityDamageEvent event = damageEvent(player, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
            player.setVelocity(new org.bukkit.util.Vector(1, 0, 0));

            prepare(new AntiKnockbackSkill(), false).onDamage(event);

            assertEquals(1, player.getVelocity().length(), 0.0001);
        }
    }

    // --- the registrar -------------------------------------------------------------------------

    @Test
    @DisplayName("every documented builtin type is registered, and none other")
    void registrarCoversEveryBuiltinType() {
        BuiltinSkillRegistrar.registerAll(plugin);

        assertEquals(java.util.Set.of(
                BuiltinSkills.FLY,
                BuiltinSkills.FLY_SPEED,
                BuiltinSkills.EFFECT,
                BuiltinSkills.KEEP_INVENTORY,
                BuiltinSkills.KEEP_EXPERIENCE,
                BuiltinSkills.PACIFICATION,
                BuiltinSkills.ANTI_PHANTOM,
                BuiltinSkills.HARVEST,
                BuiltinSkills.AUTO_SMELT,
                BuiltinSkills.ANTI_KNOCKBACK,
                BuiltinSkills.CURE_EFFECT,
                BuiltinSkills.SECOND_CHANCE,
                BuiltinSkills.BROKEN_FALL,
                BuiltinSkills.VACCUM), registry.getIds());
    }

    @Test
    @DisplayName("registering the builtins twice is refused, so a double load is caught")
    void registrarIsNotIdempotent() {
        BuiltinSkillRegistrar.registerAll(plugin);

        assertTrue(registry.isRegistered(BuiltinSkills.FLY));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> BuiltinSkillRegistrar.registerAll(plugin));
    }

    @Test
    @DisplayName("the ids in BuiltinSkills match what the handlers declare")
    void constantsMatchTheHandlers() {
        UUID uuid = UUID.randomUUID();
        BuiltinSkillRegistrar.registerAll(plugin);

        for (String id : registry.getIds()) {
            SkillHandler handler = registry.get(id);
            assertEquals(id, handler.getId());
            assertFalse(handler.isActive(uuid));
        }
    }
}
