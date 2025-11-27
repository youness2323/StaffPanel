package it.core.staffpanel.managers;

import it.core.staffpanel.StaffPanelPlugin;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReportManager {

    private final StaffPanelPlugin plugin;
    private final List<ReportData> reports = new ArrayList<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private int nextId = 1;

    public ReportManager(StaffPanelPlugin plugin) {
        this.plugin = plugin;
        loadReports();
    }

    private void loadReports() {
        if (plugin.getDataManager() != null) {
            Map<String, Object> reportsData = plugin.getDataManager().getData("reports");
            if (reportsData != null) {
                try {
                    if (reportsData.containsKey("nextId")) {
                        nextId = ((Number) reportsData.get("nextId")).intValue();
                    }
                    
                    if (reportsData.containsKey("list") && reportsData.get("list") instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> reportsList = (List<Map<String, Object>>) reportsData.get("list");
                        
                        for (Map<String, Object> reportData : reportsList) {
                            int id = ((Number) reportData.get("id")).intValue();
                            String player = (String) reportData.get("player");
                            String type = (String) reportData.get("type");
                            String reason = (String) reportData.get("reason");
                            long date = ((Number) reportData.get("date")).longValue();
                            
                            reports.add(new ReportData(id, player, type, reason, date));
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load reports!");
                }
            }
        }
    }

    public void saveReports() {
        Map<String, Object> reportsData = new HashMap<>();
        reportsData.put("nextId", nextId);
        
        List<Map<String, Object>> reportsList = new ArrayList<>();
        for (ReportData report : reports) {
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("id", report.getId());
            reportData.put("player", report.getPlayer());
            reportData.put("type", report.getType());
            reportData.put("reason", report.getReason());
            reportData.put("date", report.getDate());
            reportsList.add(reportData);
        }
        reportsData.put("list", reportsList);
        
        plugin.getDataManager().setData("reports", reportsData);
    }

    public boolean canReport(Player player) {
        if (player.hasPermission("staffpanel.bypass.reportcooldown")) {
            return true;
        }
        
        Long lastReport = cooldowns.get(player.getUniqueId());
        if (lastReport == null) {
            return true;
        }
        
        int cooldown = plugin.getConfigManager().getReportCooldown() * 1000;
        return System.currentTimeMillis() - lastReport >= cooldown;
    }

    public int getRemainingCooldown(Player player) {
        Long lastReport = cooldowns.get(player.getUniqueId());
        if (lastReport == null) {
            return 0;
        }
        
        int cooldown = plugin.getConfigManager().getReportCooldown() * 1000;
        long remaining = (lastReport + cooldown) - System.currentTimeMillis();
        return (int) Math.max(0, remaining / 1000);
    }

    public void createReport(Player player, String type, String reason) {
        ReportData report = new ReportData(nextId++, player.getName(), type, reason, System.currentTimeMillis());
        reports.add(report);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        
        saveReports();
        
        plugin.getMessageManager().broadcastStaff("report.staff-notify", "staffpanel.reports",
                MessageManager.placeholders()
                        .player(player.getName())
                        .type(type)
                        .reason(reason)
                        .build());
        
        if (plugin.getConfigManager().isCommandLogged("report")) {
            plugin.getLogManager().log("report", player.getName(), "", "[" + type + "] " + reason, "");
        }
        
        plugin.getDiscordWebhook().sendReport(player.getName(), type, reason);
    }

    public List<ReportData> getReports() {
        return new ArrayList<>(reports);
    }

    public List<ReportData> getReports(int page, int perPage) {
        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, reports.size());
        
        if (start >= reports.size()) {
            return new ArrayList<>();
        }
        
        return reports.subList(start, end);
    }

    public int getTotalPages(int perPage) {
        return (int) Math.ceil((double) reports.size() / perPage);
    }

    public void deleteReport(int id) {
        reports.removeIf(report -> report.getId() == id);
        saveReports();
    }

    public void clearReports() {
        reports.clear();
        saveReports();
    }

    public boolean isValidType(String type) {
        return plugin.getConfigManager().getReportTypes().contains(type.toUpperCase());
    }

    public String getValidTypes() {
        return String.join(", ", plugin.getConfigManager().getReportTypes());
    }

    public String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(plugin.getConfigManager().getDateFormat());
        return sdf.format(new Date(timestamp));
    }

    public static class ReportData {
        private final int id;
        private final String player;
        private final String type;
        private final String reason;
        private final long date;

        public ReportData(int id, String player, String type, String reason, long date) {
            this.id = id;
            this.player = player;
            this.type = type;
            this.reason = reason;
            this.date = date;
        }

        public int getId() {
            return id;
        }

        public String getPlayer() {
            return player;
        }

        public String getType() {
            return type;
        }

        public String getReason() {
            return reason;
        }

        public long getDate() {
            return date;
        }
    }
}
