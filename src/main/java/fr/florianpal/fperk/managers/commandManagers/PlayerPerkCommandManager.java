
/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * Last modification : 07/01/2022 23:07
 *
 *  @author Florianpal.
 */

package fr.florianpal.fperk.managers.commandManagers;


import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.objects.PlayerPerk;
import fr.florianpal.fperk.queries.PlayerPerkQueries;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerPerkCommandManager {

    private final PlayerPerkQueries playerPerkQueries;

    public PlayerPerkCommandManager(FPerk plugin) {
        this.playerPerkQueries = plugin.getPlayerPerkQueries();
    }

    public List<PlayerPerk> getPlayerPerk(Player player) {
        return playerPerkQueries.getPlayerPerks(player.getUniqueId());
    }

    public List<PlayerPerk> getPlayerPerk(OfflinePlayer player) {
        return playerPerkQueries.getPlayerPerks(player.getUniqueId());
    }

    public Map<UUID, List<PlayerPerk>> getAllPlayerPerk() {
        return playerPerkQueries.getAllPerks();
    }

    public int addPlayerPerk(PlayerPerk playerPerk) {
        return playerPerkQueries.addPlayerPerk(playerPerk);
    }

    public void updatePlayerPerk(PlayerPerk playerPerk) {
        playerPerkQueries.updatePerk(playerPerk);
    }

    public void disableAllPerk(Player player) {
        playerPerkQueries.disableAllPerk(player);
    }
}