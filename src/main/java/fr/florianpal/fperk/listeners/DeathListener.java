package fr.florianpal.fperk.listeners;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.enums.EffectType;
import fr.florianpal.fperk.objects.Competence;
import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.objects.PlayerPerk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.xml.transform.stream.StreamSource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fr.florianpal.fperk.enums.EffectType.KEEP_INVENTORY;
import static fr.florianpal.fperk.enums.EffectType.SECOND_CHANCE;
import static fr.florianpal.fperk.utils.EffectUtils.getPerkWithCompetence;

public class DeathListener implements Listener {

    private final FPerk plugin;

    public DeathListener(FPerk plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeathMonitor(PlayerDeathEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if(plugin.isPerkActive(uuid, SECOND_CHANCE)) {
            return;
        }

        if (plugin.isPerkActive(uuid, EffectType.KEEP_INVENTORY)) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }

        if (plugin.isPerkActive(uuid, EffectType.KEEP_EXPERIENCE)) {
            event.setKeepLevel(true);
            event.setShouldDropExperience(false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (plugin.isPerkActive(player.getUniqueId(), SECOND_CHANCE)) {
            event.setCancelled(true);
            player.setHealth(10);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1));
            plugin.removePerkActive(player.getUniqueId(), SECOND_CHANCE);

            TaskChain<PlayerPerk> playerPerkTaskChain = FPerk.newChain();
            playerPerkTaskChain.asyncFirst(() -> plugin.getPlayerPerkCommandManager().getPlayerPerk(player)).sync(playerPerks -> {
                Perk perk = getPerkWithCompetence(plugin, SECOND_CHANCE);
                PlayerPerk playerPerk = playerPerks.stream().filter(p -> p.getPerk().equals(perk.getId())).findFirst().get();
                playerPerk.setEnabled(false);
                plugin.getPlayerPerkCommandManager().updatePlayerPerk(playerPerk);
                return null;
            }).execute();
        }
    }
}
