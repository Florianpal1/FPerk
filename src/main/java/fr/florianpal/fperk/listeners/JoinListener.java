package fr.florianpal.fperk.listeners;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.PerkConfig;
import fr.florianpal.fperk.enums.EffectType;
import fr.florianpal.fperk.managers.commandManagers.PlayerPerkCommandManager;
import fr.florianpal.fperk.objects.PlayerPerk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class JoinListener implements Listener {

    private final FPerk plugin;

    private final PlayerPerkCommandManager playerPerkCommandManager;

    private final PerkConfig perkConfig;

    public JoinListener(FPerk plugin) {
        this.plugin = plugin;
        this.playerPerkCommandManager = plugin.getPlayerPerkCommandManager();
        this.perkConfig = plugin.getConfigurationManager().getPerkConfig();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        TaskChain<List<PlayerPerk>> chain = FPerk.newChain();
        chain.asyncFirst(() -> playerPerkCommandManager.getPlayerPerk(event.getPlayer())).sync(playerPerks -> {

            Player player = event.getPlayer();
            var perks = perkConfig.getPerks();
            for(var playerPerk : playerPerks) {
                var perk = perks.get(playerPerk.getPerk());
                if(playerPerk.isEnabled()) {
                    if (perk.isPersistant()) {
                        for(var competence : perk.getCompetences().entrySet()) {
                            switch (competence.getValue().getType()) {
                                case EFFECT -> {
                                    var potionEffectType = PotionEffectType.getByName(competence.getValue().getEffect());
                                    if(potionEffectType != null) {
                                        var potionEffect = potionEffectType.createEffect(Integer.MAX_VALUE, competence.getValue().getLevel());
                                        player.addPotionEffect(potionEffect);
                                    }
                                }
                                case FLY -> {
                                    player.setFlying(true);
                                    player.setAllowFlight(true);
                                }
                            }
                        }
                    } else {

                    }
                } else {
                    for(var competence : perk.getCompetences().entrySet()) {
                        switch (competence.getValue().getType()) {
                            case EFFECT -> {
                                var potionEffectType = PotionEffectType.getByName(competence.getValue().getEffect());
                                if(potionEffectType != null) {
                                    player.removePotionEffect(potionEffectType);
                                }
                            }
                            case FLY -> {
                                player.setFlying(false);
                                player.setAllowFlight(false);
                            }
                        }
                    }

                }
            }

            return false;
        }).execute();

    }
}
