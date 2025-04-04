package fr.florianpal.fperk.utils;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.enums.EffectType;
import fr.florianpal.fperk.objects.Skill;
import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.objects.PlayerPerk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Optional;

import static fr.florianpal.fperk.enums.EffectType.FLY;

public class EffectUtils {

    public static void removeAllNegativeEffect(Player player) {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.INSTANT_DAMAGE);
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
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
        for (var skill : perk.getSkills().entrySet()) {
            switch (skill.getValue().getType()) {
                case EFFECT -> {
                    var potionEffectType = PotionEffectType.getByName(skill.getValue().getEffect());
                    if (potionEffectType != null) {
                        player.removePotionEffect(potionEffectType);
                    }
                }
                case FLY -> EffectUtils.enabledFly(player, false);
                case FLY_SPEED -> resetFlySpeed(player);
            }
            plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());
        }
    }

    public static void enabledPerk(FPerk plugin, Player player, PlayerPerk playerPerk, Perk perk) {
        for (var skill : perk.getSkills().entrySet()) {
            switch (skill.getValue().getType()) {
                case EFFECT -> {

                    var potionEffectType = PotionEffectType.getByName(skill.getValue().getEffect());
                    if (potionEffectType != null) {
                        player.addPotionEffect(new PotionEffect(potionEffectType, -1, (int) skill.getValue().getLevel(), false, false));
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
                    player.setFlySpeed(skill.getValue().getLevel());
                    plugin.addPerkActive(player.getUniqueId(), skill.getValue().getType());

                    if (!perk.isPersistant()) {

                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            resetFlySpeed(player);

                            plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());
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

                    plugin.addPerkActive(player.getUniqueId(), skill.getValue().getType());
                    if (!perk.isPersistant()) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            plugin.removePerkActive(player.getUniqueId(), skill.getValue().getType());

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
                Perk perk = getPerkWithSkill(plugin, perkActive.getKey());
                switch (perkActive.getKey()) {
                    case FLY -> {
                        if (perk.isPersistant() && player.hasPermission(perk.getPermission())) {
                            EffectUtils.enabledFly(player, true);
                            plugin.addPerkActive(player.getUniqueId(), FLY);
                        } else if (!player.hasPermission(perk.getPermission())) {
                            EffectUtils.enabledFly(player, false);
                            plugin.removePerkActive(player.getUniqueId(), FLY);
                        }
                    }
                    case EFFECT -> {
                        for(Skill skill : perk.getSkills().values()) {
                            var potionEffectType = PotionEffectType.getByName(skill.getEffect());
                            if (potionEffectType != null) {
                                player.addPotionEffect(new PotionEffect(potionEffectType, -1, (int) skill.getLevel(), false, false));
                            }
                        }
                    }
                    case FLY_SPEED -> {
                        for(Skill skill : perk.getSkills().values()) {
                            player.setFlySpeed(skill.getLevel());
                        }
                    }
                }
            }
        }
    }

    public static Perk getPerkWithSkill(FPerk plugin, EffectType effectType) {
        List<Perk> perks = plugin.getConfigurationManager().getPerkConfig().getPerks().values().stream().toList();
        for (Perk perk : perks) {
            Optional<Skill> skill = perk.getSkills().values().stream().filter(c -> c.getType().equals(effectType)).findFirst();
            if (skill.isPresent()) {
                return perk;
            }
        }
        return null;
    }
}
