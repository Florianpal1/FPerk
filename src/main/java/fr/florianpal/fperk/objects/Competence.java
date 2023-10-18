package fr.florianpal.fperk.objects;

import fr.florianpal.fperk.enums.EffectType;

public class Competence {

    private final String id;

    private final String displayName;

    private final EffectType type;

    private final String effect;

    private final int level;

    public Competence(String id, String displayName, EffectType type, String effect, int level) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.effect = effect;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EffectType getType() {
        return type;
    }

    public String getEffect() {
        return effect;
    }

    public int getLevel() {
        return level;
    }
}
