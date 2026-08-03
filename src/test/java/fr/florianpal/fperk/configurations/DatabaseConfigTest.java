package fr.florianpal.fperk.configurations;

import fr.florianpal.fperk.enums.SQLType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseConfigTest {

    @Test
    void readsSqlite() {
        DatabaseConfig config = new DatabaseConfig();

        config.load(Yaml.of("""
                database:
                  type: SQLite
                  url: "jdbc:sqlite:test.db"
                  user: "root"
                  password: ""
                """));

        assertEquals(SQLType.SQLite, config.getSqlType());
        assertEquals("jdbc:sqlite:test.db", config.getUrl());
        assertEquals("root", config.getUser());
        assertEquals("", config.getPassword());
    }

    @Test
    void readsMysql() {
        DatabaseConfig config = new DatabaseConfig();

        config.load(Yaml.of("""
                database:
                  type: MySQL
                  url: "jdbc:mysql://localhost:3306/fperk"
                  user: "fperk"
                  password: "secret"
                """));

        assertEquals(SQLType.MySQL, config.getSqlType());
        assertEquals("secret", config.getPassword());
    }

    @Test
    @DisplayName("the type falls back to MySQL when the key is missing")
    void defaultsToMysql() {
        DatabaseConfig config = new DatabaseConfig();

        config.load(Yaml.of("""
                database:
                  url: "jdbc:mysql://localhost:3306/fperk"
                """));

        assertEquals(SQLType.MySQL, config.getSqlType());
    }

    @Test
    @DisplayName("the type is case sensitive, and a wrong value fails loudly at startup")
    void typeIsCaseSensitive() {
        DatabaseConfig config = new DatabaseConfig();

        assertThrows(IllegalArgumentException.class, () -> config.load(Yaml.of("""
                database:
                  type: mysql
                """)));

        assertThrows(IllegalArgumentException.class, () -> config.load(Yaml.of("""
                database:
                  type: sqlite
                """)));
    }

    @Test
    @DisplayName("the shipped database.yml points at a file inside the plugin folder")
    void shippedDefaults() {
        DatabaseConfig config = new DatabaseConfig();

        config.load(Yaml.resource("database.yml"));

        assertEquals(SQLType.SQLite, config.getSqlType());
        assertEquals("jdbc:sqlite:plugins/FPerk/database.db", config.getUrl());
    }
}
