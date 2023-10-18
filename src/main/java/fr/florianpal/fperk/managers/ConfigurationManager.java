
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

package fr.florianpal.fperk.managers;

import fr.florianpal.fperk.FPerk;
import fr.florianpal.fperk.configurations.CompetenceConfig;
import fr.florianpal.fperk.configurations.DatabaseConfig;
import fr.florianpal.fperk.configurations.GlobalConfig;
import fr.florianpal.fperk.configurations.PerkConfig;
import fr.florianpal.fperk.configurations.gui.MainGuiConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigurationManager {
    private final DatabaseConfig database = new DatabaseConfig();
    private final FileConfiguration databaseConfig;

    private final GlobalConfig globalConfig = new GlobalConfig();
    private FileConfiguration globalConfiguration;

    private final PerkConfig perkConfig = new PerkConfig();
    private FileConfiguration perkConfiguration;

    private final CompetenceConfig competenceConfig = new CompetenceConfig();
    private FileConfiguration competenceConfiguration;

    private final MainGuiConfig mainGuiConfig = new MainGuiConfig();
    private FileConfiguration mainGuiConfiguration;


    public ConfigurationManager(FPerk core) {

        File databaseFile = new File(core.getDataFolder(), "database.yml");
        core.createDefaultConfiguration(databaseFile, "database.yml");
        databaseConfig = YamlConfiguration.loadConfiguration(databaseFile);

        database.load(databaseConfig);
        loadAllConfiguration(core);
    }

    public void reload(FPerk core) {
        loadAllConfiguration(core);
    }

    private void loadAllConfiguration(FPerk core) {
        File globalFile = new File(core.getDataFolder(), "config.yml");
        core.createDefaultConfiguration(globalFile, "config.yml");
        globalConfiguration = YamlConfiguration.loadConfiguration(globalFile);

        File perkFile = new File(core.getDataFolder(), "perk.yml");
        core.createDefaultConfiguration(perkFile, "perk.yml");
        perkConfiguration = YamlConfiguration.loadConfiguration(perkFile);

        File competenceFile = new File(core.getDataFolder(), "competence.yml");
        core.createDefaultConfiguration(competenceFile, "competence.yml");
        competenceConfiguration = YamlConfiguration.loadConfiguration(competenceFile);

        File mainGuiFile = new File(core.getDataFolder(), "gui/mainGui.yml");
        core.createDefaultConfiguration(mainGuiFile, "gui/mainGui.yml");
        mainGuiConfiguration = YamlConfiguration.loadConfiguration(mainGuiFile);

        competenceConfig.load(competenceConfiguration);
        perkConfig.load(perkConfiguration, this);
        mainGuiConfig.load(mainGuiConfiguration);
        globalConfig.load(globalConfiguration);

    }

    public DatabaseConfig getDatabase() {
        return database;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public PerkConfig getPerkConfig() {
        return perkConfig;
    }

    public MainGuiConfig getMainGuiConfig() {
        return mainGuiConfig;
    }

    public CompetenceConfig getCompetenceConfig() {
        return competenceConfig;
    }
}
