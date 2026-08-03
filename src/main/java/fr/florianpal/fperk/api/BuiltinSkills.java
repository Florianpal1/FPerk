package fr.florianpal.fperk.api;

/**
 * Identifiers of the skills shipped with FPerk, as written in the {@code type} field of
 * {@code skill.yml}.
 *
 * <p>Addons never need these constants to declare their own skills, but they are handy to query the
 * state of a builtin one, for instance
 * {@code registry.isActive(uuid, BuiltinSkills.FLY)}.</p>
 */
public final class BuiltinSkills {

    public static final String FLY = "FLY";

    public static final String FLY_SPEED = "FLY_SPEED";

    public static final String EFFECT = "EFFECT";

    public static final String KEEP_INVENTORY = "KEEP_INVENTORY";

    public static final String KEEP_EXPERIENCE = "KEEP_EXPERIENCE";

    public static final String PACIFICATION = "PACIFICATION";

    public static final String ANTI_PHANTOM = "ANTI_PHANTOM";

    public static final String HARVEST = "HARVEST";

    public static final String AUTO_SMELT = "AUTO_SMELT";

    public static final String ANTI_KNOCKBACK = "ANTI_KNOCKBACK";

    public static final String CURE_EFFECT = "CURE_EFFECT";

    public static final String SECOND_CHANCE = "SECOND_CHANCE";

    public static final String BROKEN_FALL = "BROKEN_FALL";

    /**
     * Historical spelling of the vacuum skill. Kept as-is so existing {@code skill.yml} files
     * keep working.
     */
    public static final String VACCUM = "VACCUM";

    private BuiltinSkills() {
    }
}