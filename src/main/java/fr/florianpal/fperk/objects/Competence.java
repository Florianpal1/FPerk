package fr.florianpal.fperk.objects;

import fr.florianpal.fperk.enums.EffectType;

import java.util.List;

public class Competence {

    private final String id;

    private final List<String> displayName;

    private final EffectType type;

    private final String effect;

    private final float level;

    public Competence(String id, List<String> displayName, EffectType type, String effect, float level) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.effect = effect;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public List<String> getDisplayName() {
        return displayName;
    }

    public EffectType getType() {
        return type;
    }

    public String getEffect() {
        return effect;
    }

    public float getLevel() {
        return level;
    }
}
