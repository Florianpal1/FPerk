package fr.florianpal.fperk.listeners;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.enums.EffectType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class BlockBreakListener implements Listener {

    private final FPerk plugin;
    
    private final Map<Material, Material> minerals = Map.of(
            Material.GOLD_ORE, Material.RAW_GOLD,
            Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD,
            Material.IRON_ORE, Material.RAW_IRON,
            Material.DEEPSLATE_IRON_ORE, Material.RAW_IRON,
            Material.COPPER_ORE, Material.RAW_COPPER,
            Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER
    );

    private final Map<Material, Material> ingots = Map.of(
            Material.RAW_GOLD, Material.GOLD_INGOT,
            Material.RAW_IRON, Material.IRON_INGOT,
            Material.RAW_COPPER, Material.COPPER_INGOT
    );

    public BlockBreakListener(FPerk plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityTargetEvent(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (plugin.isPerkActive(player.getUniqueId(), EffectType.HARVEST)) {

            if (!isFarmable(block)) {
                Material seedBlockType = getSeedBlockType(block.getType());
                Material seedItemType = getSeedItemType(block.getType());
                if (seedBlockType == null || seedItemType == null) {
                    if (hasSeed(player, seedItemType)) {
                        deductSeed(player, seedItemType);
                        plant(plugin, block, seedBlockType);
                    }
                }
            }
        }

        if(plugin.isPerkActive(player.getUniqueId(), EffectType.AUTO_SMELT)) {

            Material material = getMineral(block);
            if(material != null) {
                Material ore = minerals.get(material);
                Material ingot = ingots.get(ore);
                int count = countMineral(block, ore);
                event.setDropItems(false);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(ingot, count));
            }
        }
    }

    public Material getSeedBlockType(Material crop) {
        return switch (crop) {
            case WHEAT, WHEAT_SEEDS -> Material.WHEAT;
            case POTATOES -> Material.POTATOES;
            case BEETROOT, BEETROOT_SEEDS -> Material.BEETROOT_SEEDS;
            case CARROTS -> Material.CARROTS;
            case BEETROOTS -> Material.BEETROOTS;
            default -> null;
        };
    }

    public Material getSeedItemType(Material crop) {
        return switch (crop) {
            case WHEAT, WHEAT_SEEDS -> Material.WHEAT_SEEDS;
            case POTATOES -> Material.POTATO;
            case BEETROOT, BEETROOT_SEEDS, BEETROOTS -> Material.BEETROOT_SEEDS;
            case CARROTS -> Material.CARROT;
            default -> null;
        };
    }

    public void deductSeed(Player player, Material seed) {
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && (item.getType().equals(seed))) {
                if (item.getAmount() == 1) {
                    item.setAmount(0);
                    item.setType(Material.AIR);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
                player.updateInventory();
                return;
            }
        }
    }
    
    public Material getMineral(Block block) {
        if(minerals.containsKey(block.getType())) {
            return block.getType();   
        }
        return null;
    }

    public int countMineral(Block block, Material ore) {
        int count = 0;
        for(ItemStack itemStack : block.getDrops()) {
            if(itemStack.getType().equals(ore)) {
                count = count + itemStack.getAmount();
            }
        }
        return count;
    }

    private void plant(FPerk plugin, Block block, Material seedType) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> block.setType(seedType), 1);
    }

    public boolean hasSeed(Player player, Material seed) {
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            return true;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && (item.getType().equals(seed))) {
                return true;
            }
        }
        return false;
    }

    public boolean isFarmable(Block b) {
        return b.getType().equals(Material.POTATOES) || b.getType().equals(Material.CARROTS)
                || b.getType().equals(Material.WHEAT) || b.getType().equals(Material.WHEAT_SEEDS)
                || b.getType().equals(Material.BEETROOTS);
    }
}
