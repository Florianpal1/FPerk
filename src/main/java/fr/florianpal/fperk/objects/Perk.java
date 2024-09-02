package fr.florianpal.fperk.objects;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;
import java.util.UUID;

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

    private final String permissionBypass;

    private final String texture;

    public Perk(String id, String displayName, Material material, Map<String, Competence> competences, int delais, boolean ignoreDelais, int time, boolean persistant, String permission, String permissionBypass, String texture) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.competences = competences;
        this.delais = delais;
        this.ignoreDelais = ignoreDelais;
        this.time = time;
        this.persistant = persistant;
        this.permission = permission;
        this.permissionBypass = permissionBypass;
        this.texture = texture;
    }

    public ItemStack getItemStack() {
        ItemStack itemStack;
        if (material == Material.PLAYER_HEAD) {
            itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", texture));
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setPlayerProfile(profile);

            itemStack.setItemMeta(skullMeta);
            itemStack.setAmount(1);
        } else {
            itemStack = new ItemStack(material, 1);
        }
        return itemStack;
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

    public String getPermissionBypass() {
        return permissionBypass;
    }
}
