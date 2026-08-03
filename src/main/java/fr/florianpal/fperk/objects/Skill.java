package fr.florianpal.fperk.objects;

import java.util.List;
import java.util.Locale;

public class Skill {

    private final String id;

    private final List<String> displayName;

    /**
     * The skill type, as written in the {@code type} field of {@code skill.yml}. It points at a
     * {@code SkillHandler} registered by FPerk or by an addon.
     */
    private final String type;

    private final String effect;

    private final float level;

    public Skill(String id, List<String> displayName, String type, String effect, float level) {
        this.id = id;
        this.displayName = displayName;
        this.type = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
        this.effect = effect;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public List<String> getDisplayName() {
        return displayName;
    }

    public String getType() {
        return type;
    }

    public String getEffect() {
        return effect;
    }

    public float getLevel() {
        return level;
    }
}