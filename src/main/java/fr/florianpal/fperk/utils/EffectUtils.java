package fr.florianpal.fperk.utils;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * Small helpers shared by several skills. The lifecycle of a perk itself lives in
 * {@code SkillService}, and what each skill does lives in its own {@code SkillHandler}.
 */
public class EffectUtils {

    private static final float DEFAULT_FLY_SPEED = 0.1F;

    private EffectUtils() {
    }

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
        player.setFlySpeed(DEFAULT_FLY_SPEED);
    }
}
