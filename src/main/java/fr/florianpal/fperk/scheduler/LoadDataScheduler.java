package fr.florianpal.fperk.scheduler;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.PerkConfig;
import fr.florianpal.fperk.managers.VaultIntegrationManager;
import fr.florianpal.fperk.objects.PlayerPerk;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LoadDataScheduler implements Runnable {

    private final FPerk plugin;

    private final PerkConfig perkConfig;
    
    private final VaultIntegrationManager vaultIntegrationManager;

    public LoadDataScheduler(FPerk plugin) {
        this.plugin = plugin;
        this.perkConfig = plugin.getConfigurationManager().getPerkConfig();
        this.vaultIntegrationManager = plugin.getVaultIntegrationManager();
    }

    @Override
    public void run() {
        TaskChain<Void> chain = FPerk.newChain();
        chain.asyncFirst(() -> {
            Map<UUID, List<PlayerPerk>> allPlayerPerk = plugin.getPlayerPerkCommandManager().getAllPlayerPerk();
            for (var playerPerks : allPlayerPerk.entrySet()) {
                for (var playerPerk : playerPerks.getValue()) {
                    var perk = perkConfig.getPerks().get(playerPerk.getPerk());

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerPerk.getPlayerUUID());
                    Player player = null;
                    if(offlinePlayer.isOnline()) {
                        player = offlinePlayer.getPlayer();
                    }

                    User user = plugin.getLuckPerms().getUserManager().getUser(playerPerk.getPlayerUUID());
                    if(user != null && user.getCachedData() != null) {
                        boolean havePermission = plugin.getLuckPerms().getUserManager().getUser(playerPerk.getPlayerUUID()).getCachedData().getPermissionData().checkPermission(perk.getPermission()).asBoolean();
                        if ((player == null || havePermission && playerPerk.isEnabled())) {

                            for (var competence : perk.getCompetences().entrySet()) {
                                switch (competence.getValue().getType()) {
                                    case FLY -> plugin.addPerkActive(playerPerk.getPlayerUUID(), competence.getValue().getType());
                                }
                            }
                        } else {
                            for (var competence : perk.getCompetences().entrySet()) {
                                switch (competence.getValue().getType()) {
                                    case FLY -> plugin.removePerkActive(playerPerk.getPlayerUUID(), competence.getValue().getType());
                                }
                            }

                        }
                    }
                }
            }
            return null;
        }).execute();

    }
}

