package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.List;

import static org.bukkit.event.entity.EntityTargetEvent.TargetReason.CLOSEST_ENTITY;
import static org.bukkit.event.entity.EntityTargetEvent.TargetReason.CLOSEST_PLAYER;

/**
 * Mobs stop attacking the player.
 */
public class PacificationSkill extends SkillHandler implements Listener {

    static final List<EntityTargetEvent.TargetReason> TARGET_REASONS = List.of(CLOSEST_PLAYER, CLOSEST_ENTITY);

    public PacificationSkill() {
        super(BuiltinSkills.PACIFICATION);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player)) {
            return;
        }

        if (isActive((Player) target) && TARGET_REASONS.contains(event.getReason())) {
            event.setCancelled(true);
        }
    }
}