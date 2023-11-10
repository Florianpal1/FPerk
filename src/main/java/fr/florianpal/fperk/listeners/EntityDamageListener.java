package fr.florianpal.fperk.listeners;

import fr.florianpal.fperk.FPerk;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import static fr.florianpal.fperk.enums.EffectType.ANTI_KNOCKBACK;

public class EntityDamageListener implements Listener {

    private final FPerk plugin;

    public EntityDamageListener(FPerk plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        if(event.getEntity().getType().equals(EntityType.PLAYER)) {
            Player player = (Player) event.getEntity();
            if(plugin.isPerkActive(player.getUniqueId(), ANTI_KNOCKBACK)) {
                player.setVelocity(new Vector());
                Bukkit.getScheduler().runTaskLater(plugin, () -> player.setVelocity(new Vector()), 1L);
            }
        }
    }
}
