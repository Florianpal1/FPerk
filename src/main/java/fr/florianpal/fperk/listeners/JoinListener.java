package fr.florianpal.fperk.listeners;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.PerkConfig;
import fr.florianpal.fperk.managers.commandManagers.PlayerPerkCommandManager;
import fr.florianpal.fperk.objects.PlayerPerk;
import fr.florianpal.fperk.utils.EffectUtils;
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

import static fr.florianpal.fperk.utils.EffectUtils.checkPerk;

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

        checkPerk(plugin, event.getPlayer());

        TaskChain<List<PlayerPerk>> chain = FPerk.newChain();
        chain.asyncFirst(() -> playerPerkCommandManager.getPlayerPerk(event.getPlayer())).sync(playerPerks -> {

            Player player = event.getPlayer();
            var perks = perkConfig.getPerks();
            for (var playerPerk : playerPerks) {
                var perk = perks.get(playerPerk.getPerk());

                boolean havePermission = player.hasPermission(perk.getPermission());
                if (havePermission && playerPerk.isEnabled()) {

                    for (var skill : perk.getSkills().entrySet()) {
                        long time = new Date().getTime() - playerPerk.getLastEnabled().getTime();
                        long secs = (time) / 1000;

                        switch (skill.getValue().getType()) {
                            case EFFECT -> {
                                var potionEffectType = PotionEffectType.getByName(skill.getValue().getEffect());
                                if (potionEffectType != null) {
                                    player.addPotionEffect(new PotionEffect(potionEffectType, -1, (int) skill.getValue().getLevel(), false, false));
                                }
                            }
                            case FLY -> {
                                EffectUtils.enabledFly(player, true);
                                plugin.addPerkActive(player.getUniqueId(), skill.getValue().getType());

                                if (!perk.isPersistant()) {
                                    if (secs > 0) {
                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            EffectUtils.enabledFly(player, false);
                                            plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());

                                            playerPerk.setEnabled(false);
                                            playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                        }, secs * 20L);
                                    } else {
                                        EffectUtils.enabledFly(player, false);
                                        plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());
                                        playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                    }
                                }
                            }
                            case FLY_SPEED -> {
                                player.setFlySpeed(skill.getValue().getLevel());
                                plugin.addPerkActive(player.getUniqueId(), skill.getValue().getType());

                                if (!perk.isPersistant()) {
                                    if (secs > 0) {
                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            EffectUtils.resetFlySpeed(player);

                                            plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());

                                            playerPerk.setEnabled(false);
                                            playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                        }, secs * 20L);
                                    } else {
                                        EffectUtils.resetFlySpeed(player);
                                        plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());

                                        playerPerk.setEnabled(false);
                                        playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                    }
                                }
                            }
                            default -> {
                                plugin.addPerkActive(player.getUniqueId(), skill.getValue().getType());
                                if(!perk.isPersistant()) {
                                    if(secs > 0) {
                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());

                                            playerPerk.setEnabled(false);
                                            playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                        }, secs * 20L);
                                    } else {
                                        plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());

                                        playerPerk.setEnabled(false);
                                        playerPerkCommandManager.updatePlayerPerk(playerPerk);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (var skill : perk.getSkills().entrySet()) {
                        switch (skill.getValue().getType()) {
                            case EFFECT -> {
                                var potionEffectType = PotionEffectType.getByName(skill.getValue().getEffect());
                                if (potionEffectType != null) {
                                    player.removePotionEffect(potionEffectType);
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> player.removePotionEffect(potionEffectType), 80L);
                                }
                            }
                            case FLY -> {
                                EffectUtils.enabledFly(player, false);
                                plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());
                            }
                            case FLY_SPEED -> {
                                player.setFlySpeed(0.1F);
                                plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());
                            }
                            default -> plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());
                        }
                    }

                }
            }

            return false;
        }).execute();
    }
}
