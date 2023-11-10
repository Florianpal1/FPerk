package fr.florianpal.fperk.listeners;

import fr.florianpal.fperk.FPerk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {

    private final FPerk plugin;

    public LeaveListener(FPerk plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event) {
        plugin.removeAllPerkActive(event.getPlayer().getUniqueId());
    }
}
