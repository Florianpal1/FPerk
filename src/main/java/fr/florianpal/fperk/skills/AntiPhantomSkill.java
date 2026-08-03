package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * Phantoms stop attacking the player.
 */
public class AntiPhantomSkill extends SkillHandler implements Listener {

    public AntiPhantomSkill() {
        super(BuiltinSkills.ANTI_PHANTOM);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player) || !event.getEntity().getType().equals(EntityType.PHANTOM)) {
            return;
        }

        if (isActive((Player) target) && PacificationSkill.TARGET_REASONS.contains(event.getReason())) {
            event.setCancelled(true);
        }
    }
}