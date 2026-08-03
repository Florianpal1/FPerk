package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillHandler;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * The player no longer takes fall damage.
 */
public class BrokenFallSkill extends SkillHandler implements Listener {

    public BrokenFallSkill() {
        super(BuiltinSkills.BROKEN_FALL);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!event.getEntity().getType().equals(EntityType.PLAYER)
                || event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (isActive((Player) event.getEntity())) {
            event.setDamage(0);
        }
    }
}