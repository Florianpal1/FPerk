package fr.florianpal.fperk.utils;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.enums.EffectType;
import fr.florianpal.fperk.objects.Competence;
import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.objects.PlayerPerk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.swing.plaf.SplitPaneUI;

import java.util.List;
import java.util.Optional;

import static fr.florianpal.fperk.enums.EffectType.FLY;

public class EffectUtils {

    public static void removeAllNegativeEffect(Player player) {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.CONFUSION);
        player.removePotionEffect(PotionEffectType.HARM);
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.WITHER);
    }

    public static void enabledFly(Player player, boolean status) {
        player.setAllowFlight(status);
        player.setFlying(status);
    }

    public static void resetFlySpeed(Player player) {
        player.setFlySpeed(0.1F);
    }

    public static void disabledPerk(FPerk plugin, Player player, Perk perk) {
        for (var competence : perk.getCompetences().entrySet()) {
            switch (competence.getValue().getType()) {
                case EFFECT -> {
                    var potionEffectType = PotionEffectType.getByName(competence.getValue().getEffect());
                    if (potionEffectType != null) {
                        player.removePotionEffect(potionEffectType);
                    }
                }
                case FLY -> EffectUtils.enabledFly(player, false);
                case FLY_SPEED -> resetFlySpeed(player);
            }
            plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());
        }
    }

    public static void enabledPerk(FPerk plugin, Player player, PlayerPerk playerPerk, Perk perk) {
        for (var competence : perk.getCompetences().entrySet()) {
            switch (competence.getValue().getType()) {
                case EFFECT -> {

                    var potionEffectType = PotionEffectType.getByName(competence.getValue().getEffect());
                    if (potionEffectType != null) {
                        player.addPotionEffect(new PotionEffect(potionEffectType, -1, (int) competence.getValue().getLevel(), false, false));
                    }
                    if (!perk.isPersistant()) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {

                            playerPerk.setEnabled(false);
                            if (potionEffectType != null) {
                                player.removePotionEffect(potionEffectType);
                            }

                            plugin.getPlayerPerkCommandManager().updatePlayerPerk(playerPerk);
                        }, perk.getTime() * 20L);
                    }

                }
                case FLY -> {
                    EffectUtils.enabledFly(player, true);
                    plugin.addPerkActive(player.getUniqueId(), FLY);
                    if (!perk.isPersistant()) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            EffectUtils.enabledFly(player, false);

                            plugin.removePerkActive(player.getUniqueId(), FLY);
                            playerPerk.setEnabled(false);

                            plugin.getPlayerPerkCommandManager().updatePlayerPerk(playerPerk);
                        }, perk.getTime() * 20L);
                    }
                }
                case FLY_SPEED -> {
                    player.setFlySpeed(competence.getValue().getLevel());
                    plugin.addPerkActive(player.getUniqueId(), competence.getValue().getType());

                    if (!perk.isPersistant()) {

                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            resetFlySpeed(player);

                            plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());
                            playerPerk.setEnabled(false);
                            plugin.getPlayerPerkCommandManager().updatePlayerPerk(playerPerk);
                        }, perk.getTime() * 20L);
                    }
                }
                case CURE_EFFECT -> {
                    plugin.addPerkActive(player.getUniqueId(), EffectType.CURE_EFFECT);
                    EffectUtils.removeAllNegativeEffect(player);
                }
                default -> {

                    plugin.addPerkActive(player.getUniqueId(), competence.getValue().getType());
                    if (!perk.isPersistant()) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            plugin.removePerkActive(player.getUniqueId(), competence.getValue().getType());

                            playerPerk.setEnabled(false);
                            plugin.getPlayerPerkCommandManager().updatePlayerPerk(playerPerk);
                        }, perk.getTime() * 20L);
                    }
                }
            }
        }
    }

    public static void checkPerk(FPerk plugin, Player player) {
        for(var perkActive : plugin.getAllPerkActive().entrySet()) {
            if(perkActive.getValue().contains(player.getUniqueId())) {
                Perk perk = getPerkWithCompetence(plugin, perkActive.getKey());
                switch (perkActive.getKey()) {
                    case FLY -> {
                        if (perk.isPersistant()) {
                            EffectUtils.enabledFly(player, true);
                            plugin.addPerkActive(player.getUniqueId(), FLY);
                        }
                    }
                }
            }
        }
    }

    public static Perk getPerkWithCompetence(FPerk plugin, EffectType effectType) {
        List<Perk> perks = plugin.getConfigurationManager().getPerkConfig().getPerks().values().stream().toList();
        for (Perk perk : perks) {
            Optional<Competence> competence = perk.getCompetences().values().stream().filter(c -> c.getType().equals(effectType)).findFirst();
            if (competence.isPresent()) {
                return perk;
            }
        }
        return null;
    }
}
