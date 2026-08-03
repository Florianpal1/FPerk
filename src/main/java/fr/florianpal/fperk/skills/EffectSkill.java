package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillContext;
import fr.florianpal.fperk.api.SkillHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies the potion effect named in the {@code effect} field, at the {@code level} of the skill.
 */
public class EffectSkill extends SkillHandler {

    public EffectSkill() {
        super(BuiltinSkills.EFFECT);
    }

    @Override
    public void onEnable(SkillContext context) {
        PotionEffectType type = resolve(context);
        if (type != null) {
            context.getPlayer().addPotionEffect(new PotionEffect(type, -1, (int) context.getLevel(), false, false));
        }
    }

    @Override
    public void onDisable(SkillContext context) {
        PotionEffectType type = resolve(context);
        if (type != null) {
            context.getPlayer().removePotionEffect(type);
        }
    }

    private PotionEffectType resolve(SkillContext context) {
        String effect = context.getEffect();
        if (effect == null || effect.trim().isEmpty()) {
            return null;
        }
        return PotionEffectType.getByName(effect);
    }
}