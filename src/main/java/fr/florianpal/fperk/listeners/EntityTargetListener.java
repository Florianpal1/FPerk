package fr.florianpal.fperk.listeners;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.enums.EffectType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.List;

import static org.bukkit.event.entity.EntityTargetEvent.TargetReason.CLOSEST_ENTITY;
import static org.bukkit.event.entity.EntityTargetEvent.TargetReason.CLOSEST_PLAYER;

public class EntityTargetListener {

    private FPerk plugin;

    List<EntityTargetEvent.TargetReason> targetReasons = List.of(CLOSEST_PLAYER, CLOSEST_ENTITY);

    public EntityTargetListener(FPerk plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityTargetEvent(EntityTargetEvent event) {
        Entity entity = event.getTarget();
        if (entity instanceof Player player) {
            if (plugin.isPerkActive(player.getUniqueId(), EffectType.PACIFICATION) && targetReasons.contains(event.getReason())) {
                event.setCancelled(true);
            }
        }
    }
}
