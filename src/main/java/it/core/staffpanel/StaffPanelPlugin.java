package it.core.staffpanel;

import it.core.staffpanel.commands.*;
import it.core.staffpanel.listeners.*;
import it.core.staffpanel.managers.*;
import it.core.staffpanel.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class StaffPanelPlugin extends JavaPlugin {

    private static StaffPanelPlugin instance;
    
    private ConfigManager configManager;
    private MessageManager messageManager;
    private BanManager banManager;
    private MuteManager muteManager;
    private WarnManager warnManager;
    private FreezeManager freezeManager;
    private ReportManager reportManager;
    private VanishManager vanishManager;
    private LogManager logManager;
    private DiscordWebhook discordWebhook;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;
        
        long startTime = System.currentTimeMillis();
        
        sendStartupMessage();
        
        createDataFolder();
        
        initManagers();
        
        registerCommands();
        
        registerListeners();
        
        long endTime = System.currentTimeMillis();
        
        getLogger().info("StaffPanele enabled in " + (endTime - startTime) + "ms!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        
        if (logManager != null) {
            logManager.saveAll();
        }
        
        getLogger().info("StaffPanele disabled!");
    }

    private void sendStartupMessage() {
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("§c  ____  _         __  __ ____                   _      ");
        Bukkit.getConsoleSender().sendMessage("§c / ___|| |_ __ _ / _|/ _|  _ \\ __ _ _ __   ___| | ___ ");
        Bukkit.getConsoleSender().sendMessage("§c \\___ \\| __/ _` | |_| |_| |_) / _` | '_ \\ / _ \\ |/ _ \\");
        Bukkit.getConsoleSender().sendMessage("§c  ___) | || (_| |  _|  _|  __/ (_| | | | |  __/ |  __/");
        Bukkit.getConsoleSender().sendMessage("§c |____/ \\__\\__,_|_| |_| |_|   \\__,_|_| |_|\\___|_|\\___|");
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("§7Loading §cStaffPanele §7v" + getDescription().getVersion() + "...");
        Bukkit.getConsoleSender().sendMessage("");
    }

    private void createDataFolder() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        File logsFolder = new File(getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
        
        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    private void initManagers() {
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        dataManager = new DataManager(this);
        logManager = new LogManager(this);
        discordWebhook = new DiscordWebhook(this);
        
        banManager = new BanManager(this);
        muteManager = new MuteManager(this);
        warnManager = new WarnManager(this);
        freezeManager = new FreezeManager(this);
        reportManager = new ReportManager(this);
        vanishManager = new VanishManager(this);
    }

    private void registerCommands() {
        StaffPanelCommand mainCommand = new StaffPanelCommand(this);
        getCommand("staffpanel").setExecutor(mainCommand);
        getCommand("staffpanel").setTabCompleter(new StaffPanelTabCompleter(this));
        
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("report").setTabCompleter(new ReportTabCompleter(this));
        
        getCommand("reports").setExecutor(new ReportsCommand(this));
        
        getCommand("discord").setExecutor(new DiscordCommand(this));
        
        getCommand("invsee").setExecutor(new InvseeCommand(this));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerCommandListener(this), this);
    }

    public void reload() {
        configManager.reload();
        messageManager.reload();
        discordWebhook.reload();
    }

    public static StaffPanelPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public MuteManager getMuteManager() {
        return muteManager;
    }

    public WarnManager getWarnManager() {
        return warnManager;
    }

    public FreezeManager getFreezeManager() {
        return freezeManager;
    }

    public ReportManager getReportManager() {
        return reportManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
