package fr.florianpal.fperk.managers;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.api.HandlerBinding;
import fr.florianpal.fperk.api.SkillContext;
import fr.florianpal.fperk.api.SkillHandler;
import fr.florianpal.fperk.api.SkillRegistry;
import fr.florianpal.fperk.configurations.PerkConfig;
import fr.florianpal.fperk.managers.commandManagers.PlayerPerkCommandManager;
import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.objects.PlayerPerk;
import fr.florianpal.fperk.objects.Skill;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The orchestration layer : which handler runs when, and when a timed perk turns itself off.
 */
class SkillServiceTest {

    private ServerMock server;

    private FPerk plugin;

    private Plugin host;

    private SkillRegistry registry;

    private PlayerPerkCommandManager storage;

    private PerkConfig perkConfig;

    private SkillService service;

    private PlayerMock player;

    /**
     * Records the lifecycle calls it receives, and can be told to blow up.
     */
    private static final class Recording extends SkillHandler {

        final List<String> calls = new ArrayList<>();

        boolean explode;

        Recording(String id) {
            super(id);
        }

        @Override
        public void onEnable(SkillContext context) {
            calls.add("enable");
            if (explode) {
                throw new IllegalStateException("boom");
            }
        }

        @Override
        public void onDisable(SkillContext context) {
            calls.add("disable");
            if (explode) {
                throw new IllegalStateException("boom");
            }
        }

        @Override
        public void onRestore(SkillContext context) {
            calls.add("restore");
        }
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        host = MockBukkit.createMockPlugin("Host");

        plugin = mock(FPerk.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("FPerkTest"));
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getName()).thenReturn("FPerk");
        when(plugin.getServer()).thenReturn(server);

        registry = new SkillRegistry(plugin);
        when(plugin.getSkillRegistry()).thenReturn(registry);

        storage = mock(PlayerPerkCommandManager.class);
        when(plugin.getPlayerPerkCommandManager()).thenReturn(storage);

        perkConfig = mock(PerkConfig.class);
        ConfigurationManager configuration = mock(ConfigurationManager.class);
        when(configuration.getPerkConfig()).thenReturn(perkConfig);
        when(plugin.getConfigurationManager()).thenReturn(configuration);

        service = new SkillService(plugin);
        player = server.addPlayer("Tester");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private Recording handler(String id) {
        Recording handler = new Recording(id);
        HandlerBinding.bind(handler, plugin, host);
        registry.register(host, handler);
        return handler;
    }

    private Perk perk(String id, boolean persistant, int timeSeconds, String... skillTypes) {
        Map<String, Skill> skills = new LinkedHashMap<>();
        for (int i = 0; i < skillTypes.length; i++) {
            skills.put("skill" + i, new Skill("skill" + i, List.of("n"), skillTypes[i], "", 1F));
        }

        return new Perk(id, id, Material.STONE, skills, 0, true, timeSeconds, persistant,
                "fperk." + id, null, "");
    }

    private PlayerPerk state(String perkId, boolean enabled, long lastEnabledMillisAgo) {
        return new PlayerPerk(1, player.getUniqueId(), perkId,
                new Date().getTime() - lastEnabledMillisAgo, enabled);
    }

    // --- activation ----------------------------------------------------------------------------

    @Test
    void enableRunsEveryHandlerOfThePerkAndMarksThemActive() {
        Recording fly = handler("FLY");
        Recording harvest = handler("HARVEST");
        Perk perk = perk("combo", true, 0, "FLY", "HARVEST");

        service.enable(player, state("combo", true, 0), perk);

        assertEquals(List.of("enable"), fly.calls);
        assertEquals(List.of("enable"), harvest.calls);
        assertTrue(registry.isActive(player.getUniqueId(), "FLY"));
        assertTrue(registry.isActive(player.getUniqueId(), "HARVEST"));
    }

    @Test
    @DisplayName("enabling stamps the activation date, which the cooldown and the duration read")
    void enableStampsTheActivationDate() {
        handler("FLY");
        PlayerPerk playerPerk = state("fly", false, 500_000L);
        long before = playerPerk.getLastEnabled().getTime();

        service.enable(player, playerPerk, perk("fly", true, 0, "FLY"));

        assertTrue(playerPerk.getLastEnabled().getTime() > before,
                "a stale date would make the perk expire immediately on the next join");
        assertTrue(Math.abs(new Date().getTime() - playerPerk.getLastEnabled().getTime()) < 5_000L);
    }

    @Test
    @DisplayName("an unknown skill type is skipped, and the other skills of the perk still apply")
    void enableSkipsUnknownTypes() {
        Recording fly = handler("FLY");
        Perk perk = perk("combo", true, 0, "FLY", "NOT_REGISTERED");

        service.enable(player, state("combo", true, 0), perk);

        assertEquals(List.of("enable"), fly.calls);
        assertFalse(registry.isActive(player.getUniqueId(), "NOT_REGISTERED"));
    }

    @Test
    @DisplayName("a handler that throws is contained : its siblings still run")
    void enableContainsHandlerFailures() {
        Recording broken = handler("BROKEN");
        broken.explode = true;
        Recording healthy = handler("HEALTHY");

        service.enable(player, state("combo", true, 0), perk("combo", true, 0, "BROKEN", "HEALTHY"));

        assertEquals(List.of("enable"), healthy.calls);
        assertTrue(registry.isActive(player.getUniqueId(), "HEALTHY"));
    }

    // --- deactivation --------------------------------------------------------------------------

    @Test
    void disableRunsEveryHandlerAndClearsTheState() {
        Recording fly = handler("FLY");
        Perk perk = perk("fly", true, 0, "FLY");
        PlayerPerk playerPerk = state("fly", true, 0);
        service.enable(player, playerPerk, perk);

        service.disable(player, playerPerk, perk);

        assertEquals(List.of("enable", "disable"), fly.calls);
        assertFalse(registry.isActive(player.getUniqueId(), "FLY"));
    }

    @Test
    @DisplayName("disabling twice is harmless, as the contract promises handlers")
    void disableIsIdempotent() {
        Recording fly = handler("FLY");
        Perk perk = perk("fly", true, 0, "FLY");
        PlayerPerk playerPerk = state("fly", false, 0);

        service.disable(player, playerPerk, perk);
        service.disable(player, playerPerk, perk);

        assertEquals(List.of("disable", "disable"), fly.calls);
    }

    // --- expiry --------------------------------------------------------------------------------

    @Test
    @DisplayName("a timed perk turns itself off after time seconds, and the change is persisted")
    void timedPerkExpires() {
        Recording fly = handler("FLY");
        PlayerPerk playerPerk = state("fly", true, 0);

        service.enable(player, playerPerk, perk("fly", false, 5, "FLY"));

        server.getScheduler().performTicks(4 * 20);
        assertTrue(registry.isActive(player.getUniqueId(), "FLY"), "still on before the deadline");

        server.getScheduler().performTicks(20 + 1);

        assertEquals(List.of("enable", "disable"), fly.calls);
        assertFalse(registry.isActive(player.getUniqueId(), "FLY"));
        assertFalse(playerPerk.isEnabled());
        verify(storage).updatePlayerPerk(playerPerk);
    }

    @Test
    @DisplayName("a persistant perk schedules nothing, whatever time says")
    void persistentPerkNeverExpires() {
        Recording fly = handler("FLY");

        service.enable(player, state("fly", true, 0), perk("fly", true, 1, "FLY"));

        server.getScheduler().performTicks(200);

        assertEquals(List.of("enable"), fly.calls);
        assertTrue(registry.isActive(player.getUniqueId(), "FLY"));
        verify(storage, never()).updatePlayerPerk(any());
    }

    @Test
    @DisplayName("turning a perk off cancels its pending expiry, so it cannot fire later")
    void disableCancelsThePendingExpiry() {
        Recording fly = handler("FLY");
        Perk perk = perk("fly", false, 5, "FLY");
        PlayerPerk playerPerk = state("fly", true, 0);
        service.enable(player, playerPerk, perk);

        service.disable(player, playerPerk, perk);
        server.getScheduler().performTicks(10 * 20);

        assertEquals(List.of("enable", "disable"), fly.calls, "the timer must not fire a second disable");
    }

    @Test
    @DisplayName("re-enabling reschedules from scratch instead of stacking timers")
    void enableReschedulesRatherThanStacking() {
        Recording fly = handler("FLY");
        Perk perk = perk("fly", false, 5, "FLY");
        PlayerPerk playerPerk = state("fly", true, 0);

        service.enable(player, playerPerk, perk);
        server.getScheduler().performTicks(3 * 20);
        service.enable(player, playerPerk, perk);
        server.getScheduler().performTicks(3 * 20);

        assertEquals(List.of("enable", "enable"), fly.calls, "the first timer was dropped");

        server.getScheduler().performTicks(2 * 20 + 1);
        assertEquals(List.of("enable", "enable", "disable"), fly.calls);
    }

    // --- restoration ---------------------------------------------------------------------------

    @Test
    @DisplayName("restoring calls onRestore, not onEnable")
    void restoreUsesItsOwnCallback() {
        Recording fly = handler("FLY");

        service.restore(player, state("fly", true, 1_000L), perk("fly", true, 0, "FLY"));

        assertEquals(List.of("restore"), fly.calls);
        assertTrue(registry.isActive(player.getUniqueId(), "FLY"));
    }

    @Test
    @DisplayName("a timed perk resumes with the duration it has left, not a full one")
    void restoreResumesTheRemainingDuration() {
        Recording fly = handler("FLY");

        // 10 seconds long, activated 6 seconds ago : 4 left.
        service.restore(player, state("fly", true, 6_000L), perk("fly", false, 10, "FLY"));

        server.getScheduler().performTicks(3 * 20);
        assertEquals(List.of("restore"), fly.calls, "still on after 3 of the 4 remaining seconds");

        server.getScheduler().performTicks(20 + 1);
        assertEquals(List.of("restore", "disable"), fly.calls);
    }

    @Test
    @DisplayName("a duration that ran out while the player was away turns the perk off")
    void restoreDropsAnExpiredPerk() {
        Recording fly = handler("FLY");
        PlayerPerk playerPerk = state("fly", true, 60_000L);

        service.restore(player, playerPerk, perk("fly", false, 10, "FLY"));

        assertEquals(List.of("disable"), fly.calls);
        assertFalse(registry.isActive(player.getUniqueId(), "FLY"));
        assertFalse(playerPerk.isEnabled());
        verify(storage).updatePlayerPerk(playerPerk);
    }

    @Test
    @DisplayName("a persistant perk is restored however old its activation is")
    void restoreAlwaysRestoresPersistentPerks() {
        Recording keep = handler("KEEP_INVENTORY");

        service.restore(player, state("keep", true, 999_999_999L), perk("keep", true, 0, "KEEP_INVENTORY"));

        assertEquals(List.of("restore"), keep.calls);
        assertTrue(registry.isActive(player.getUniqueId(), "KEEP_INVENTORY"));
    }

    // --- syncing a player ----------------------------------------------------------------------

    @Test
    @DisplayName("on join, an enabled perk is restored only while its permission is held")
    void syncRestoresWhatThePlayerMayStillUse() {
        Recording allowed = handler("ALLOWED");
        Recording revoked = handler("REVOKED");

        Perk allowedPerk = perk("allowed", true, 0, "ALLOWED");
        Perk revokedPerk = perk("revoked", true, 0, "REVOKED");
        when(perkConfig.getPerks()).thenReturn(Map.of("allowed", allowedPerk, "revoked", revokedPerk));

        player.addAttachment(host, "fperk.allowed", true);

        service.syncPlayer(player, List.of(state("allowed", true, 0), state("revoked", true, 0)));

        assertEquals(List.of("restore"), allowed.calls);
        assertEquals(List.of("disable"), revoked.calls);
        assertTrue(registry.isActive(player.getUniqueId(), "ALLOWED"));
        assertFalse(registry.isActive(player.getUniqueId(), "REVOKED"));
    }

    @Test
    @DisplayName("a disabled perk is asserted off rather than restored")
    void syncAssertsDisabledPerksOff() {
        Recording fly = handler("FLY");
        when(perkConfig.getPerks()).thenReturn(Map.of("fly", perk("fly", true, 0, "FLY")));
        player.addAttachment(host, "fperk.fly", true);

        service.syncPlayer(player, List.of(state("fly", false, 0)));

        assertTrue(fly.calls.contains("disable"));
        assertFalse(fly.calls.contains("restore"));
    }

    @Test
    @DisplayName("a perk id no longer in perk.yml is skipped, not fatal")
    void syncSkipsUnknownPerks() {
        when(perkConfig.getPerks()).thenReturn(Map.of());

        service.syncPlayer(player, List.of(state("deleted_perk", true, 0)));

        assertTrue(registry.getActiveSkills(player.getUniqueId()).isEmpty());
    }

    // --- resetting and forgetting --------------------------------------------------------------

    @Test
    @DisplayName("reset all notifies every handler so an addon can undo its own changes")
    void resetAllDisablesEverything() {
        Recording fly = handler("FLY");
        Recording harvest = handler("HARVEST");
        when(perkConfig.getPerks()).thenReturn(Map.of(
                "fly", perk("fly", true, 0, "FLY"),
                "harvest", perk("harvest", true, 0, "HARVEST")));

        List<PlayerPerk> states = List.of(state("fly", true, 0), state("harvest", true, 0));
        service.enable(player, states.get(0), perk("fly", true, 0, "FLY"));
        service.enable(player, states.get(1), perk("harvest", true, 0, "HARVEST"));

        service.resetAll(player, states);

        assertTrue(fly.calls.contains("disable"));
        assertTrue(harvest.calls.contains("disable"));
        assertTrue(registry.getActiveSkills(player.getUniqueId()).isEmpty());
    }

    @Test
    @DisplayName("leaving drops the active state and the pending timers, without touching storage")
    void forgetClearsEverythingLocally() {
        Recording fly = handler("FLY");
        service.enable(player, state("fly", true, 0), perk("fly", false, 5, "FLY"));

        service.forget(player.getUniqueId());
        server.getScheduler().performTicks(10 * 20);

        assertTrue(registry.getActiveSkills(player.getUniqueId()).isEmpty());
        assertEquals(List.of("enable"), fly.calls, "no expiry fires for a player who left");
        verify(storage, never()).updatePlayerPerk(any());
    }

    // --- multi server --------------------------------------------------------------------------

    @Test
    @DisplayName("a perk enabled on another server is visible to addons, without running handlers")
    void marksSkillsActiveForOfflinePlayers() {
        Recording fly = handler("FLY");
        java.util.UUID elsewhere = java.util.UUID.randomUUID();

        service.markActiveOffline(elsewhere, perk("fly", true, 0, "FLY"));

        assertTrue(registry.isActive(elsewhere, "FLY"));
        assertTrue(fly.calls.isEmpty(), "the player is not on this server, nothing to apply");

        service.markInactiveOffline(elsewhere, perk("fly", true, 0, "FLY"));

        assertFalse(registry.isActive(elsewhere, "FLY"));
        assertTrue(fly.calls.isEmpty());
    }

    @Test
    @DisplayName("expiry keeps the players apart : one timer firing leaves the other alone")
    void expiryIsScopedToOnePlayer() {
        Recording fly = handler("FLY");
        PlayerMock other = server.addPlayer("Other");
        Perk perk = perk("fly", false, 5, "FLY");

        service.enable(player, state("fly", true, 0), perk);
        service.enable(other, new PlayerPerk(2, other.getUniqueId(), "fly", new Date().getTime(), true),
                perk("fly", false, 20, "FLY"));

        server.getScheduler().performTicks(5 * 20 + 1);

        assertFalse(registry.isActive(player.getUniqueId(), "FLY"));
        assertTrue(registry.isActive(other.getUniqueId(), "FLY"));
        verify(storage, atLeastOnce()).updatePlayerPerk(any());
    }
}
