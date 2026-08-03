package fr.florianpal.fperk.resources;

import fr.florianpal.fperk.languages.MessageKeys;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The language files are data, and the plugin never overwrites an existing one, so a missing or
 * misspelt key ships to every new server. These tests keep them honest.
 */
class LanguageFileTest {

    private static Configuration load(String name) {
        InputStream stream = Objects.requireNonNull(
                LanguageFileTest.class.getClassLoader().getResourceAsStream(name), name);
        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /**
     * The keys ACF resolves through {@link MessageKeys}, plus the command descriptions referenced by
     * the {@code @Description} annotations of the command class.
     */
    private static final List<String> DESCRIPTION_KEYS = List.of(
            "help_description",
            "show_help_description",
            "modify_help_description",
            "reload_help_description");

    @ParameterizedTest
    @ValueSource(strings = {"lang_en.yml", "lang_fr.yml"})
    @DisplayName("every MessageKeys entry has a translation, including databaseerror")
    void everyMessageKeyIsTranslated(String file) {
        Configuration config = load(file);

        for (MessageKeys key : MessageKeys.values()) {
            String path = "fperk." + key.name().toLowerCase(Locale.ROOT);
            assertNotNull(config.getString(path), file + " is missing " + path);
            assertFalse(config.getString(path).isBlank(), file + " has a blank " + path);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"lang_en.yml", "lang_fr.yml"})
    void everyCommandDescriptionIsTranslated(String file) {
        Configuration config = load(file);

        for (String key : DESCRIPTION_KEYS) {
            assertNotNull(config.getString("fperk." + key), file + " is missing fperk." + key);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"lang_en.yml", "lang_fr.yml"})
    @DisplayName("the placeholders written in the files are the ones the code substitutes")
    void placeholdersMatchTheCode(String file) {
        Configuration config = load(file);

        for (String key : List.of("modify_perk", "not_found", "no_permission", "delais")) {
            assertTrue(config.getString("fperk." + key).contains("{PerkName}"),
                    file + ": fperk." + key + " should carry {PerkName}");
        }

        // The command substitutes {NewStatus}; {Status} used to be sent and never matched.
        String modify = config.getString("fperk.modify_perk");
        assertTrue(modify.contains("{NewStatus}"), file + ": modify_perk should carry {NewStatus}");
        assertFalse(modify.contains("{Status}") && !modify.contains("{NewStatus}"),
                file + ": modify_perk uses a placeholder the code never substitutes");
    }

    @ParameterizedTest
    @ValueSource(strings = {"lang_en.yml", "lang_fr.yml"})
    @DisplayName("the ACF sections are kept, so framework messages stay translated")
    void acfSectionsArePresent(String file) {
        Configuration config = load(file);

        assertNotNull(config.getConfigurationSection("acf-core"));
        assertNotNull(config.getConfigurationSection("acf-minecraft"));
        assertNotNull(config.getString("acf-core.permission_denied"));
    }

    @Test
    @DisplayName("the French file describes perks, not the leftovers of another plugin")
    void frenchFileHasNoLeftovers() {
        Configuration config = load("lang_fr.yml");

        String description = config.getString("fperk.show_help_description");

        assertFalse(description.toLowerCase(Locale.ROOT).contains("entreprise"),
                "show_help_description still mentions another plugin's wording");
        assertTrue(description.toLowerCase(Locale.ROOT).contains("perk"));
    }

    @Test
    @DisplayName("both files carry the same fperk keys, so switching language loses nothing")
    void bothFilesCarryTheSameKeys() {
        Configuration english = load("lang_en.yml");
        Configuration french = load("lang_fr.yml");

        assertNotNull(english.getConfigurationSection("fperk"));
        assertNotNull(french.getConfigurationSection("fperk"));

        assertTrue(french.getConfigurationSection("fperk").getKeys(false)
                        .containsAll(english.getConfigurationSection("fperk").getKeys(false)),
                "the French file lacks keys the English one has");
        assertTrue(english.getConfigurationSection("fperk").getKeys(false)
                        .containsAll(french.getConfigurationSection("fperk").getKeys(false)),
                "the English file lacks keys the French one has");
    }
}
