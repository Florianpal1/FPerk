package fr.florianpal.fperk.configurations;

import fr.florianpal.fperk.api.BuiltinSkills;

import java.util.Set;

/**
 * The skill types FPerk registers itself. Kept next to the configuration tests so that the shipped
 * {@code skill.yml} can be checked against what the plugin actually provides.
 */
final class DefaultResources {

    static final Set<String> BUILTIN_TYPES = Set.of(
            BuiltinSkills.FLY,
            BuiltinSkills.FLY_SPEED,
            BuiltinSkills.EFFECT,
            BuiltinSkills.KEEP_INVENTORY,
            BuiltinSkills.KEEP_EXPERIENCE,
            BuiltinSkills.PACIFICATION,
            BuiltinSkills.ANTI_PHANTOM,
            BuiltinSkills.HARVEST,
            BuiltinSkills.AUTO_SMELT,
            BuiltinSkills.ANTI_KNOCKBACK,
            BuiltinSkills.CURE_EFFECT,
            BuiltinSkills.SECOND_CHANCE,
            BuiltinSkills.BROKEN_FALL,
            BuiltinSkills.VACCUM);

    private DefaultResources() {
    }
}
