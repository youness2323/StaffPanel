package it.core.staffpanel.managers;

import it.core.staffpanel.StaffPanelPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final StaffPanelPlugin plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public MessageManager(StaffPanelPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reload() {
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        String message = messages.getString(path);
        if (message == null) {
            return ChatColor.RED + "Message not found: " + path;
        }
        return colorize(message);
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public String getMessageWithPrefix(String path) {
        return colorize(plugin.getConfigManager().getPrefix()) + getMessage(path);
    }

    public String getMessageWithPrefix(String path, Map<String, String> placeholders) {
        return colorize(plugin.getConfigManager().getPrefix()) + getMessage(path, placeholders);
    }

    public void send(CommandSender sender, String path) {
        sender.sendMessage(getMessageWithPrefix(path));
    }

    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(getMessageWithPrefix(path, placeholders));
    }

    public void sendRaw(CommandSender sender, String path) {
        sender.sendMessage(getMessage(path));
    }

    public void sendRaw(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(getMessage(path, placeholders));
    }

    public void broadcast(String path) {
        String message = getMessageWithPrefix(path);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public void broadcast(String path, Map<String, String> placeholders) {
        String message = getMessageWithPrefix(path, placeholders);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public void broadcastStaff(String path, String permission) {
        String message = getMessageWithPrefix(path);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(message);
            }
        }
    }

    public void broadcastStaff(String path, String permission, Map<String, String> placeholders) {
        String message = getMessageWithPrefix(path, placeholders);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(message);
            }
        }
    }

    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static Map<String, String> placeholder(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public static class PlaceholderBuilder {
        private final Map<String, String> placeholders = new HashMap<>();

        public PlaceholderBuilder add(String key, String value) {
            placeholders.put(key, value != null ? value : "");
            return this;
        }

        public PlaceholderBuilder player(String name) {
            return add("player", name);
        }

        public PlaceholderBuilder staff(String name) {
            return add("staff", name);
        }

        public PlaceholderBuilder reason(String reason) {
            return add("reason", reason);
        }

        public PlaceholderBuilder duration(String duration) {
            return add("duration", duration);
        }

        public PlaceholderBuilder time(String time) {
            return add("time", time);
        }

        public PlaceholderBuilder remaining(String remaining) {
            return add("remaining", remaining);
        }

        public PlaceholderBuilder current(int current) {
            return add("current", String.valueOf(current));
        }

        public PlaceholderBuilder max(int max) {
            return add("max", String.valueOf(max));
        }

        public PlaceholderBuilder type(String type) {
            return add("type", type);
        }

        public PlaceholderBuilder date(String date) {
            return add("date", date);
        }

        public PlaceholderBuilder id(int id) {
            return add("id", String.valueOf(id));
        }

        public PlaceholderBuilder page(int page) {
            return add("page", String.valueOf(page));
        }

        public PlaceholderBuilder maxpage(int maxpage) {
            return add("maxpage", String.valueOf(maxpage));
        }

        public PlaceholderBuilder gamemode(String gamemode) {
            return add("gamemode", gamemode);
        }

        public PlaceholderBuilder ip(String ip) {
            return add("ip", ip);
        }

        public PlaceholderBuilder alt(String alt) {
            return add("alt", alt);
        }

        public PlaceholderBuilder delay(int delay) {
            return add("delay", String.valueOf(delay));
        }

        public PlaceholderBuilder discordLink(String link) {
            return add("discord-link", link);
        }

        public PlaceholderBuilder usage(String usage) {
            return add("usage", usage);
        }

        public PlaceholderBuilder types(String types) {
            return add("types", types);
        }

        public PlaceholderBuilder action(String action) {
            return add("action", action);
        }

        public PlaceholderBuilder cause(String cause) {
            return add("cause", cause);
        }

        public PlaceholderBuilder location(String location) {
            return add("location", location);
        }

        public PlaceholderBuilder command(String command) {
            return add("command", command);
        }

        public Map<String, String> build() {
            return placeholders;
        }
    }

    public static PlaceholderBuilder placeholders() {
        return new PlaceholderBuilder();
    }
}
