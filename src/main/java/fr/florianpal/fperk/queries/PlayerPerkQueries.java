
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

package fr.florianpal.fperk.queries;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.IDatabaseTable;
import fr.florianpal.fperk.enums.SQLType;
import fr.florianpal.fperk.managers.DatabaseManager;
import fr.florianpal.fperk.objects.PlayerPerk;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class PlayerPerkQueries implements IDatabaseTable {

    private static final String GET_ALL_PERK = "SELECT * FROM fperk_playerperk";

    private static final String GET_ALL_PERK_FROM_PLAYER = "SELECT * FROM fperk_playerperk where playerUUID=?";
    private static final String GET_PERK_WITH_ID = "SELECT * FROM fperk_playerperk WHERE id=?";
    private static final String ADD_PERK = "INSERT INTO fperk_playerperk (playerUUID, perk, lastEnabled, isEnabled) VALUES(?,?,?,?)";

    private static final String UPDATE_PERK = "UPDATE fperk_playerperk SET playerUUID=?, perk=?, lastEnabled=?, isEnabled=? WHERE id=?";

    private static final String DISABLE_ALL_PERK = "UPDATE fperk_playerperk SET isEnabled=? WHERE playerUUID=?";

    private static final String DELETE_PERK = "DELETE FROM fperk_playerperk WHERE id=?";

    private final DatabaseManager databaseManager;

    private String autoIncrement = "AUTO_INCREMENT";

    private String parameters = "DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci";

    private final FPerk plugin;

    public PlayerPerkQueries(FPerk plugin) {
        this.databaseManager = plugin.getDatabaseManager();
        this.plugin = plugin;

        if (plugin.getConfigurationManager().getDatabase().getSqlType() == SQLType.SQLite) {
            autoIncrement = "AUTOINCREMENT";
            parameters = "";
        }
    }

    public int addPlayerPerk(PlayerPerk playerPerk) {
        int id = -1;
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(ADD_PERK, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, playerPerk.getPlayerUUID().toString());
            statement.setString(2, playerPerk.getPerk());
            statement.setLong(3, playerPerk.getLastEnabled().getTime());
            statement.setBoolean(4, playerPerk.isEnabled());
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            id = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    public void updatePerk(PlayerPerk playerPerk) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(UPDATE_PERK);
            statement.setString(1, playerPerk.getPlayerUUID().toString());
            statement.setString(2, playerPerk.getPerk());
            statement.setLong(3, new Date().getTime());
            statement.setBoolean(4, playerPerk.isEnabled());
            statement.setInt(5, playerPerk.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void disableAllPerk(Player player) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(DISABLE_ALL_PERK);
            statement.setInt(1, 0);
            statement.setString(2, player.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void deletePerk(int id) {
        PreparedStatement statement = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(DELETE_PERK);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Map<UUID, List<PlayerPerk>> getAllPerks() {
        PreparedStatement statement = null;
        ResultSet result = null;
        Map<UUID, List<PlayerPerk>> playerPerks = new HashMap<>();
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_ALL_PERK);

            result = statement.executeQuery();

            while (result.next()) {
                int id = result.getInt(1);
                UUID uuid = UUID.fromString(result.getString(2));
                String perk = result.getString(3);
                long lastEnabled = result.getLong(4);
                boolean enabled = result.getBoolean(5);

                if(playerPerks.containsKey(uuid)) {
                    playerPerks.get(uuid).add(new PlayerPerk(id, uuid, perk, lastEnabled, enabled));
                } else {
                    playerPerks.put(uuid, new ArrayList<>());
                    playerPerks.get(uuid).add(new PlayerPerk(id, uuid, perk, lastEnabled, enabled));
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return playerPerks;
    }

    public List<PlayerPerk> getPlayerPerks(UUID playerUUID) {
        ArrayList<PlayerPerk> playerPerks = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet result = null;
        try (Connection connection = databaseManager.getConnection()) {
            statement = connection.prepareStatement(GET_ALL_PERK_FROM_PLAYER);
            statement.setString(1, playerUUID.toString());
            result = statement.executeQuery();

            while (result.next()) {
                int id = result.getInt(1);
                String perk = result.getString(3);
                long lastEnabled = result.getLong(4);
                boolean enabled = result.getBoolean(5);

                playerPerks.add(new PlayerPerk(id, playerUUID, perk, lastEnabled, enabled));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return playerPerks;
    }

    @Override
    public String[] getTable() {
        return new String[]{"fperk_playerperk",
                "`id` INTEGER PRIMARY KEY " + autoIncrement + ", " +
                        "`playerUUID` VARCHAR(36) NOT NULL, " +
                        "`perk` VARCHAR(36) NOT NULL, " +
                        "`lastEnabled` LONG NOT NULL, " +
                        "`isEnabled` TINYINT(1) NOT NULL",
                parameters
        };
    }
}
