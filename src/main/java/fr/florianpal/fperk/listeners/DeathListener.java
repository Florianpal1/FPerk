package fr.florianpal.fperk.listeners;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.enums.EffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class DeathListener implements Listener {

    private final FPerk plugin;

    public DeathListener(FPerk plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();
        if(plugin.isPerkActive(uuid, EffectType.KEEP_INVENTORY)) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }

        if(plugin.isPerkActive(uuid, EffectType.KEEP_EXPERIENCE)) {
            event.setKeepLevel(true);
            event.setShouldDropExperience(false);
        }
    }

}
