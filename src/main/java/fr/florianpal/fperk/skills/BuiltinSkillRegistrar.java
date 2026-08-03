package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.api.SkillRegistry;

/**
 * Registers the skills shipped with FPerk. They go through the very same
 * {@link SkillRegistry#register} an addon uses, so anything a builtin skill can do, an addon can do
 * too.
 */
public final class BuiltinSkillRegistrar {

    private BuiltinSkillRegistrar() {
    }

    public static void registerAll(FPerk plugin) {
        SkillRegistry registry = plugin.getSkillRegistry();

        registry.register(plugin, new FlySkill());
        registry.register(plugin, new FlySpeedSkill());
        registry.register(plugin, new EffectSkill());
        registry.register(plugin, new KeepInventorySkill());
        registry.register(plugin, new KeepExperienceSkill());
        registry.register(plugin, new PacificationSkill());
        registry.register(plugin, new AntiPhantomSkill());
        registry.register(plugin, new HarvestSkill());
        registry.register(plugin, new AutoSmeltSkill());
        registry.register(plugin, new AntiKnockbackSkill());
        registry.register(plugin, new CureEffectSkill());
        registry.register(plugin, new SecondChanceSkill());
        registry.register(plugin, new BrokenFallSkill());
        registry.register(plugin, new VacuumSkill());
    }
}