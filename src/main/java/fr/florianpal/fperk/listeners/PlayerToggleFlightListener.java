package fr.florianpal.fperk.listeners;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.utils.EffectUtils;
import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();
        EffectUtils.enabledFly(player, plugin.isPerkActive(event.getPlayer().getUniqueId(), FLY));
    }

}
