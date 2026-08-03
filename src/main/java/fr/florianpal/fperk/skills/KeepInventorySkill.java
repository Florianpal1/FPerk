package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * The player keeps their inventory on death.
 */
public class KeepInventorySkill extends SkillHandler implements Listener {

    public KeepInventorySkill() {
        super(BuiltinSkills.KEEP_INVENTORY);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        // A skill such as the second chance may have cancelled the death entirely.
        if (event.isCancelled() || !isActive(event.getPlayer())) {
            return;
        }

        event.setKeepInventory(true);
        event.getDrops().clear();
    }
}