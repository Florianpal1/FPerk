package fr.florianpal.fperk.listeners;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.PerkConfig;
import fr.florianpal.fperk.managers.commandManagers.PlayerPerkCommandManager;
import fr.florianpal.fperk.objects.PlayerPerk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Date;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        TaskChain<List<PlayerPerk>> chain = FPerk.newChain();
        chain.asyncFirst(() -> playerPerkCommandManager.getPlayerPerk(event.getPlayer())).sync(playerPerks -> {

            Player player = event.getPlayer();
            var perks = perkConfig.getPerks();
            for (var playerPerk : playerPerks) {
                var perk = perks.get(playerPerk.getPerk());
                if (playerPerk.isEnabled() && player.hasPermission(perk.getPermission())) {

                    for (var competence : perk.getCompetences().entrySet()) {
                        long time = new Date().getTime() - playerPerk.getLastEnabled().getTime();
                        long secs = (time) / 1000;

                        switch (competence.getValue().getType()) {
                            case EFFECT -> {
                                var potionEffectType = PotionEffectType.getByName(competence.getValue().getEffect());
                                if (potionEffectType != null) {
                                    player.addPotionEffect(new PotionEffect(potionEffectType, -1, (int) competence.getValue().getLevel(), false, false));
                                }
                            }
                            case FLY -> {
                                player.setAllowFlight(true);
                                player.setFlying(true);
                                plugin.addPerkActive(player.getUniqueId(), competence.getValue().getType());

                                if (!perk.isPersistant()) {
                                    if (secs > 0) {
                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            player.setAllowFlight(false);
                                            player.setFlying(false);

                                            plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());
                                            playerPerk.setEnabled(false);

                                            playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                        }, secs * 20L);
                                    } else {
                                        player.setAllowFlight(false);
                                        player.setFlying(false);
                                        plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());
                                        playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                    }
                                }
                            }
                            case FLY_SPEED -> {
                                player.setFlySpeed(competence.getValue().getLevel());
                                plugin.addPerkActive(player.getUniqueId(), competence.getValue().getType());

                                if (!perk.isPersistant()) {
                                    if (secs > 0) {
                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            player.setFlySpeed(1);

                                            plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());
                                            playerPerk.setEnabled(false);

                                            playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                        }, secs * 20L);
                                    } else {
                                        player.setFlySpeed(0.1F);
                                        plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());
                                        playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                    }
                                }
                            }
                            default -> {
                                plugin.addPerkActive(player.getUniqueId(), competence.getValue().getType());
                                if(!perk.isPersistant()) {
                                    if(secs > 0) {
                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());
                                            playerPerk.setEnabled(false);
                                            playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                        }, secs * 20L);
                                    } else {
                                        plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());
                                        playerPerk.setEnabled(false);
                                        playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (var competence : perk.getCompetences().entrySet()) {
                        switch (competence.getValue().getType()) {
                            case EFFECT -> {
                                var potionEffectType = PotionEffectType.getByName(competence.getValue().getEffect());
                                if (potionEffectType != null) {
                                    player.removePotionEffect(potionEffectType);
                                }
                            }
                            case FLY -> {
                                player.setAllowFlight(false);
                                player.setFlying(false);
                                plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());
                            }
                            case FLY_SPEED -> {
                                player.setFlySpeed(0.1F);
                                plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());
                            }
                            default -> plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());
                        }
                    }

                }
            }

            return false;
        }).execute();
    }
}
