package it.core.staffpanel.managers;

import it.core.staffpanel.StaffPanelPlugin;
import it.core.staffpanel.utils.TimeUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanManager {

    private final StaffPanelPlugin plugin;
    private final Map<UUID, BanData> tempBans = new HashMap<>();

    public BanManager(StaffPanelPlugin plugin) {
        this.plugin = plugin;
        loadBans();
    }

    private void loadBans() {
        if (plugin.getDataManager() != null) {
            Map<String, Object> bans = plugin.getDataManager().getData("bans");
            if (bans != null) {
                for (Map.Entry<String, Object> entry : bans.entrySet()) {
                    try {
                        UUID uuid = UUID.fromString(entry.getKey());
                        if (entry.getValue() instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> data = (Map<String, Object>) entry.getValue();
                            String reason = (String) data.get("reason");
                            String staff = (String) data.get("staff");
                            long expiry = ((Number) data.get("expiry")).longValue();
                            long created = ((Number) data.get("created")).longValue();

                            if (expiry > System.currentTimeMillis() || expiry == -1) {
                                tempBans.put(uuid, new BanData(reason, staff, expiry, created));
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load ban for: " + entry.getKey());
                    }
                }
            }
        }
    }

    public void saveBans() {
        Map<String, Object> bans = new HashMap<>();
        for (Map.Entry<UUID, BanData> entry : tempBans.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            data.put("reason", entry.getValue().getReason());
            data.put("staff", entry.getValue().getStaff());
            data.put("expiry", entry.getValue().getExpiry());
            data.put("created", entry.getValue().getCreated());
            bans.put(entry.getKey().toString(), data);
        }
        plugin.getDataManager().setData("bans", bans);
    }

    public void ban(OfflinePlayer target, String staff, String reason) {
        ban(target, staff, reason, -1);
    }

    public void ban(OfflinePlayer target, String staff, String reason, long duration) {
        UUID uuid = target.getUniqueId();
        long expiry = duration == -1 ? -1 : System.currentTimeMillis() + duration;

        tempBans.put(uuid, new BanData(reason, staff, expiry, System.currentTimeMillis()));

        Date expiryDate = expiry == -1 ? null : new Date(expiry);
        Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, expiryDate, staff);

        if (target.isOnline()) {
            Player player = target.getPlayer();
            if (player != null) {
                String kickMessage = plugin.getMessageManager().getMessage("ban.kick-message",
                        MessageManager.placeholders()
                                .reason(reason)
                                .staff(staff)
                                .duration(duration == -1 ? "Permanente" : TimeUtils.formatDuration(duration))
                                .build());
                player.kickPlayer(kickMessage);
            }
        }

        saveBans();

        if (plugin.getConfigManager().isCommandLogged("ban")) {
            plugin.getLogManager().log("ban", staff, target.getName(), reason,
                    duration == -1 ? "Permanente" : TimeUtils.formatDuration(duration));
        }

        plugin.getDiscordWebhook().sendBan(target.getName(), staff, reason,
                duration == -1 ? "Permanente" : TimeUtils.formatDuration(duration));
    }

    public void unban(OfflinePlayer target, String staff) {
        UUID uuid = target.getUniqueId();
        tempBans.remove(uuid);
        saveBans();

        if (plugin.getConfigManager().isCommandLogged("unban")) {
            plugin.getLogManager().log("unban", staff, target.getName(), "", "");
        }

        plugin.getDiscordWebhook().sendUnban(target.getName(), staff);
    }

    public boolean isBanned(OfflinePlayer target) {
        UUID uuid = target.getUniqueId();

        if (tempBans.containsKey(uuid)) {
            BanData data = tempBans.get(uuid);
            if (data.getExpiry() != -1 && data.getExpiry() < System.currentTimeMillis()) {
                tempBans.remove(uuid);
                Bukkit.getBanList(BanList.Type.NAME).pardon(target.getName());
                saveBans();
                return false;
            }
            return true;
        }

        return Bukkit.getBanList(BanList.Type.NAME).isBanned(target.getName());
    }

    public BanData getBanData(OfflinePlayer target) {
        return tempBans.get(target.getUniqueId());
    }

    public static class BanData {
        private final String reason;
        private final String staff;
        private final long expiry;
        private final long created;

        public BanData(String reason, String staff, long expiry, long created) {
            this.reason = reason;
            this.staff = staff;
            this.expiry = expiry;
            this.created = created;
        }

        public String getReason() {
            return reason;
        }

        public String getStaff() {
            return staff;
        }

        public long getExpiry() {
            return expiry;
        }

        public long getCreated() {
            return created;
        }

        public boolean isPermanent() {
            return expiry == -1;
        }

        public long getRemainingTime() {
            if (expiry == -1) return -1;
            return Math.max(0, expiry - System.currentTimeMillis());
        }
    }
}