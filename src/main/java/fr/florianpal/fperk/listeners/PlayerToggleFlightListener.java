package fr.florianpal.fperk.listeners;

import fr.florianpal.fperk.FPerk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class PlayerToggleFlightListener implements Listener {

    private final FPerk plugin;

    public PlayerToggleFlightListener(FPerk plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        //Player player = event.getPlayer();
        //EffectUtils.enabledFly(player, plugin.isPerkActive(event.getPlayer().getUniqueId(), FLY));
    }

}
