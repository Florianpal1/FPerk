package fr.florianpal.fperk;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import fr.florianpal.fperk.commands.PerkCommand;
import fr.florianpal.fperk.managers.ConfigurationManager;
import fr.florianpal.fperk.managers.DatabaseManager;
import fr.florianpal.fperk.managers.VaultIntegrationManager;
import fr.florianpal.fperk.managers.commandManagers.CommandManager;
import fr.florianpal.fperk.managers.commandManagers.PlayerPerkCommandManager;
import fr.florianpal.fperk.objects.Perk;
import fr.florianpal.fperk.placeholders.FPlaceholderExpansion;
import fr.florianpal.fperk.queries.PlayerPerkQueries;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class FPerk extends JavaPlugin {

    private static TaskChainFactory taskChainFactory;

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static TaskChainFactory getTaskChainFactory() {
        return taskChainFactory;
    }

    private ConfigurationManager configurationManager;

    private PlayerPerkCommandManager playerPerkCommandManager;

    private CommandManager commandManager;

    private VaultIntegrationManager vaultIntegrationManager;

    private DatabaseManager databaseManager;

    private PlayerPerkQueries playerPerkQueries;


    @Override
    public void onEnable() {

        taskChainFactory = BukkitTaskChainFactory.create(this);

        configurationManager = new ConfigurationManager(this);

        File languageFile = new File(getDataFolder(), "lang_" + configurationManager.getGlobalConfig().getLang() + ".yml");
        createDefaultConfiguration(languageFile, "lang_" + configurationManager.getGlobalConfig().getLang() + ".yml");

        commandManager = new CommandManager(this);
        commandManager.registerDependency(ConfigurationManager.class, configurationManager);

        vaultIntegrationManager = new VaultIntegrationManager(this);

        databaseManager = new DatabaseManager(this);

        playerPerkQueries = new PlayerPerkQueries(this);

        databaseManager.addRepository(playerPerkQueries);
        databaseManager.initializeTables();

        playerPerkCommandManager = new PlayerPerkCommandManager(this);

        commandManager.registerCommand(new PerkCommand(this));


        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FPlaceholderExpansion(this).register();
        } else {
            Bukkit.getLogger().severe("Error : PlaceholderAPI not found !");
        }

    }

    public void createDefaultConfiguration(File actual, String defaultName) {
        // Make parent directories
        File parent = actual.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        if (actual.exists()) {
            return;
        }

        InputStream input = null;
        try {
            JarFile file = new JarFile(this.getFile());
            ZipEntry copy = file.getEntry(defaultName);
            if (copy == null) throw new FileNotFoundException();
            input = file.getInputStream(copy);
        } catch (IOException e) {
            getLogger().severe("Unable to read default configuration: " + defaultName);
        }

        if (input != null) {
            FileOutputStream output;
            try {
                output = new FileOutputStream(actual);
                byte[] buf = new byte[8192];
                int length;
                while ((length = input.read(buf)) > 0) {
                    output.write(buf, 0, length);
                }

                getLogger().info("Default configuration file written: " + actual.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void reloadConfig() {
        configurationManager.reload(this);
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public PlayerPerkCommandManager getPlayerPerkCommandManager() {
        return playerPerkCommandManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public VaultIntegrationManager getVaultIntegrationManager() {
        return vaultIntegrationManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerPerkQueries getPlayerPerkQueries() {
        return playerPerkQueries;
    }
}
