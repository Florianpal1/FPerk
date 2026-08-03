package fr.florianpal.fperk.api;

import fr.florianpal.fperk.FPerk;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Where every skill type known to FPerk lives, builtin and addon alike.
 *
 * <p>Reach it with {@code FPerk#getSkillRegistry()} and call
 * {@link #register(Plugin, SkillHandler)} from the {@code onEnable()} of your plugin.</p>
 *
 * <p>The registry also owns the runtime state : which skills are currently running for which
 * players. Handlers query it through {@link SkillHandler#isActive(java.util.UUID)}; the state itself
 * is driven by {@code SkillService} and should not be written to from an addon.</p>
 */
public class SkillRegistry {

    private final FPerk plugin;

    private final Map<String, SkillHandler> handlers = new LinkedHashMap<>();

    private final Map<String, Set<UUID>> activePlayers = new ConcurrentHashMap<>();

    public SkillRegistry(FPerk plugin) {
        this.plugin = plugin;
    }

    /**
     * Makes a skill type usable in {@code skill.yml}.
     *
     * <p>If the handler implements {@link Listener}, it is registered with Bukkit on behalf of
     * {@code owner}, so that disabling the addon also unhooks its events.</p>
     *
     * @param owner   the plugin the handler belongs to, usually {@code this}
     * @param handler the handler to add
     * @throws IllegalStateException if another handler already claims the same id
     */
    public void register(Plugin owner, SkillHandler handler) {
        if (owner == null || handler == null) {
            throw new IllegalArgumentException("owner and handler are both required");
        }

        SkillHandler previous = handlers.get(handler.getId());
        if (previous != null) {
            throw new IllegalStateException("The skill type '" + handler.getId() + "' is already provided by "
                    + previous + " (plugin " + previous.getOwner().getName() + ")");
        }

        handler.bind(plugin, owner);
        handlers.put(handler.getId(), handler);

        if (handler instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) handler, owner);
        }

        try {
            handler.onRegister();
        } catch (Exception e) {
            plugin.getLogger().severe("onRegister() failed for the skill '" + handler.getId() + "' of "
                    + owner.getName() + ": " + e);
        }

        if (owner != plugin) {
            plugin.getLogger().info("Skill '" + handler.getId() + "' registered by " + owner.getName());
        }
    }

    /**
     * Removes a skill type. Any perk still referencing it stops applying it.
     *
     * @return the handler that was removed, {@code null} if the id was unknown
     */
    public SkillHandler unregister(String id) {
        SkillHandler handler = handlers.remove(normalize(id));
        if (handler == null) {
            return null;
        }

        if (handler instanceof Listener) {
            HandlerList.unregisterAll((Listener) handler);
        }
        activePlayers.remove(handler.getId());

        try {
            handler.onUnregister();
        } catch (Exception e) {
            plugin.getLogger().severe("onUnregister() failed for the skill '" + handler.getId() + "': " + e);
        }
        return handler;
    }

    /**
     * Removes every skill registered by that plugin. Call it from your {@code onDisable()} if your
     * addon can be reloaded at runtime.
     */
    public void unregisterAll(Plugin owner) {
        for (String id : handlers.values().stream()
                .filter(h -> h.getOwner() == owner)
                .map(SkillHandler::getId)
                .collect(Collectors.toList())) {
            unregister(id);
        }
    }

    /**
     * The handler answering to that skill type, {@code null} if none is registered.
     */
    public SkillHandler get(String id) {
        return handlers.get(normalize(id));
    }

    public boolean isRegistered(String id) {
        return handlers.containsKey(normalize(id));
    }

    /**
     * Every skill type currently usable in {@code skill.yml}.
     */
    public Set<String> getIds() {
        return Collections.unmodifiableSet(handlers.keySet());
    }

    /**
     * Whether that skill type is currently running for that player.
     */
    public boolean isActive(UUID uuid, String id) {
        Set<UUID> players = activePlayers.get(normalize(id));
        return players != null && players.contains(uuid);
    }

    /**
     * Every skill type currently running for that player.
     */
    public Set<String> getActiveSkills(UUID uuid) {
        return activePlayers.entrySet().stream()
                .filter(entry -> entry.getValue().contains(uuid))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void markActive(UUID uuid, String id) {
        activePlayers.computeIfAbsent(normalize(id), key -> ConcurrentHashMap.newKeySet()).add(uuid);
    }

    public void markInactive(UUID uuid, String id) {
        Set<UUID> players = activePlayers.get(normalize(id));
        if (players != null) {
            players.remove(uuid);
        }
    }

    /**
     * Drops every active skill of that player, without notifying the handlers.
     */
    public void clear(UUID uuid) {
        for (Set<UUID> players : activePlayers.values()) {
            players.remove(uuid);
        }
    }

    private String normalize(String id) {
        return id == null ? "" : id.trim().toUpperCase(Locale.ROOT);
    }
}
