package it.core.staffpanel.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.core.staffpanel.StaffPanelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiscordWebhook {

    private final StaffPanelPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public DiscordWebhook(StaffPanelPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "discord-webhook.yml");

        if (!configFile.exists()) {
            plugin.saveResource("discord-webhook.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    private boolean isEventEnabled(String event) {
        return config.getBoolean("webhooks." + event + ".enabled", true);
    }

    private String getWebhookUrl(String event) {
        return config.getString("webhooks." + event + ".url", "");
    }

    private String getTitle(String event) {
        return config.getString("webhooks." + event + ".title", event.toUpperCase());
    }

    private String getColor(String event) {
        String colorHex = config.getString("embed-settings.color-" + event, "#FF0000");
        return colorHex.replace("#", "");
    }

    private int getColorInt(String event) {
        try {
            return Integer.parseInt(getColor(event), 16);
        } catch (NumberFormatException e) {
            return 16711680;
        }
    }

    private String getFooterText() {
        return config.getString("embed-settings.footer-text", "© developed by Youness | StaffPanel");
    }

    private String getFooterIcon() {
        return config.getString("embed-settings.footer-icon", "");
    }

    private String getThumbnail() {
        return config.getString("embed-settings.thumbnail", "");
    }

    private String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(plugin.getConfigManager().getDateFormat());
        return sdf.format(new Date());
    }

    private void sendWebhook(String event, String description) {
        if (!isEnabled() || !isEventEnabled(event)) {
            return;
        }

        String webhookUrl = getWebhookUrl(event);
        if (webhookUrl.isEmpty() || webhookUrl.contains("YOUR_WEBHOOK_URL")) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                JsonObject json = new JsonObject();
                JsonArray embeds = new JsonArray();
                JsonObject embed = new JsonObject();

                embed.addProperty("title", getTitle(event));
                embed.addProperty("description", description);
                embed.addProperty("color", getColorInt(event));
                embed.addProperty("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));

                JsonObject footer = new JsonObject();
                footer.addProperty("text", getFooterText());
                String footerIcon = getFooterIcon();
                if (!footerIcon.isEmpty()) {
                    footer.addProperty("icon_url", footerIcon);
                }
                embed.add("footer", footer);

                String thumbnail = getThumbnail();
                if (!thumbnail.isEmpty()) {
                    JsonObject thumbObj = new JsonObject();
                    thumbObj.addProperty("url", thumbnail);
                    embed.add("thumbnail", thumbObj);
                }

                embeds.add(embed);
                json.add("embeds", embeds);

                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode != 200 && responseCode != 204) {
                    plugin.getLogger().warning("Discord webhook failed for " + event + ": " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
                }
            }
        });
    }

    public void sendBan(String player, String staff, String reason, String duration) {
        String message = String.format(
                "**Giocatore:** %s\n**Staff:** %s\n**Motivo:** %s\n**Durata:** %s\n**Data:** %s",
                player, staff, reason, duration, getFormattedDate()
        );
        sendWebhook("ban", message);
    }

    public void sendUnban(String player, String staff) {
        String message = String.format(
                "**Giocatore:** %s\n**Staff:** %s\n**Data:** %s",
                player, staff, getFormattedDate()
        );
        sendWebhook("unban", message);
    }

    public void sendMute(String player, String staff, String duration) {
        String message = String.format(
                "**Giocatore:** %s\n**Staff:** %s\n**Durata:** %s\n**Data:** %s",
                player, staff, duration, getFormattedDate()
        );
        sendWebhook("mute", message);
    }

    public void sendUnmute(String player, String staff) {
        String message = String.format(
                "**Giocatore:** %s\n**Staff:** %s\n**Data:** %s",
                player, staff, getFormattedDate()
        );
        sendWebhook("unmute", message);
    }

    public void sendKick(String player, String staff, String reason) {
        String message = String.format(
                "**Giocatore:** %s\n**Staff:** %s\n**Motivo:** %s\n**Data:** %s",
                player, staff, reason, getFormattedDate()
        );
        sendWebhook("kick", message);
    }

    public void sendWarn(String player, String staff, String reason, int current, int max) {
        String message = String.format(
                "**Giocatore:** %s\n**Staff:** %s\n**Motivo:** %s\n**Avvisi:** %d/%d\n**Data:** %s",
                player, staff, reason, current, max, getFormattedDate()
        );
        sendWebhook("warn", message);
    }

    public void sendFreeze(String player, String staff, String action) {
        String message = String.format(
                "**Giocatore:** %s\n**Staff:** %s\n**Azione:** %s\n**Data:** %s",
                player, staff, action, getFormattedDate()
        );
        sendWebhook("freeze", message);
    }

    public void sendReport(String player, String type, String reason) {
        String message = String.format(
                "**Reporter:** %s\n**Tipologia:** %s\n**Motivo:** %s\n**Data:** %s",
                player, type, reason, getFormattedDate()
        );
        sendWebhook("report", message);
    }

    public void sendDeath(String player, String cause, String location) {
        String message = String.format(
                "**Giocatore:** %s\n**Causa:** %s\n**Posizione:** %s\n**Data:** %s",
                player, cause, location, getFormattedDate()
        );
        sendWebhook("death", message);
    }

    public void sendStaffCommand(String staff, String command) {
        String message = String.format(
                "**Staff:** %s\n**Comando:** %s\n**Data:** %s",
                staff, command, getFormattedDate()
        );
        sendWebhook("staffcommand", message);
    }
}