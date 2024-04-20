package fr.florianpal.fperk.listeners;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.utils.EffectUtils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static fr.florianpal.fperk.enums.EffectType.ASPIRATOR;
import static fr.florianpal.fperk.enums.EffectType.CURE_EFFECT;

public class PlayerMoveListener implements Listener {
    private final FPerk plugin;

    public PlayerMoveListener(FPerk plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        if (plugin.isPerkActive(player.getUniqueId(), ASPIRATOR)) {
            List<Entity> entities = event.getTo().getNearbyEntities(3, 3, 3).stream().toList();
            for (Entity entity : entities) {
                if (entity instanceof Item item && (havePlaceInInventory(player, item) && item.canPlayerPickup())) {
                    addItemInInventory(player, item);
                }
            }
        }

        if (plugin.isPerkActive(player.getUniqueId(), CURE_EFFECT)) {
            EffectUtils.removeAllNegativeEffect(player);
        }
    }

    public boolean havePlaceInInventory(Player player, Item item) {
        boolean emptyEmplacement = Arrays.stream(player.getInventory().getStorageContents()).anyMatch(i -> (i == null || i.getType().equals(Material.AIR)));
        boolean sameItemStack = Arrays.stream(player.getInventory().getStorageContents()).filter(i -> !(i == null || i.getType().equals(Material.AIR))).anyMatch(i -> i.isSimilar(item.getItemStack()) && (i.getAmount() + item.getItemStack().getAmount() <= item.getItemStack().getMaxStackSize()));

        return emptyEmplacement || sameItemStack;
    }


    public void addItemInInventory(Player player, Item item) {
        boolean emptyEmplacement = Arrays.stream(player.getInventory().getStorageContents()).anyMatch(i -> (i == null || i.getType().equals(Material.AIR)));
        if (emptyEmplacement) {
            player.getPlayer().getInventory().addItem(item.getItemStack());
            item.remove();
        } else {

            for (var itemStack : player.getInventory().getStorageContents()) {
                if((itemStack != null && !itemStack.getType().equals(Material.AIR))) {
                    if(itemStack.isSimilar(item.getItemStack()) && (itemStack.getAmount() + item.getItemStack().getAmount() <= item.getItemStack().getMaxStackSize())) {
                        itemStack.setAmount(itemStack.getAmount() + item.getItemStack().getAmount());
                        item.remove();
                        return;
                    }
                }

            }
        }
    }


}
