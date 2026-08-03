package fr.florianpal.fperk.api;

import fr.florianpal.fperk.FPerk;
import org.bukkit.plugin.Plugin;

/**
 * Binds a handler the way {@link SkillRegistry#register} does, without going through Bukkit event
 * registration. Lets a test drive one skill in isolation.
 *
 * <p>Lives in the api package because {@code SkillHandler#bind} is deliberately package private.</p>
 */
public final class HandlerBinding {

    private HandlerBinding() {
    }

    public static void bind(SkillHandler handler, FPerk plugin, Plugin owner) {
        handler.bind(plugin, owner);
    }
}
