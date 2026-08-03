package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * The player keeps their experience on death.
 */
public class KeepExperienceSkill extends SkillHandler implements Listener {

    public KeepExperienceSkill() {
        super(BuiltinSkills.KEEP_EXPERIENCE);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        // A skill such as the second chance may have cancelled the death entirely.
        if (event.isCancelled() || !isActive(event.getPlayer())) {
            return;
        }

        event.setKeepLevel(true);
        event.setShouldDropExperience(false);
    }
}