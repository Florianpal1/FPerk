package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillContext;
import fr.florianpal.fperk.api.SkillHandler;
import fr.florianpal.fperk.utils.EffectUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * Lets the player fly.
 */
public class FlySkill extends SkillHandler implements Listener {

    public FlySkill() {
        super(BuiltinSkills.FLY);
    }

    @Override
    public void onEnable(SkillContext context) {
        EffectUtils.enabledFly(context.getPlayer(), true);
    }

    @Override
    public void onDisable(SkillContext context) {
        EffectUtils.enabledFly(context.getPlayer(), false);
    }

    /**
     * Changing world resets the flight state, so it has to be applied again.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        EffectUtils.enabledFly(player, isActive(player));
    }
}