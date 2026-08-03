package fr.florianpal.fperk;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import fr.florianpal.fperk.api.SkillRegistry;
import fr.florianpal.fperk.commands.PerkCommand;
import fr.florianpal.fperk.listeners.*;
import fr.florianpal.fperk.managers.ConfigurationManager;
import fr.florianpal.fperk.managers.DatabaseManager;
import fr.florianpal.fperk.managers.SkillService;
import fr.florianpal.fperk.managers.VaultIntegrationManager;
import fr.florianpal.fperk.managers.commandManagers.CommandManager;
import fr.florianpal.fperk.managers.commandManagers.PlayerPerkCommandManager;
import fr.florianpal.fperk.placeholders.FPlaceholderExpansion;
import fr.florianpal.fperk.queries.PlayerPerkQueries;
import fr.florianpal.fperk.scheduler.LoadDataScheduler;
import fr.florianpal.fperk.skills.BuiltinSkillRegistrar;
import net.luckperms.api.LuckPerms;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
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

    private SkillRegistry skillRegistry;

    private SkillService skillService;

    private LuckPerms luckPerms;

    private Metrics metrics;

    @Override
    public void onEnable() {

        metrics = new Metrics(this, 24472);

        taskChainFactory = BukkitTaskChainFactory.create(this);

        configurationManager = new ConfigurationManager(this);

        File languageFile = new File(getDataFolder(), "lang_" + configurationManager.getGlobalConfig().getLang() + ".yml");
        createDefaultConfiguration(languageFile, "lang_" + configurationManager.getGlobalConfig().getLang() + ".yml");

        commandManager = new CommandManager(this);
        commandManager.registerDependency(ConfigurationManager.class, configurationManager);

        vaultIntegrationManager = new VaultIntegrationManager(this);

        try {
            databaseManager = new DatabaseManager(this);
        } catch (SQLException e) {
            getLogger().severe(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }

        playerPerkQueries = new PlayerPerkQueries(this);

        databaseManager.addRepository(playerPerkQueries);
        databaseManager.initializeTables();

        playerPerkCommandManager = new PlayerPerkCommandManager(this);

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }

        // The registry has to exist before anything can apply a skill, and before the addons that
        // depend on FPerk are enabled.
        skillRegistry = new SkillRegistry(this);
        skillService = new SkillService(this);
        BuiltinSkillRegistrar.registerAll(this);

        commandManager.registerCommand(new PerkCommand(this));

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new LoadDataScheduler(this));

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FPlaceholderExpansion(this).register();
        } else {
            Bukkit.getLogger().severe("Error : PlaceholderAPI not found !");
        }

        initChart();
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private void initChart() {
        metrics.addCustomChart(new AdvancedPie("player_per_country", () -> {
            Map<String, Integer> valueMap = new HashMap<>();
            valueMap.put(TimeZone.getDefault().getID(), Bukkit.getServer().getOnlinePlayers().size());
            return valueMap;
        }));
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

    /**
     * Where an addon declares its own skills. Call it from your {@code onEnable()} :
     * <pre>
     * FPerk fperk = (FPerk) getServer().getPluginManager().getPlugin("FPerk");
     * fperk.getSkillRegistry().register(this, new MySkill());
     * </pre>
     *
     * @see fr.florianpal.fperk.api.SkillHandler
     */
    public SkillRegistry getSkillRegistry() {
        return skillRegistry;
    }

    /**
     * Drives when the skills of a perk are turned on, off, or applied again.
     */
    public SkillService getSkillService() {
        return skillService;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
}