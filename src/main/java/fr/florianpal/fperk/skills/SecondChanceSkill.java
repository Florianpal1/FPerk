package fr.florianpal.fperk.skills;

import fr.florianpal.fperk.api.BuiltinSkills;
import fr.florianpal.fperk.api.SkillHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Acts as a totem of undying : the death is cancelled, and the perk is consumed.
 */
public class SecondChanceSkill extends SkillHandler implements Listener {

    public SecondChanceSkill() {
        super(BuiltinSkills.SECOND_CHANCE);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)) {
            return;
        }

        event.setCancelled(true);
        player.setHealth(10);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1));

        getRegistry().markInactive(player.getUniqueId(), getId());
        getPlugin().getSkillService().consumeSkill(player, getId());
    }
}