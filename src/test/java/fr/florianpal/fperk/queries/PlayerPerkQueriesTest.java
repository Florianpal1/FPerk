package fr.florianpal.fperk.queries;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.DatabaseConfig;
import fr.florianpal.fperk.managers.ConfigurationManager;
import fr.florianpal.fperk.managers.DatabaseManager;
import fr.florianpal.fperk.objects.PlayerPerk;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.StringReader;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises the storage against a real SQLite database. A file rather than {@code :memory:} : the
 * manager hands its connection back to the pool between calls, and an in-memory database would be a
 * different, empty one every time.
 */
class PlayerPerkQueriesTest {

    private PlayerPerkQueries queries;

    private final UUID alice = UUID.randomUUID();

    private final UUID bob = UUID.randomUUID();

    @BeforeEach
    void setUp(@TempDir Path folder) throws SQLException {
        DatabaseConfig database = new DatabaseConfig();
        database.load(YamlConfiguration.loadConfiguration(new StringReader("""
                database:
                  type: SQLite
                  url: "jdbc:sqlite:%s"
                  user: ""
                  password: ""
                """.formatted(folder.resolve("test.db")))));

        ConfigurationManager configuration = mock(ConfigurationManager.class);
        when(configuration.getDatabase()).thenReturn(database);

        FPerk plugin = mock(FPerk.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("FPerkTest"));
        when(plugin.getConfigurationManager()).thenReturn(configuration);

        DatabaseManager manager = new DatabaseManager(plugin);
        when(plugin.getDatabaseManager()).thenReturn(manager);

        queries = new PlayerPerkQueries(plugin);
        manager.addRepository(queries);
        manager.initializeTables();
    }

    private PlayerPerk store(UUID uuid, String perk, boolean enabled, long lastEnabled) {
        PlayerPerk playerPerk = new PlayerPerk(-1, uuid, perk, lastEnabled, enabled);
        playerPerk.setId(queries.addPlayerPerk(playerPerk));
        return playerPerk;
    }

    @Test
    @DisplayName("the SQLite schema uses AUTOINCREMENT and no MySQL table options")
    void describesASqliteTable() {
        String[] table = queries.getTable();

        assertEquals("fperk_playerperk", table[0]);
        assertTrue(table[1].contains("AUTOINCREMENT"), table[1]);
        assertFalse(table[1].contains("AUTO_INCREMENT "), table[1]);
        assertEquals("", table[2], "the MySQL charset clause is not valid SQLite");
    }

    @Test
    void insertsAndReadsBack() {
        PlayerPerk stored = store(alice, "fly", true, 1_000L);

        assertTrue(stored.getId() > 0, "the generated key is returned");

        List<PlayerPerk> read = queries.getPlayerPerks(alice);
        assertEquals(1, read.size());
        assertEquals(alice, read.get(0).getPlayerUUID());
        assertEquals("fly", read.get(0).getPerk());
        assertTrue(read.get(0).isEnabled());
        assertEquals(1_000L, read.get(0).getLastEnabled().getTime());
    }

    @Test
    void readsOnePerkByName() {
        store(alice, "fly", true, 1_000L);

        assertTrue(queries.getPlayerPerk(alice, "fly").isPresent());
        assertTrue(queries.getPlayerPerk(alice, "harvest").isEmpty());
        assertTrue(queries.getPlayerPerk(bob, "fly").isEmpty());
    }

    @Test
    void readsEveryPlayerAtOnce() {
        store(alice, "fly", true, 1_000L);
        store(alice, "harvest", false, 2_000L);
        store(bob, "fly", true, 3_000L);

        Map<UUID, List<PlayerPerk>> all = queries.getAllPerks();

        assertEquals(2, all.size());
        assertEquals(2, all.get(alice).size());
        assertEquals(1, all.get(bob).size());
    }

    @Test
    void unknownPlayerReadsAsEmpty() {
        assertTrue(queries.getPlayerPerks(UUID.randomUUID()).isEmpty());
        assertTrue(queries.getAllPerks().isEmpty());
    }

    @Test
    void updatePersistsTheState() {
        PlayerPerk stored = store(alice, "fly", true, 1_000L);

        stored.setEnabled(false);
        queries.updatePerk(stored);

        assertFalse(queries.getPlayerPerk(alice, "fly").orElseThrow().isEnabled());
    }

    @Test
    @DisplayName("update writes the activation date carried by the object, not the date of the write")
    void updateKeepsTheActivationDate() {
        PlayerPerk stored = store(alice, "fly", true, 1_000L);

        stored.setEnabled(false);
        queries.updatePerk(stored);

        assertEquals(1_000L, queries.getPlayerPerk(alice, "fly").orElseThrow().getLastEnabled().getTime(),
                "stamping the write date would restart the cooldown on every deactivation");
    }

    @Test
    @DisplayName("a fresh activation date is persisted when the object carries one")
    void updateStoresANewActivationDate() {
        PlayerPerk stored = store(alice, "fly", false, 1_000L);

        stored.setLastEnabled(new Date(50_000L));
        stored.setEnabled(true);
        queries.updatePerk(stored);

        PlayerPerk read = queries.getPlayerPerk(alice, "fly").orElseThrow();
        assertEquals(50_000L, read.getLastEnabled().getTime());
        assertTrue(read.isEnabled());
    }

    @Test
    @DisplayName("disableAllPerk turns off every perk of one player and leaves the others alone")
    void disablesEveryPerkOfOnePlayer() {
        store(alice, "fly", true, 1_000L);
        store(alice, "harvest", true, 1_000L);
        store(bob, "fly", true, 1_000L);

        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(alice);

        queries.disableAllPerk(player);

        assertTrue(queries.getPlayerPerks(alice).stream().noneMatch(PlayerPerk::isEnabled));
        assertTrue(queries.getPlayerPerks(bob).stream().allMatch(PlayerPerk::isEnabled));
    }

    @Test
    @DisplayName("disableAllPerk keeps the activation dates, so cooldowns survive a reset")
    void disableAllKeepsActivationDates() {
        store(alice, "fly", true, 7_000L);

        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(alice);
        queries.disableAllPerk(player);

        assertEquals(7_000L, queries.getPlayerPerk(alice, "fly").orElseThrow().getLastEnabled().getTime());
    }

    @Test
    void deleteRemovesTheRow() {
        PlayerPerk stored = store(alice, "fly", true, 1_000L);

        queries.deletePerk(stored.getId());

        assertTrue(queries.getPlayerPerks(alice).isEmpty());
    }

    @Test
    @DisplayName("a player may own several perks, one row each")
    void storesOneRowPerPerk() {
        store(alice, "fly", true, 1_000L);
        store(alice, "harvest", false, 2_000L);
        store(alice, "vacuum", true, 3_000L);

        assertEquals(3, queries.getPlayerPerks(alice).size());
        assertEquals(3, queries.getPlayerPerks(alice).stream().map(PlayerPerk::getId).distinct().count());
    }
}
