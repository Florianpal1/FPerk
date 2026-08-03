package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Drops the smelted ingot instead of the raw ore, fortune included.
 */
public class AutoSmeltSkill extends SkillHandler implements Listener {

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

    public AutoSmeltSkill() {
        super(BuiltinSkills.AUTO_SMELT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() || !isActive(event.getPlayer())) {
            return;
        }

        Block block = event.getBlock();
        Material mineral = getMineral(block);
        if (mineral == null) {
            return;
        }

        Material ore = minerals.get(mineral);
        Material ingot = ingots.get(ore);
        int count = countMineral(block, ore);
        count = applyLuck(event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.FORTUNE), count);

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(ingot, count));
    }

    public Material getMineral(Block block) {
        return minerals.containsKey(block.getType()) ? block.getType() : null;
    }

    public int countMineral(Block block, Material ore) {
        int count = 0;
        for (ItemStack itemStack : block.getDrops()) {
            if (itemStack.getType().equals(ore)) {
                count = count + itemStack.getAmount();
            }
        }
        return count;
    }

    private int applyLuck(int enchantmentLevel, int count) {
        double d = Math.random();
        switch (enchantmentLevel) {
            case 1 -> {
                if (d <= 0.33) {
                    count = count * 2;
                }
            }
            case 2 -> {
                if (d <= 0.2) {
                    count = count * 3;
                } else if (d <= 0.45) {
                    count = count * 2;
                }
            }
            case 3 -> {
                if (d <= 0.20) {
                    count = count * 4;
                } else if (d <= 0.4) {
                    count = count * 3;
                } else if (d <= 0.6) {
                    count = count * 2;
                }
            }
        }

        return count;
    }
}