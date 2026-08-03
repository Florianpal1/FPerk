package fr.florianpal.fperk.listeners;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.managers.commandManagers.PlayerPerkCommandManager;
import fr.florianpal.fperk.objects.PlayerPerk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class JoinListener implements Listener {

    private final FPerk plugin;

    private final PlayerPerkCommandManager playerPerkCommandManager;

    public JoinListener(FPerk plugin) {
        this.plugin = plugin;
        this.playerPerkCommandManager = plugin.getPlayerPerkCommandManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        TaskChain<List<PlayerPerk>> chain = FPerk.newChain();
        chain.asyncFirst(() -> playerPerkCommandManager.getPlayerPerk(event.getPlayer()))
                .syncLast(playerPerks -> plugin.getSkillService().syncPlayer(event.getPlayer(), playerPerks))
                .execute();
    }
}