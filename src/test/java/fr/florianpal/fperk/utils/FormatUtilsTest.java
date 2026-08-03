package fr.florianpal.fperk.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FormatUtilsTest {

    @Test
    @DisplayName("legacy & codes are translated")
    void translatesLegacyCodes() {
        assertEquals(ChatColor.GREEN + "Enabled", FormatUtils.format("&aEnabled"));
    }

    @Test
    @DisplayName("{#RRGGBB} hex colours are translated")
    void translatesHexColours() {
        assertEquals(ChatColor.of("#ff8800") + "Orange", FormatUtils.format("{#ff8800}Orange"));
    }

    @Test
    @DisplayName("several hex colours in one line are all translated")
    void translatesEveryHexColour() {
        String formatted = FormatUtils.format("{#ff0000}red{#00ff00}green");

        assertEquals(ChatColor.of("#ff0000") + "red" + ChatColor.of("#00ff00") + "green", formatted);
        assertFalse(formatted.contains("{#"));
    }

    @Test
    @DisplayName("hex is case insensitive and mixes with legacy codes")
    void mixesNotations() {
        String formatted = FormatUtils.format("{#AABBCC}hex &lbold");

        assertEquals(ChatColor.of("#AABBCC") + "hex " + ChatColor.BOLD + "bold", formatted);
    }

    @Test
    @DisplayName("a malformed hex colour is left alone rather than throwing")
    void leavesMalformedHexAlone() {
        assertEquals("{#ZZZZZZ}text", FormatUtils.format("{#ZZZZZZ}text"));
        assertEquals("{#fff}text", FormatUtils.format("{#fff}text"));
    }

    @Test
    void leavesPlainTextUntouched() {
        assertEquals("plain text", FormatUtils.format("plain text"));
        assertEquals("", FormatUtils.format(""));
    }

    @Test
    void humanizesMaterialNames() {
        assertEquals("Diamond Pickaxe", FormatUtils.humanize(Material.DIAMOND_PICKAXE));
        assertEquals("Elytra", FormatUtils.humanize(Material.ELYTRA));
    }

    @Test
    void formatsBooleans() {
        assertEquals("Oui", FormatUtils.format(true));
        assertEquals("Non", FormatUtils.format(false));
    }
}
