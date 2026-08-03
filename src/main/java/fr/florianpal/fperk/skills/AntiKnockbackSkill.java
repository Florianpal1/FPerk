package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

/**
 * The player is no longer pushed back when taking damage.
 *
 * <p>Listening to {@link EntityDamageEvent} also covers damage dealt by another entity, which
 * shares the same handler list.</p>
 */
public class AntiKnockbackSkill extends SkillHandler implements Listener {

    public AntiKnockbackSkill() {
        super(BuiltinSkills.ANTI_KNOCKBACK);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!isActive(player)) {
            return;
        }

        player.setVelocity(new Vector());
        // The knockback is applied after the event, so it has to be cleared again next tick.
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> player.setVelocity(new Vector()), 1L);
    }
}