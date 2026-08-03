
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
import fr.florianpal.fperk.enums.SQLType;
import fr.florianpal.fperk.objects.PlayerPerk;
import fr.florianpal.fperk.queries.PlayerPerkQueries;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerPerkCommandManager {

    private final PlayerPerkQueries playerPerkQueries;

    private Map<UUID, List<PlayerPerk>> sqliteCache = new HashMap<>();

    private final SQLType sqlType;

    private int idMax = 0;

    public PlayerPerkCommandManager(FPerk plugin) {
        this.playerPerkQueries = plugin.getPlayerPerkQueries();
        this.sqliteCache = playerPerkQueries.getAllPerks();
        this.sqlType = plugin.getConfigurationManager().getDatabase().getSqlType();
    }

    public List<PlayerPerk> getPlayerPerk(Player player) {
        if (SQLType.SQLite.equals(sqlType)) {
            return new ArrayList<>(sqliteCache.getOrDefault(player.getUniqueId(), new ArrayList<>()));
        }
        return playerPerkQueries.getPlayerPerks(player.getUniqueId());
    }

    public List<PlayerPerk> getPlayerPerk(OfflinePlayer player) {
        if (SQLType.SQLite.equals(sqlType)) {
            return new ArrayList<>(sqliteCache.getOrDefault(player.getUniqueId(), new ArrayList<>()));
        }
        return playerPerkQueries.getPlayerPerks(player.getUniqueId());
    }

    public Optional<PlayerPerk> getPlayerPerk(OfflinePlayer player, String perk) {
        if (SQLType.SQLite.equals(sqlType)) {
            return sqliteCache.getOrDefault(player.getUniqueId(), new ArrayList<>()).stream().filter(p -> p.getPerk().equals(perk)).findFirst();
        }
        return playerPerkQueries.getPlayerPerk(player.getUniqueId(), perk);
    }

    public Map<UUID, List<PlayerPerk>> getAllPlayerPerk() {
        if (SQLType.SQLite.equals(sqlType)) {
            return sqliteCache;
        }
        return playerPerkQueries.getAllPerks();
    }

    public int addPlayerPerk(PlayerPerk playerPerk) {
        int id = playerPerkQueries.addPlayerPerk(playerPerk);
        if (SQLType.SQLite.equals(sqlType))  {

            playerPerk.setId(id);
            if (sqliteCache.containsKey(playerPerk.getPlayerUUID())) {
                sqliteCache.get(playerPerk.getPlayerUUID()).add(playerPerk);
            } else {
                sqliteCache.put(playerPerk.getPlayerUUID(), new ArrayList<>(List.of(playerPerk)));
            }
            idMax = idMax + 1;
        }
        return id;
    }

    public void updatePlayerPerk(PlayerPerk playerPerk) {
        if (SQLType.SQLite.equals(sqlType))  {

            if (sqliteCache.containsKey(playerPerk.getPlayerUUID())) {
                Optional<PlayerPerk> playerPerkOptional = sqliteCache.get(playerPerk.getPlayerUUID()).stream().filter(p -> p.getId() == playerPerk.getId()).findFirst();
                playerPerkOptional.ifPresent(perk -> sqliteCache.get(playerPerk.getPlayerUUID()).remove(perk));
                sqliteCache.get(playerPerk.getPlayerUUID()).add(playerPerk);
            } else {
                sqliteCache.put(playerPerk.getPlayerUUID(), new ArrayList<>());
                sqliteCache.get(playerPerk.getPlayerUUID()).add(playerPerk);
            }
        }
        playerPerkQueries.updatePerk(playerPerk);
    }

    public void disableAllPerk(Player player) {
        if (SQLType.SQLite.equals(sqlType) && sqliteCache.containsKey(player.getUniqueId())) {
                sqliteCache.get(player.getUniqueId()).forEach(p -> p.setEnabled(false));
            }

        playerPerkQueries.disableAllPerk(player);
    }
}