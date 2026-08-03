package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.api.HandlerBinding;
import fr.florianpal.fperk.api.SkillHandler;
import fr.florianpal.fperk.api.SkillRegistry;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The skills that read or change the world : harvesting, smelting, vacuuming, and the phantom guard.
 */
class WorldSkillsTest {

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
        when(plugin.getServer()).thenReturn(server);

        registry = new SkillRegistry(plugin);
        when(plugin.getSkillRegistry()).thenReturn(registry);

        player = server.addPlayer("Tester");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private <T extends SkillHandler> T prepare(T handler, boolean active) {
        HandlerBinding.bind(handler, plugin, plugin);
        if (active) {
            registry.markActive(player.getUniqueId(), handler.getId());
        }
        return handler;
    }

    private BlockBreakEvent breakEvent(Block block, boolean cancelled) {
        BlockBreakEvent event = mock(BlockBreakEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getBlock()).thenReturn(block);
        when(event.isCancelled()).thenReturn(cancelled);
        return event;
    }

    @Nested
    class AntiPhantom {

        private EntityTargetEvent targetEvent(EntityType hunter, Entity target,
                                              EntityTargetEvent.TargetReason reason) {
            Entity entity = mock(Entity.class);
            when(entity.getType()).thenReturn(hunter);

            EntityTargetEvent event = mock(EntityTargetEvent.class);
            when(event.getEntity()).thenReturn(entity);
            when(event.getTarget()).thenReturn(target);
            when(event.getReason()).thenReturn(reason);
            return event;
        }

        @Test
        void stopsPhantomsFromTargetingThePlayer() {
            EntityTargetEvent event = targetEvent(EntityType.PHANTOM, player,
                    EntityTargetEvent.TargetReason.CLOSEST_PLAYER);

            prepare(new AntiPhantomSkill(), true).onEntityTarget(event);

            verify(event).setCancelled(true);
        }

        @Test
        @DisplayName("other mobs are untouched : that is what PACIFICATION is for")
        void leavesOtherMobsAlone() {
            EntityTargetEvent event = targetEvent(EntityType.ZOMBIE, player,
                    EntityTargetEvent.TargetReason.CLOSEST_PLAYER);

            prepare(new AntiPhantomSkill(), true).onEntityTarget(event);

            verify(event, never()).setCancelled(true);
        }

        @Test
        void leavesPlayersWithoutTheSkillAlone() {
            EntityTargetEvent event = targetEvent(EntityType.PHANTOM, player,
                    EntityTargetEvent.TargetReason.CLOSEST_PLAYER);

            prepare(new AntiPhantomSkill(), false).onEntityTarget(event);

            verify(event, never()).setCancelled(true);
        }
    }

    @Nested
    class AutoSmelt {

        private Block ore(Material type, Material drop, int amount) {
            World world = mock(World.class);
            Location location = new Location(null, 0, 64, 0);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(type);
            when(block.getWorld()).thenReturn(world);
            when(block.getLocation()).thenReturn(location);
            when(block.getDrops()).thenReturn(java.util.Set.of(new ItemStack(drop, amount)));
            return block;
        }

        @Test
        @DisplayName("iron ore drops an ingot instead of the raw ore")
        void dropsTheSmeltedIngot() {
            Block block = ore(Material.IRON_ORE, Material.RAW_IRON, 1);
            BlockBreakEvent event = breakEvent(block, false);

            prepare(new AutoSmeltSkill(), true).onBlockBreak(event);

            verify(event).setDropItems(false);
            verify(block.getWorld()).dropItemNaturally(block.getLocation(),
                    new ItemStack(Material.IRON_INGOT, 1));
        }

        @Test
        @DisplayName("the deepslate variants smelt too")
        void handlesDeepslateVariants() {
            Block block = ore(Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD, 1);

            prepare(new AutoSmeltSkill(), true).onBlockBreak(breakEvent(block, false));

            verify(block.getWorld()).dropItemNaturally(any(Location.class),
                    eq(new ItemStack(Material.GOLD_INGOT, 1)));
        }

        @Test
        void copperKeepsItsDropCount() {
            Block block = ore(Material.COPPER_ORE, Material.RAW_COPPER, 3);

            prepare(new AutoSmeltSkill(), true).onBlockBreak(breakEvent(block, false));

            verify(block.getWorld()).dropItemNaturally(any(Location.class),
                    eq(new ItemStack(Material.COPPER_INGOT, 3)));
        }

        @Test
        @DisplayName("a block that is not a smeltable ore is left to vanilla")
        void ignoresOtherBlocks() {
            Block block = ore(Material.STONE, Material.STONE, 1);
            BlockBreakEvent event = breakEvent(block, false);

            prepare(new AutoSmeltSkill(), true).onBlockBreak(event);

            verify(event, never()).setDropItems(false);
            verify(block.getWorld(), never()).dropItemNaturally(any(), any(ItemStack.class));
        }

        @Test
        void leavesPlayersWithoutTheSkillAlone() {
            Block block = ore(Material.IRON_ORE, Material.RAW_IRON, 1);
            BlockBreakEvent event = breakEvent(block, false);

            prepare(new AutoSmeltSkill(), false).onBlockBreak(event);

            verify(event, never()).setDropItems(false);
        }

        @Test
        @DisplayName("a break cancelled by a protection plugin is not smelted")
        void skipsACancelledBreak() {
            Block block = ore(Material.IRON_ORE, Material.RAW_IRON, 1);
            BlockBreakEvent event = breakEvent(block, true);

            prepare(new AutoSmeltSkill(), true).onBlockBreak(event);

            verify(event, never()).setDropItems(false);
        }

        @Test
        void recognisesTheOresItHandles() {
            AutoSmeltSkill skill = prepare(new AutoSmeltSkill(), true);

            assertEquals(Material.IRON_ORE, skill.getMineral(ore(Material.IRON_ORE, Material.RAW_IRON, 1)));
            assertNull(skill.getMineral(ore(Material.DIAMOND_ORE, Material.DIAMOND, 1)));
        }

        @Test
        void countsOnlyTheMatchingDrops() {
            AutoSmeltSkill skill = prepare(new AutoSmeltSkill(), true);
            Block block = mock(Block.class);
            when(block.getDrops()).thenReturn(java.util.Set.of(
                    new ItemStack(Material.RAW_IRON, 2),
                    new ItemStack(Material.COBBLESTONE, 5)));

            assertEquals(2, skill.countMineral(block, Material.RAW_IRON));
            assertEquals(0, skill.countMineral(block, Material.RAW_GOLD));
        }
    }

    @Nested
    class Harvest {

        private Block crop(Material type) {
            Block block = mock(Block.class);
            when(block.getType()).thenReturn(type);
            return block;
        }

        @Test
        @DisplayName("wheat is replanted, and one seed is taken from the inventory")
        void replantsWheatAndSpendsASeed() {
            player.getInventory().addItem(new ItemStack(Material.WHEAT_SEEDS, 5));
            Block block = crop(Material.WHEAT);

            prepare(new HarvestSkill(), true).onBlockBreak(breakEvent(block, false));
            server.getScheduler().performTicks(2);

            verify(block).setType(Material.WHEAT);
            assertEquals(4, player.getInventory().getItem(0).getAmount());
        }

        @Test
        void replantsCarrotsAndPotatoes() {
            player.getInventory().addItem(new ItemStack(Material.CARROT, 1));
            player.getInventory().addItem(new ItemStack(Material.POTATO, 1));

            HarvestSkill skill = prepare(new HarvestSkill(), true);
            Block carrots = crop(Material.CARROTS);
            Block potatoes = crop(Material.POTATOES);

            skill.onBlockBreak(breakEvent(carrots, false));
            skill.onBlockBreak(breakEvent(potatoes, false));
            server.getScheduler().performTicks(2);

            verify(carrots).setType(Material.CARROTS);
            verify(potatoes).setType(Material.POTATOES);
        }

        @Test
        @DisplayName("nothing is replanted without a seed to spend")
        void needsASeed() {
            Block block = crop(Material.WHEAT);

            prepare(new HarvestSkill(), true).onBlockBreak(breakEvent(block, false));
            server.getScheduler().performTicks(2);

            verify(block, never()).setType(any(Material.class));
        }

        @Test
        @DisplayName("creative mode replants without spending anything")
        void creativeNeedsNoSeed() {
            player.setGameMode(GameMode.CREATIVE);
            Block block = crop(Material.WHEAT);

            prepare(new HarvestSkill(), true).onBlockBreak(breakEvent(block, false));
            server.getScheduler().performTicks(2);

            verify(block).setType(Material.WHEAT);
        }

        @Test
        void ignoresBlocksThatAreNotCrops() {
            Block block = crop(Material.STONE);

            prepare(new HarvestSkill(), true).onBlockBreak(breakEvent(block, false));
            server.getScheduler().performTicks(2);

            verify(block, never()).setType(any(Material.class));
        }

        @Test
        void leavesPlayersWithoutTheSkillAlone() {
            player.getInventory().addItem(new ItemStack(Material.WHEAT_SEEDS, 5));
            Block block = crop(Material.WHEAT);

            prepare(new HarvestSkill(), false).onBlockBreak(breakEvent(block, false));
            server.getScheduler().performTicks(2);

            verify(block, never()).setType(any(Material.class));
        }

        @Test
        void skipsACancelledBreak() {
            player.getInventory().addItem(new ItemStack(Material.WHEAT_SEEDS, 5));
            Block block = crop(Material.WHEAT);

            prepare(new HarvestSkill(), true).onBlockBreak(breakEvent(block, true));
            server.getScheduler().performTicks(2);

            verify(block, never()).setType(any(Material.class));
        }

        @Test
        @DisplayName("spending the last seed empties the slot rather than leaving a zero stack")
        void spendingTheLastSeedClearsTheSlot() {
            HarvestSkill skill = prepare(new HarvestSkill(), true);
            player.getInventory().addItem(new ItemStack(Material.WHEAT_SEEDS, 1));

            skill.deductSeed(player, Material.WHEAT_SEEDS);

            ItemStack slot = player.getInventory().getItem(0);
            assertTrue(slot == null || slot.getType() == Material.AIR, "the slot should be empty");
        }

        @Test
        void mapsCropsToTheirSeeds() {
            HarvestSkill skill = prepare(new HarvestSkill(), true);

            assertEquals(Material.WHEAT_SEEDS, skill.getSeedItemType(Material.WHEAT));
            assertEquals(Material.CARROT, skill.getSeedItemType(Material.CARROTS));
            assertEquals(Material.POTATO, skill.getSeedItemType(Material.POTATOES));
            assertEquals(Material.BEETROOT_SEEDS, skill.getSeedItemType(Material.BEETROOTS));
            assertNull(skill.getSeedItemType(Material.STONE));

            assertTrue(skill.isFarmable(crop(Material.WHEAT)));
            assertTrue(skill.isFarmable(crop(Material.BEETROOTS)));
            assertFalse(skill.isFarmable(crop(Material.STONE)));
        }
    }

    @Nested
    class Vacuum {

        private Item droppedItem(ItemStack stack) {
            Item item = mock(Item.class);
            when(item.getItemStack()).thenReturn(stack);
            when(item.canPlayerPickup()).thenReturn(true);
            return item;
        }

        @Test
        @DisplayName("an item lands in a free slot and is removed from the ground")
        void picksUpIntoAFreeSlot() {
            VacuumSkill skill = prepare(new VacuumSkill(), true);
            Item item = droppedItem(new ItemStack(Material.DIAMOND, 2));

            assertTrue(skill.havePlaceInInventory(player, item));
            skill.addItemInInventory(player, item);

            assertTrue(player.getInventory().contains(Material.DIAMOND, 2));
            verify(item).remove();
        }

        @Test
        @DisplayName("an item merges into a matching stack rather than taking a new slot")
        void mergesIntoAnExistingStack() {
            VacuumSkill skill = prepare(new VacuumSkill(), true);
            for (int slot = 0; slot < player.getInventory().getStorageContents().length; slot++) {
                player.getInventory().setItem(slot, new ItemStack(Material.STONE, 1));
            }
            player.getInventory().setItem(0, new ItemStack(Material.DIAMOND, 1));

            Item item = droppedItem(new ItemStack(Material.DIAMOND, 2));

            assertTrue(skill.havePlaceInInventory(player, item), "a partial stack counts as room");
            skill.addItemInInventory(player, item);

            assertEquals(3, player.getInventory().getItem(0).getAmount());
            verify(item).remove();
        }

        @Test
        @DisplayName("a full inventory with no matching stack has no room")
        void reportsAFullInventory() {
            VacuumSkill skill = prepare(new VacuumSkill(), true);
            for (int slot = 0; slot < player.getInventory().getStorageContents().length; slot++) {
                player.getInventory().setItem(slot, new ItemStack(Material.STONE, 1));
            }

            Item item = droppedItem(new ItemStack(Material.DIAMOND, 1));

            assertFalse(skill.havePlaceInInventory(player, item));
        }

        @Test
        @DisplayName("a stack that would overflow is not merged")
        void refusesAnOverflowingMerge() {
            VacuumSkill skill = prepare(new VacuumSkill(), true);
            for (int slot = 0; slot < player.getInventory().getStorageContents().length; slot++) {
                player.getInventory().setItem(slot, new ItemStack(Material.STONE, 1));
            }
            player.getInventory().setItem(0, new ItemStack(Material.DIAMOND, 64));

            Item item = droppedItem(new ItemStack(Material.DIAMOND, 5));

            assertFalse(skill.havePlaceInInventory(player, item));
        }

        @Test
        @DisplayName("moving does nothing for a player without the skill")
        void leavesPlayersWithoutTheSkillAlone() {
            VacuumSkill skill = prepare(new VacuumSkill(), false);

            skill.onMove(new PlayerMoveEvent(player, player.getLocation(), player.getLocation()));

            assertTrue(player.getInventory().isEmpty());
        }

        @Test
        @DisplayName("moving with nothing nearby is harmless")
        void toleratesAnEmptyNeighbourhood() {
            VacuumSkill skill = prepare(new VacuumSkill(), true);

            skill.onMove(new PlayerMoveEvent(player, player.getLocation(), player.getLocation()));

            assertTrue(player.getInventory().isEmpty());
        }
    }
}
