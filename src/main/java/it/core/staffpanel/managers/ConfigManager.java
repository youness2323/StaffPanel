package it.core.staffpanel.managers;

import it.core.staffpanel.StaffPanelPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {

    private final StaffPanelPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(StaffPanelPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml!");
            e.printStackTrace();
        }
    }

    public String getPrefix() {
        return config.getString("prefix", "&8[&c&lStaffPanel&8] ");
    }

    public String getDiscordLink() {
        return config.getString("discord-link", "discord.gg/tuoserver");
    }

    public boolean isCommandEnabled(String command) {
        return config.getBoolean("commands." + command + ".enabled", true);
    }

    public String getCommandPermission(String command) {
        return config.getString("commands." + command + ".permission", "staffpanel." + command);
    }

    public boolean isCommandLogged(String command) {
        return config.getBoolean("commands." + command + ".log", true);
    }

    public boolean shouldNotifyStaff(String command) {
        return config.getBoolean("commands." + command + ".notify-staff", true);
    }

    public int getMaxWarns() {
        return config.getInt("commands.warn.max-warns", 5);
    }

    public String getMaxWarnsAction() {
        return config.getString("commands.warn.action-on-max", "ban");
    }

    public String getDefaultMuteDuration() {
        return config.getString("commands.mute.default-duration", "1h");
    }

    public int getWildMaxDistance() {
        return config.getInt("commands.wild.max-distance", 5000);
    }

    public int getWildMinDistance() {
        return config.getInt("commands.wild.min-distance", 500);
    }

    public int getWildCooldown() {
        return config.getInt("commands.wild.cooldown", 60);
    }

    public int getClearChatLines() {
        return config.getInt("commands.clearchat.lines", 100);
    }

    public int getDefaultSlowchatDelay() {
        return config.getInt("commands.slowchat.default-delay", 5);
    }

    public int getReportCooldown() {
        return config.getInt("commands.report.cooldown", 60);
    }

    public List<String> getReportTypes() {
        return config.getStringList("report-types");
    }

    public boolean isSlowchatEnabled() {
        return config.getBoolean("slowchat.enabled", false);
    }

    public void setSlowchatEnabled(boolean enabled) {
        config.set("slowchat.enabled", enabled);
        save();
    }

    public int getSlowchatDelay() {
        return config.getInt("slowchat.delay", 5);
    }

    public void setSlowchatDelay(int delay) {
        config.set("slowchat.delay", delay);
        save();
    }

    public boolean hideVanishJoinMessage() {
        return config.getBoolean("vanish.hide-join-message", true);
    }

    public boolean hideVanishQuitMessage() {
        return config.getBoolean("vanish.hide-quit-message", true);
    }

    public boolean showFakeQuitMessage() {
        return config.getBoolean("vanish.fake-quit-message", true);
    }

    public List<String> getSafeBlocks() {
        return config.getStringList("wild.safe-blocks");
    }

    public List<String> getUnsafeBlocks() {
        return config.getStringList("wild.unsafe-blocks");
    }

    public boolean isDebugMode() {
        return config.getBoolean("settings.debug", false);
    }

    public boolean isLogToFile() {
        return config.getBoolean("settings.log-to-file", true);
    }

    public String getDateFormat() {
        return config.getString("settings.date-format", "dd/MM/yyyy HH:mm:ss");
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
