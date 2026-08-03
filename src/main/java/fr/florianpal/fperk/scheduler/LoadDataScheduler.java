package fr.florianpal.fperk.scheduler;

import co.aikar.taskchain.TaskChain;
import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.PerkConfig;
import fr.florianpal.fperk.managers.SkillService;
import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.objects.PlayerPerk;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Rebuilds the state of every stored perk when the server starts.
 *
 * <p>Players already connected get their perks applied again. Players who are not on this server
 * only get their skills recorded as running, so that a BungeeCord setup sharing the same database
 * stays coherent.</p>
 */
public class LoadDataScheduler implements Runnable {

    private final FPerk plugin;

    private final PerkConfig perkConfig;

    public LoadDataScheduler(FPerk plugin) {
        this.plugin = plugin;
        this.perkConfig = plugin.getConfigurationManager().getPerkConfig();
    }

    @Override
    public void run() {
        TaskChain<Map<UUID, List<PlayerPerk>>> chain = FPerk.newChain();
        chain.asyncFirst(() -> plugin.getPlayerPerkCommandManager().getAllPlayerPerk())
                .syncLast(this::apply)
                .execute();
    }

    private void apply(Map<UUID, List<PlayerPerk>> allPlayerPerks) {
        SkillService skillService = plugin.getSkillService();

        for (Map.Entry<UUID, List<PlayerPerk>> entry : allPlayerPerks.entrySet()) {
            UUID uuid = entry.getKey();
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                skillService.syncPlayer(player, new ArrayList<>(entry.getValue()));
                continue;
            }

            for (PlayerPerk playerPerk : entry.getValue()) {
                Perk perk = perkConfig.getPerks().get(playerPerk.getPerk());
                if (perk == null) {
                    continue;
                }

                if (playerPerk.isEnabled() && hasPermission(uuid, perk.getPermission())) {
                    skillService.markActiveOffline(uuid, perk);
                } else {
                    skillService.markInactiveOffline(uuid, perk);
                }
            }
        }
    }

    /**
     * Permission of a player who is not connected here, read from LuckPerms. Defaults to granted
     * when LuckPerms has nothing loaded for them, which is what FPerk did before.
     */
    private boolean hasPermission(UUID uuid, String permission) {
        if (plugin.getLuckPerms() == null || permission == null) {
            return true;
        }

        User user = plugin.getLuckPerms().getUserManager().getUser(uuid);
        if (user == null) {
            return true;
        }

        return user.getNodes().stream()
                .filter(NodeType.PERMISSION::matches)
                .map(NodeType.PERMISSION::cast)
                .filter(Node::getValue)
                .anyMatch(node -> node.getPermission().equals(permission));
    }
}