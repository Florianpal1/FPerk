package fr.florianpal.fperk.objects.gui;

import fr.florianpal.fperk.enums.ActionType;
import org.bukkit.Material;

import java.util.List;

public class Action {

    private final int index;
    private final Material material;
    private final String title;
    private final List<String> description;

    private final String texture;

    private final ActionType type;

    private Action remplacement;

    public Action(int index, Material material, String title, List<String> description, String texture, ActionType type, Action remplacement) {
        this.index = index;
        this.material = material;
        this.title = title;
        this.description = description;
        this.texture = texture;
        this.type = type;
        this.remplacement = remplacement;
    }

    public Action(int index, Material material, String title, List<String> description, String texture, ActionType type) {
        this.index = index;
        this.material = material;
        this.title = title;
        this.description = description;
        this.texture = texture;
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public Material getMaterial() {
        return material;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getDescription() {
        return description;
    }

    public Action getRemplacement() {
        return remplacement;
    }

    public ActionType getType() {
        return type;
    }

    public String getTexture() {
        return texture;
    }
}
