package fr.florianpal.fperk.listeners;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.PerkConfig;
import fr.florianpal.fperk.enums.EffectType;
import fr.florianpal.fperk.managers.commandManagers.PlayerPerkCommandManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class DeathListener implements Listener {

    private final FPerk plugin;

    private final PlayerPerkCommandManager playerPerkCommandManager;

    private final PerkConfig perkConfig;

    public DeathListener(FPerk plugin) {
        this.plugin = plugin;
        this.playerPerkCommandManager = plugin.getPlayerPerkCommandManager();
        this.perkConfig = plugin.getConfigurationManager().getPerkConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();
        if(plugin.isPerkActive(uuid, EffectType.KEEP_INVENTORY)) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }

        if(plugin.isPerkActive(uuid, EffectType.KEEP_EXPERIENCE)) {
            event.setKeepLevel(true);
            event.setShouldDropExperience(false);
        }
    }

}
