package fr.florianpal.fperk.listeners;

import fr.florianpal.fperk.FPerk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import static fr.florianpal.fperk.enums.EffectType.FLY;

public class PlayerToggleFlightListener implements Listener {

    private final FPerk plugin;

    public PlayerToggleFlightListener(FPerk plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerToggleFlightEvent event) {
        if(plugin.isPerkActive(event.getPlayer().getUniqueId(), FLY)) {
            event.getPlayer().setAllowFlight(true);
            event.getPlayer().setFlying(true);
            plugin.addPerkActive(event.getPlayer().getUniqueId(), FLY);
        }
    }

}
