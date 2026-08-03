package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillContext;
import fr.florianpal.fperk.api.SkillHandler;
import fr.florianpal.fperk.utils.EffectUtils;

/**
 * Raises the flight speed to the {@code level} of the skill. Needs {@link FlySkill} to be useful.
 */
public class FlySpeedSkill extends SkillHandler {

    public FlySpeedSkill() {
        super(BuiltinSkills.FLY_SPEED);
    }

    @Override
    public void onEnable(SkillContext context) {
        context.getPlayer().setFlySpeed(context.getLevel());
    }

    @Override
    public void onDisable(SkillContext context) {
        EffectUtils.resetFlySpeed(context.getPlayer());
    }
}