package fr.florianpal.fperk.scheduler;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.PerkConfig;
import fr.florianpal.fperk.managers.VaultIntegrationManager;
import fr.florianpal.fperk.objects.PlayerPerk;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LoadDataScheduler implements Runnable {

    private final FPerk plugin;

    private final PerkConfig perkConfig;

    public LoadDataScheduler(FPerk plugin) {
        this.plugin = plugin;
        this.perkConfig = plugin.getConfigurationManager().getPerkConfig();
    }

    @Override
    public void run() {
        TaskChain<Void> chain = FPerk.newChain();
        chain.asyncFirst(() -> {
            Map<UUID, List<PlayerPerk>> allPlayerPerk = plugin.getPlayerPerkCommandManager().getAllPlayerPerk();
            for (var playerPerks : allPlayerPerk.entrySet()) {
                for (var playerPerk : playerPerks.getValue()) {
                    var perk = perkConfig.getPerks().get(playerPerk.getPerk());
                    if (playerPerk.isEnabled()) {

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
            return null;
        }).execute();

    }
}

