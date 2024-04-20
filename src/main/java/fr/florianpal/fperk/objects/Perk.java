package fr.florianpal.fperk.objects;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Map;

import static java.util.UUID.randomUUID;

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
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

            GameProfile gameProfile = new GameProfile(randomUUID(), null);

            Field field = null;
            try {
                field = skullMeta.getClass().getDeclaredField("profile");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            gameProfile.getProperties().put("textures", new Property("textures", texture));

            field.setAccessible(true); // We set as accessible to modify.
            try {
                field.set(skullMeta, gameProfile);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

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
