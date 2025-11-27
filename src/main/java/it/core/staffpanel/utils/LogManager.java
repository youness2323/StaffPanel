package it.core.staffpanel.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.core.staffpanel.StaffPanelPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogManager {

    private final StaffPanelPlugin plugin;
    private final Gson gson;
    private final File logsFolder;
    private final List<LogEntry> logs = new ArrayList<>();
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd");

    public LogManager(StaffPanelPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.logsFolder = new File(plugin.getDataFolder(), "logs");
        this.dateFormat = new SimpleDateFormat(plugin.getConfigManager().getDateFormat());
        
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
        
        loadTodayLogs();
    }

    private void loadTodayLogs() {
        File todayFile = getTodayLogFile();
        
        if (!todayFile.exists()) {
            return;
        }
        
        try (Reader reader = new FileReader(todayFile)) {
            Type type = new TypeToken<List<LogEntry>>(){}.getType();
            List<LogEntry> loadedLogs = gson.fromJson(reader, type);
            if (loadedLogs != null) {
                logs.addAll(loadedLogs);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load today's logs");
        }
    }

    private File getTodayLogFile() {
        String fileName = "log_" + fileFormat.format(new Date()) + ".json";
        return new File(logsFolder, fileName);
    }

    public void log(String action, String staff, String target, String reason, String extra) {
        if (!plugin.getConfigManager().isLogToFile()) {
            return;
        }
        
        LogEntry entry = new LogEntry(
                action,
                staff,
                target,
                reason,
                extra,
                System.currentTimeMillis()
        );
        
        logs.add(entry);
        saveAll();
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info(String.format("[LOG] %s: %s -> %s (%s)", action, staff, target, reason));
        }
    }

    public void saveAll() {
        File todayFile = getTodayLogFile();
        
        try (Writer writer = new FileWriter(todayFile)) {
            gson.toJson(logs, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save logs");
        }
    }

    public List<LogEntry> getLogs() {
        return new ArrayList<>(logs);
    }

    public List<LogEntry> getLogsByAction(String action) {
        List<LogEntry> filtered = new ArrayList<>();
        for (LogEntry entry : logs) {
            if (entry.getAction().equalsIgnoreCase(action)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public List<LogEntry> getLogsByStaff(String staff) {
        List<LogEntry> filtered = new ArrayList<>();
        for (LogEntry entry : logs) {
            if (entry.getStaff().equalsIgnoreCase(staff)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public List<LogEntry> getLogsByTarget(String target) {
        List<LogEntry> filtered = new ArrayList<>();
        for (LogEntry entry : logs) {
            if (entry.getTarget().equalsIgnoreCase(target)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    public static class LogEntry {
        private final String action;
        private final String staff;
        private final String target;
        private final String reason;
        private final String extra;
        private final long timestamp;

        public LogEntry(String action, String staff, String target, String reason, String extra, long timestamp) {
            this.action = action;
            this.staff = staff;
            this.target = target;
            this.reason = reason;
            this.extra = extra;
            this.timestamp = timestamp;
        }

        public String getAction() {
            return action;
        }

        public String getStaff() {
            return staff;
        }

        public String getTarget() {
            return target;
        }

        public String getReason() {
            return reason;
        }

        public String getExtra() {
            return extra;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
