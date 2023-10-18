package fr.florianpal.fperk.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatUtil {

    public static String format(String msg) {
        Pattern pattern = Pattern.compile("[{]#[a-fA-F0-9]{6}[}]");

        Matcher match = pattern.matcher(msg);
        while (match.find()) {
            String color = msg.substring(match.start(), match.end());
            String replace = color;
            color = color.replace("{", "");
            color = color.replace("}", "");
            msg = msg.replace(replace, ChatColor.of(color) + "");
            match = pattern.matcher(msg);
        }

        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static String format(boolean bool) {
        if(bool) {
            return "Oui";
        } else {
            return "Non";
        }
    }

    public static String humanize(Material type) {
        String[] parts = type.toString().split("_");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].substring(0, 1) + parts[i].substring(1).toLowerCase();
        }
        return String.join(" ", parts);
    }
}
