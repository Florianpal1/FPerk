package fr.florianpal.fperk.placeholders;

import fr.florianpal.fperk.FPerk;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FPlaceholderExpansion extends PlaceholderExpansion {

    private final FPerk plugin;

    public FPlaceholderExpansion(FPerk plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "fperk";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Florianpal";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        return null;
    }

}
