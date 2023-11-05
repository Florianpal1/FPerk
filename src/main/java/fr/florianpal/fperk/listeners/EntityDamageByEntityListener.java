package fr.florianpal.fperk.listeners;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.enums.EffectType;
import fr.florianpal.fperk.objects.Competence;
import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.objects.PlayerPerk;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static fr.florianpal.fperk.enums.EffectType.ANTI_KNOCKBACK;
import static fr.florianpal.fperk.enums.EffectType.SECOND_CHANCE;

public class EntityDamageByEntityListener implements Listener {

    private final FPerk plugin;

    public EntityDamageByEntityListener(FPerk plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if(event.getEntity().getType().equals(EntityType.PLAYER)) {
            Player player = (Player) event.getEntity();
            if(plugin.isPerkActive(player.getUniqueId(), ANTI_KNOCKBACK)) {
                player.setVelocity(new Vector());
                Bukkit.getScheduler().runTaskLater(plugin, () -> player.setVelocity(new Vector()), 1L);
            }
        }
    }
}
