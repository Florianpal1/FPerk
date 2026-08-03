package fr.florianpal.fperk.configurations;

import fr.florianpal.fperk.enums.StatusType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalConfigTest {

    @Test
    void readsLanguageAndStatusLabels() {
        GlobalConfig config = new GlobalConfig();

        config.load(Yaml.of("""
                lang: "fr"
                status:
                  enabled: "&2On"
                  disabled: "&cOff"
                """));

        assertEquals("fr", config.getLang());
        assertEquals("&2On", config.getStatus().get(StatusType.ACTIVATED));
        assertEquals("&cOff", config.getStatus().get(StatusType.DESACTIVED));
    }

    @Test
    @DisplayName("the shipped config.yml is the documented one")
    void shippedDefaults() {
        GlobalConfig config = new GlobalConfig();

        config.load(Yaml.resource("config.yml"));

        assertEquals("en", config.getLang());
        assertEquals("&2Enabled", config.getStatus().get(StatusType.ACTIVATED));
        assertEquals("&cDisabled", config.getStatus().get(StatusType.DESACTIVED));
    }
}
