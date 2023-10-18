package fr.florianpal.fperk.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class Perk {

    private final String id;

    private final String displayName;

    private final Material material;

    private final Map<String, Competence> competences;

    private final int delais;

    private final boolean ignoreDelais;

    private final int time;

    private final boolean persistant;

    private final String permission;

    public Perk(String id, String displayName, Material material, Map<String, Competence> competences, int delais, boolean ignoreDelais, int time, boolean persistant, String permission) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.competences = competences;
        this.delais = delais;
        this.ignoreDelais = ignoreDelais;
        this.time = time;
        this.persistant = persistant;
        this.permission = permission;

    }

    public ItemStack getItemStack() {
        return new ItemStack(material);
    }

    public String getId() {
        return id;
    }

    public Map<String, Competence> getCompetences() {
        return competences;
    }

    public int getDelais() {
        return delais;
    }

    public String getPermission() {
        return permission;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getTime() {
        return time;
    }

    public boolean isPersistant() {
        return persistant;
    }

    public boolean isIgnoreDelais() {
        return ignoreDelais;
    }
}
