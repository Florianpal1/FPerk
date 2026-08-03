package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillHandler;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Picks up the items lying around the player.
 *
 * <p>The historical spelling {@code VACCUM} is kept as the skill type so that existing
 * {@code skill.yml} files keep working.</p>
 */
public class VacuumSkill extends SkillHandler implements Listener {

    private static final int RANGE = 3;

    public VacuumSkill() {
        super(BuiltinSkills.VACCUM);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)) {
            return;
        }

        List<Entity> entities = event.getTo().getNearbyEntities(RANGE, RANGE, RANGE).stream().toList();
        for (Entity entity : entities) {
            if (entity instanceof Item item && (havePlaceInInventory(player, item) && item.canPlayerPickup())) {
                addItemInInventory(player, item);
            }
        }
    }

    public boolean havePlaceInInventory(Player player, Item item) {
        boolean emptyEmplacement = Arrays.stream(player.getInventory().getStorageContents())
                .anyMatch(i -> (i == null || i.getType().equals(Material.AIR)));
        boolean sameItemStack = Arrays.stream(player.getInventory().getStorageContents())
                .filter(i -> !(i == null || i.getType().equals(Material.AIR)))
                .anyMatch(i -> i.isSimilar(item.getItemStack())
                        && (i.getAmount() + item.getItemStack().getAmount() <= item.getItemStack().getMaxStackSize()));

        return emptyEmplacement || sameItemStack;
    }

    public void addItemInInventory(Player player, Item item) {
        boolean emptyEmplacement = Arrays.stream(player.getInventory().getStorageContents())
                .anyMatch(i -> (i == null || i.getType().equals(Material.AIR)));

        if (emptyEmplacement) {
            player.getInventory().addItem(item.getItemStack());
            item.remove();
            return;
        }

        for (ItemStack itemStack : player.getInventory().getStorageContents()) {
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                continue;
            }

            if (itemStack.isSimilar(item.getItemStack())
                    && (itemStack.getAmount() + item.getItemStack().getAmount() <= item.getItemStack().getMaxStackSize())) {
                itemStack.setAmount(itemStack.getAmount() + item.getItemStack().getAmount());
                item.remove();
                return;
            }
        }
    }
}