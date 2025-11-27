package it.core.staffpanel.managers;

import it.core.staffpanel.StaffPanelPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class WarnManager {

    private final StaffPanelPlugin plugin;
    private final Map<UUID, List<WarnData>> warns = new HashMap<>();

    public WarnManager(StaffPanelPlugin plugin) {
        this.plugin = plugin;
        loadWarns();
    }

    private void loadWarns() {
        if (plugin.getDataManager() != null) {
            Map<String, Object> warnsData = plugin.getDataManager().getData("warns");
            if (warnsData != null) {
                for (Map.Entry<String, Object> entry : warnsData.entrySet()) {
                    try {
                        UUID uuid = UUID.fromString(entry.getKey());
                        if (entry.getValue() instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> warnsList = (List<Map<String, Object>>) entry.getValue();
                            List<WarnData> playerWarns = new ArrayList<>();
                            
                            for (Map<String, Object> warnData : warnsList) {
                                String reason = (String) warnData.get("reason");
                                String staff = (String) warnData.get("staff");
                                long date = ((Number) warnData.get("date")).longValue();
                                playerWarns.add(new WarnData(reason, staff, date));
                            }
                            
                            warns.put(uuid, playerWarns);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load warns for: " + entry.getKey());
                    }
                }
            }
        }
    }

    public void saveWarns() {
        Map<String, Object> warnsData = new HashMap<>();
        for (Map.Entry<UUID, List<WarnData>> entry : warns.entrySet()) {
            List<Map<String, Object>> warnsList = new ArrayList<>();
            for (WarnData warn : entry.getValue()) {
                Map<String, Object> warnData = new HashMap<>();
                warnData.put("reason", warn.getReason());
                warnData.put("staff", warn.getStaff());
                warnData.put("date", warn.getDate());
                warnsList.add(warnData);
            }
            warnsData.put(entry.getKey().toString(), warnsList);
        }
        plugin.getDataManager().setData("warns", warnsData);
    }

    public void warn(OfflinePlayer target, String staff, String reason) {
        UUID uuid = target.getUniqueId();
        
        List<WarnData> playerWarns = warns.computeIfAbsent(uuid, k -> new ArrayList<>());
        playerWarns.add(new WarnData(reason, staff, System.currentTimeMillis()));
        
        int current = playerWarns.size();
        int max = plugin.getConfigManager().getMaxWarns();
        
        if (target.isOnline()) {
            Player player = target.getPlayer();
            if (player != null) {
                plugin.getMessageManager().send(player, "warn.target-message",
                        MessageManager.placeholders()
                                .staff(staff)
                                .reason(reason)
                                .current(current)
                                .max(max)
                                .build());
            }
        }
        
        saveWarns();
        
        if (plugin.getConfigManager().isCommandLogged("warn")) {
            plugin.getLogManager().log("warn", staff, target.getName(), reason, current + "/" + max);
        }
        
        plugin.getDiscordWebhook().sendWarn(target.getName(), staff, reason, current, max);
        
        if (current >= max) {
            executeMaxWarnsAction(target, staff);
        }
    }

    private void executeMaxWarnsAction(OfflinePlayer target, String staff) {
        String action = plugin.getConfigManager().getMaxWarnsAction();
        
        if (plugin.getConfigManager().shouldNotifyStaff("warn")) {
            plugin.getMessageManager().broadcastStaff("warn.max-reached", "staffpanel.warns",
                    MessageManager.placeholders()
                            .player(target.getName())
                            .build());
        }
        
        switch (action.toLowerCase()) {
            case "ban":
                plugin.getBanManager().ban(target, "Console", "Troppi avvisi", -1);
                break;
            case "tempban":
                plugin.getBanManager().ban(target, "Console", "Troppi avvisi", 86400000L);
                break;
            case "kick":
                if (target.isOnline()) {
                    Player player = target.getPlayer();
                    if (player != null) {
                        player.kickPlayer(MessageManager.colorize("&cSei stato kickato per troppi avvisi!"));
                    }
                }
                break;
        }
        
        clearWarns(target, "Console");
    }

    public List<WarnData> getWarns(OfflinePlayer target) {
        return warns.getOrDefault(target.getUniqueId(), new ArrayList<>());
    }

    public int getWarnCount(OfflinePlayer target) {
        return getWarns(target).size();
    }

    public void clearWarns(OfflinePlayer target, String staff) {
        warns.remove(target.getUniqueId());
        saveWarns();
        
        if (plugin.getConfigManager().isCommandLogged("clearwarns")) {
            plugin.getLogManager().log("clearwarns", staff, target.getName(), "", "");
        }
    }

    public String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(plugin.getConfigManager().getDateFormat());
        return sdf.format(new Date(timestamp));
    }

    public static class WarnData {
        private final String reason;
        private final String staff;
        private final long date;

        public WarnData(String reason, String staff, long date) {
            this.reason = reason;
            this.staff = staff;
            this.date = date;
        }

        public String getReason() {
            return reason;
        }

        public String getStaff() {
            return staff;
        }

        public long getDate() {
            return date;
        }
    }
}
