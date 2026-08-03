package fr.florianpal.fperk.configurations;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Builds the {@link Configuration} the config loaders expect, either from an inline document or from
 * one of the files actually shipped in the jar.
 */
final class Yaml {

    private Yaml() {
    }

    static Configuration of(String document) {
        return YamlConfiguration.loadConfiguration(new StringReader(document));
    }

    /**
     * A default file as it ships in {@code src/main/resources}, read from the test classpath.
     */
    static Configuration resource(String name) {
        InputStream stream = Objects.requireNonNull(
                Yaml.class.getClassLoader().getResourceAsStream(name), "missing resource " + name);

        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
