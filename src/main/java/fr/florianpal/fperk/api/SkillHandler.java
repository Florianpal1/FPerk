package fr.florianpal.fperk.api;

import fr.florianpal.fperk.FPerk;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.UUID;

/**
 * The single class an addon has to extend to teach FPerk a new skill.
 *
 * <p>The {@link #getId() id} given to the constructor is the value server owners write in the
 * {@code type} field of {@code skill.yml}. Once the handler is registered, that type behaves
 * exactly like a builtin one : it can be listed in the {@code skills} of any perk of
 * {@code perk.yml}, it inherits the perk icon, permission, cooldown and duration, and it shows up in
 * the GUI without a single line of extra code.</p>
 *
 * <h2>Minimal addon</h2>
 * <pre>
 * public class DoubleJumpSkill extends SkillHandler implements Listener {
 *
 *     public DoubleJumpSkill() {
 *         super("DOUBLE_JUMP");
 *     }
 *
 *     &#64;Override
 *     public void onEnable(SkillContext context) {
 *         context.getPlayer().setAllowFlight(true);
 *     }
 *
 *     &#64;Override
 *     public void onDisable(SkillContext context) {
 *         context.getPlayer().setAllowFlight(false);
 *     }
 *
 *     &#64;EventHandler
 *     public void onToggleFlight(PlayerToggleFlightEvent event) {
 *         if (!isActive(event.getPlayer())) {
 *             return;
 *         }
 *         event.setCancelled(true);
 *         event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.5));
 *     }
 * }
 * </pre>
 * registered from the addon {@code onEnable()} :
 * <pre>
 * FPerk fperk = (FPerk) getServer().getPluginManager().getPlugin("FPerk");
 * fperk.getSkillRegistry().register(this, new DoubleJumpSkill());
 * </pre>
 *
 * <h2>Passive behaviour</h2>
 * A handler that also implements {@link Listener} is registered with Bukkit automatically, on behalf
 * of the plugin that owns it. There is nothing else to do : inside an event, call
 * {@link #isActive(Player)} to know whether the skill is currently running for that player.
 *
 * <h2>Contract</h2>
 * <ul>
 *   <li>The three lifecycle methods always run on the main server thread.</li>
 *   <li>{@link #onDisable(SkillContext)} may be called several times in a row, and may be called
 *       without a preceding {@code onEnable} (a player joining without the permission, for
 *       instance). Keep it idempotent.</li>
 *   <li>An exception thrown by a lifecycle method is caught and logged by FPerk : it never breaks
 *       the other skills of the perk.</li>
 * </ul>
 */
public abstract class SkillHandler {

    private final String id;

    private FPerk plugin;

    private Plugin owner;

    /**
     * @param id the value to write in the {@code type} field of {@code skill.yml}. Case insensitive,
     *           normalised to upper case. Two handlers cannot share the same id.
     */
    protected SkillHandler(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("A skill id cannot be null or empty");
        }
        this.id = id.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * The skill type this handler answers to, upper cased.
     */
    public final String getId() {
        return id;
    }

    /**
     * Called once, right after the handler has been registered. At that point {@link #getPlugin()}
     * and {@link #getRegistry()} are available.
     */
    public void onRegister() {
    }

    /**
     * Called once the handler has been unregistered, typically when the owning addon is disabled.
     * Release here whatever {@link #onRegister()} acquired.
     */
    public void onUnregister() {
    }

    /**
     * The player just turned the perk on, through the GUI or through {@code /perk}.
     */
    public void onEnable(SkillContext context) {
    }

    /**
     * The perk stopped applying : the player turned it off, its duration expired, it was reset, or
     * the player lost the permission.
     */
    public void onDisable(SkillContext context) {
    }

    /**
     * The perk was already on and has to be applied again : the player joined, respawned, or the
     * server restarted. Defaults to {@link #onEnable(SkillContext)}, which is the right behaviour
     * for most skills. Override it when re-applying differs from a fresh activation, for example to
     * skip a one-shot animation or an instant heal.
     */
    public void onRestore(SkillContext context) {
        onEnable(context);
    }

    /**
     * Whether this skill is currently running for that player.
     */
    public final boolean isActive(Player player) {
        return player != null && isActive(player.getUniqueId());
    }

    /**
     * Whether this skill is currently running for that player. Also answers for offline players
     * whose perks were loaded from the database.
     */
    public final boolean isActive(UUID uuid) {
        return plugin != null && plugin.getSkillRegistry().isActive(uuid, id);
    }

    /**
     * FPerk itself, to reach the scheduler, the configuration or the perk storage.
     */
    protected final FPerk getPlugin() {
        return plugin;
    }

    /**
     * The registry this handler is registered in.
     */
    protected final SkillRegistry getRegistry() {
        return plugin == null ? null : plugin.getSkillRegistry();
    }

    /**
     * The plugin that registered this handler : FPerk for a builtin skill, the addon otherwise.
     */
    public final Plugin getOwner() {
        return owner;
    }

    void bind(FPerk plugin, Plugin owner) {
        this.plugin = plugin;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + id + "]";
    }
}
