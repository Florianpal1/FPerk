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
                if (entity instanceof Item item && (havePlaceInInventory(player) && item.canPlayerPickup())) {
                    player.getPlayer().getInventory().addItem(item.getItemStack());
                    item.remove();
                }
            }
        }

        if (plugin.isPerkActive(player.getUniqueId(), CURE_EFFECT)) {
            EffectUtils.removeAllNegativeEffect(player);
        }
    }

    public boolean havePlaceInInventory(Player player) {
        return Arrays.stream(player.getInventory().getStorageContents()).anyMatch(i -> i == null || i.getType().equals(Material.AIR));
    }
}
