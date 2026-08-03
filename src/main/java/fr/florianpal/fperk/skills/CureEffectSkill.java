package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillContext;
import fr.florianpal.fperk.api.SkillHandler;
import fr.florianpal.fperk.utils.EffectUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Keeps the player free of every negative effect.
 */
public class CureEffectSkill extends SkillHandler implements Listener {

    public CureEffectSkill() {
        super(BuiltinSkills.CURE_EFFECT);
    }

    @Override
    public void onEnable(SkillContext context) {
        EffectUtils.removeAllNegativeEffect(context.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isActive(player)) {
            EffectUtils.removeAllNegativeEffect(player);
        }
    }
}