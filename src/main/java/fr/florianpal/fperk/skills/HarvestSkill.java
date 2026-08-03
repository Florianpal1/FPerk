package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Replants a crop right after it has been harvested, taking one seed from the inventory.
 */
public class HarvestSkill extends SkillHandler implements Listener {

    public HarvestSkill() {
        super(BuiltinSkills.HARVEST);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!isActive(player) || !isFarmable(block)) {
            return;
        }

        Material seedBlockType = getSeedBlockType(block.getType());
        Material seedItemType = getSeedItemType(block.getType());

        if (seedBlockType != null && seedItemType != null && hasSeed(player, seedItemType)) {
            deductSeed(player, seedItemType);
            plant(block, seedBlockType);
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

    public boolean isFarmable(Block block) {
        return block.getType().equals(Material.POTATOES) || block.getType().equals(Material.CARROTS)
                || block.getType().equals(Material.WHEAT) || block.getType().equals(Material.WHEAT_SEEDS)
                || block.getType().equals(Material.BEETROOTS);
    }

    public boolean hasSeed(Player player, Material seed) {
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            return true;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().equals(seed)) {
                return true;
            }
        }
        return false;
    }

    public void deductSeed(Player player, Material seed) {
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().equals(seed)) {
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

    private void plant(Block block, Material seedType) {
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> block.setType(seedType), 1);
    }
}