package fr.florianpal.fperk.objects.gui;

import org.bukkit.Material;

public class BarrierMaterial {

    private final int index;

    private Material material;

    public BarrierMaterial(int index) {
        this.index = index;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getIndex() {
        return index;
    }
}
