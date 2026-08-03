package fr.florianpal.fperk.managers;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.api.SkillContext;
import fr.florianpal.fperk.api.SkillHandler;
import fr.florianpal.fperk.api.SkillRegistry;
import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.objects.PlayerPerk;
import fr.florianpal.fperk.objects.Skill;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drives the lifecycle of the skills of a perk : activation, deactivation, expiry and restoration
 * after a join, a respawn or a restart.
 *
 * <p>This is the only place that decides <em>when</em> a handler runs. What a skill actually does
 * lives in its {@link SkillHandler}, builtin or addon alike.</p>
 */
public class SkillService {

    private static final long TICKS_PER_SECOND = 20L;

    /**
     * How long to wait before asserting again that a perk the player is not allowed to use is
     * really off. Potion effects in particular are re-applied by the respawn sequence itself.
     */
    private static final long REASSERT_DELAY_TICKS = 80L;

    private final FPerk plugin;

    private final SkillRegistry registry;

    /**
     * Pending expiry tasks, per player then per perk, so that toggling a perk off cancels the timer
     * that was about to turn it off on its own.
     */
    private final Map<UUID, Map<String, BukkitTask>> expirations = new ConcurrentHashMap<>();

    private final Set<String> unknownTypesWarned = ConcurrentHashMap.newKeySet();

    private final Set<String> unknownPerksWarned = ConcurrentHashMap.newKeySet();

    public SkillService(FPerk plugin) {
        this.plugin = plugin;
        this.registry = plugin.getSkillRegistry();
    }

    /**
     * Turns every skill of the perk on and, unless the perk is persistent, schedules its expiry.
     *
     * <p>Stamps the activation date, which is what the cooldown of the perk and the remaining
     * duration computed on {@link #restore} are both measured from. Callers are expected to persist
     * the player perk afterwards. Restoring a perk goes through {@link #restore} instead and leaves
     * the stamp alone, so that a duration keeps running down across a reconnection.</p>
     */
    public void enable(Player player, PlayerPerk playerPerk, Perk perk) {
        UUID uuid = player.getUniqueId();

        playerPerk.setLastEnabled(new Date());

        for (Skill skill : perk.getSkills().values()) {
            SkillHandler handler = resolve(perk, skill);
            if (handler == null) {
                continue;
            }

            registry.markActive(uuid, handler.getId());
            run(handler, "onEnable", () -> handler.onEnable(new SkillContext(player, perk, skill, playerPerk)));
        }

        if (!perk.isPersistant()) {
            scheduleExpiry(player, playerPerk, perk, perk.getTime());
        }
    }

    /**
     * Turns every skill of the perk off and drops any pending expiry.
     */
    public void disable(Player player, PlayerPerk playerPerk, Perk perk) {
        UUID uuid = player.getUniqueId();
        cancelExpiry(uuid, perk.getId());

        for (Skill skill : perk.getSkills().values()) {
            SkillHandler handler = resolve(perk, skill);
            if (handler == null) {
                continue;
            }

            registry.markInactive(uuid, handler.getId());
            run(handler, "onDisable", () -> handler.onDisable(new SkillContext(player, perk, skill, playerPerk)));
        }
    }

    /**
     * Applies again a perk that was already on before the player joined, respawned or before the
     * server restarted. A timed perk whose duration ran out while the player was away is turned off
     * instead, and the change is persisted.
     */
    public void restore(Player player, PlayerPerk playerPerk, Perk perk) {
        long remaining = remainingSeconds(playerPerk, perk);

        if (!perk.isPersistant() && remaining <= 0) {
            disable(player, playerPerk, perk);
            if (playerPerk.isEnabled()) {
                playerPerk.setEnabled(false);
                plugin.getPlayerPerkCommandManager().updatePlayerPerk(playerPerk);
            }
            return;
        }

        UUID uuid = player.getUniqueId();
        for (Skill skill : perk.getSkills().values()) {
            SkillHandler handler = resolve(perk, skill);
            if (handler == null) {
                continue;
            }

            registry.markActive(uuid, handler.getId());
            run(handler, "onRestore", () -> handler.onRestore(new SkillContext(player, perk, skill, playerPerk)));
        }

        if (!perk.isPersistant()) {
            scheduleExpiry(player, playerPerk, perk, remaining);
        }
    }

    /**
     * Brings a player back in line with what the database says : restores the perks that are on and
     * that they are still allowed to use, turns the others off. Used on join and on respawn.
     */
    public void syncPlayer(Player player, List<PlayerPerk> playerPerks) {
        Map<String, Perk> perks = plugin.getConfigurationManager().getPerkConfig().getPerks();

        for (PlayerPerk playerPerk : playerPerks) {
            Perk perk = perks.get(playerPerk.getPerk());
            if (perk == null) {
                warnUnknownPerk(playerPerk.getPerk());
                continue;
            }

            if (playerPerk.isEnabled() && player.hasPermission(perk.getPermission())) {
                restore(player, playerPerk, perk);
            } else {
                disable(player, playerPerk, perk);
                reassertDisabled(player, playerPerk, perk);
            }
        }
    }

    /**
     * Turns off every perk the player currently owns, whatever its state.
     */
    public void resetAll(Player player, List<PlayerPerk> playerPerks) {
        Map<String, Perk> perks = plugin.getConfigurationManager().getPerkConfig().getPerks();

        for (PlayerPerk playerPerk : playerPerks) {
            Perk perk = perks.get(playerPerk.getPerk());
            if (perk != null) {
                disable(player, playerPerk, perk);
            }
        }

        cancelExpiries(player.getUniqueId());
        registry.clear(player.getUniqueId());
    }

    /**
     * Turns off, and persists as off, every enabled perk of the player holding that skill type.
     * A skill consumed on use, such as the second chance, calls this once it fired.
     */
    public void consumeSkill(Player player, String skillType) {
        Map<String, Perk> perks = plugin.getConfigurationManager().getPerkConfig().getPerks();

        TaskChain<List<PlayerPerk>> chain = FPerk.newChain();
        chain.asyncFirst(() -> plugin.getPlayerPerkCommandManager().getPlayerPerk(player)).syncLast(playerPerks -> {
            for (PlayerPerk playerPerk : playerPerks) {
                if (!playerPerk.isEnabled()) {
                    continue;
                }

                Perk perk = perks.get(playerPerk.getPerk());
                if (perk == null || !holdsSkill(perk, skillType)) {
                    continue;
                }

                disable(player, playerPerk, perk);
                playerPerk.setEnabled(false);
                plugin.getPlayerPerkCommandManager().updatePlayerPerk(playerPerk);
            }
        }).execute();
    }

    /**
     * Records the skills of a perk as running for a player who is not connected to this server,
     * without invoking any handler. Keeps the state coherent on a BungeeCord setup, where the perk
     * may have been turned on somewhere else.
     */
    public void markActiveOffline(UUID uuid, Perk perk) {
        for (Skill skill : perk.getSkills().values()) {
            SkillHandler handler = resolve(perk, skill);
            if (handler != null) {
                registry.markActive(uuid, handler.getId());
            }
        }
    }

    /**
     * The counterpart of {@link #markActiveOffline(UUID, Perk)}.
     */
    public void markInactiveOffline(UUID uuid, Perk perk) {
        for (Skill skill : perk.getSkills().values()) {
            SkillHandler handler = resolve(perk, skill);
            if (handler != null) {
                registry.markInactive(uuid, handler.getId());
            }
        }
    }

    /**
     * Forgets everything about a player : pending timers and active skills. Called when they leave.
     */
    public void forget(UUID uuid) {
        cancelExpiries(uuid);
        registry.clear(uuid);
    }

    /**
     * Seconds left before a timed perk expires, from the moment it was last turned on. Negative or
     * zero once it is over.
     */
    public long remainingSeconds(PlayerPerk playerPerk, Perk perk) {
        long elapsed = (new Date().getTime() - playerPerk.getLastEnabled().getTime()) / 1000L;
        return perk.getTime() - elapsed;
    }

    private void scheduleExpiry(Player player, PlayerPerk playerPerk, Perk perk, long delaySeconds) {
        UUID uuid = player.getUniqueId();
        cancelExpiry(uuid, perk.getId());

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Map<String, BukkitTask> pending = expirations.get(uuid);
            if (pending != null) {
                pending.remove(perk.getId());
            }

            disable(player, playerPerk, perk);
            playerPerk.setEnabled(false);
            plugin.getPlayerPerkCommandManager().updatePlayerPerk(playerPerk);
        }, Math.max(1L, delaySeconds * TICKS_PER_SECOND));

        expirations.computeIfAbsent(uuid, key -> new ConcurrentHashMap<>()).put(perk.getId(), task);
    }

    private void cancelExpiry(UUID uuid, String perkId) {
        Map<String, BukkitTask> tasks = expirations.get(uuid);
        if (tasks == null) {
            return;
        }

        BukkitTask task = tasks.remove(perkId);
        if (task != null) {
            task.cancel();
        }
    }

    private void cancelExpiries(UUID uuid) {
        Map<String, BukkitTask> tasks = expirations.remove(uuid);
        if (tasks != null) {
            tasks.values().forEach(BukkitTask::cancel);
        }
    }

    /**
     * Asserts a second time that a perk the player may not use is off. The respawn sequence and
     * other plugins can re-apply an effect a few ticks after we removed it. Skipped if the player
     * legitimately turned the perk back on in the meantime.
     */
    private void reassertDisabled(Player player, PlayerPerk playerPerk, Perk perk) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || holdsAnyActiveSkill(player.getUniqueId(), perk)) {
                return;
            }
            disable(player, playerPerk, perk);
        }, REASSERT_DELAY_TICKS);
    }

    private boolean holdsAnyActiveSkill(UUID uuid, Perk perk) {
        for (Skill skill : perk.getSkills().values()) {
            if (skill != null && registry.isActive(uuid, skill.getType())) {
                return true;
            }
        }
        return false;
    }

    private boolean holdsSkill(Perk perk, String skillType) {
        for (Skill skill : perk.getSkills().values()) {
            if (skill != null && skill.getType().equalsIgnoreCase(skillType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The handler in charge of a skill, or {@code null} with a one-off warning when the perk points
     * at a skill that does not exist or at a type no plugin provides.
     */
    private SkillHandler resolve(Perk perk, Skill skill) {
        if (skill == null) {
            warnUnknownType(perk.getId(), "<missing skill entry>");
            return null;
        }

        SkillHandler handler = registry.get(skill.getType());
        if (handler == null) {
            warnUnknownType(perk.getId(), skill.getType());
        }
        return handler;
    }

    private void run(SkillHandler handler, String phase, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            plugin.getLogger().severe("The skill '" + handler.getId() + "' provided by "
                    + handler.getOwner().getName() + " failed during " + phase + "()");
            e.printStackTrace();
        }
    }

    private void warnUnknownType(String perkId, String type) {
        if (unknownTypesWarned.add(perkId + "/" + type)) {
            plugin.getLogger().warning("The perk '" + perkId + "' uses the skill type '" + type
                    + "', which no plugin provides. It is ignored. Known types: " + registry.getIds());
        }
    }

    private void warnUnknownPerk(String perkId) {
        if (unknownPerksWarned.add(perkId)) {
            plugin.getLogger().warning("A player owns the perk '" + perkId
                    + "', which is no longer defined in perk.yml. It is ignored.");
        }
    }
}