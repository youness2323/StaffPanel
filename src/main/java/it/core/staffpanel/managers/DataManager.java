package it.core.staffpanel.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.core.staffpanel.StaffPanelPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DataManager {

    private final StaffPanelPlugin plugin;
    private final Gson gson;
    private final File dataFolder;
    private final Map<String, Map<String, Object>> dataCache = new HashMap<>();

    public DataManager(StaffPanelPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        loadAllData();
    }

    private void loadAllData() {
        loadData("bans");
        loadData("mutes");
        loadData("warns");
        loadData("reports");
        loadData("players");
    }

    private void loadData(String name) {
        File file = new File(dataFolder, name + ".json");
        
        if (!file.exists()) {
            dataCache.put(name, new HashMap<>());
            return;
        }
        
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> data = gson.fromJson(reader, type);
            dataCache.put(name, data != null ? data : new HashMap<>());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load " + name + ".json");
            dataCache.put(name, new HashMap<>());
        }
    }

    public void saveData(String name) {
        File file = new File(dataFolder, name + ".json");
        
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(dataCache.getOrDefault(name, new HashMap<>()), writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save " + name + ".json");
        }
    }

    public void saveAll() {
        for (String name : dataCache.keySet()) {
            saveData(name);
        }
    }

    public Map<String, Object> getData(String name) {
        return dataCache.getOrDefault(name, new HashMap<>());
    }

    public void setData(String name, Map<String, Object> data) {
        dataCache.put(name, data);
        saveData(name);
    }

    public void setData(String name, String key, Object value) {
        Map<String, Object> data = dataCache.computeIfAbsent(name, k -> new HashMap<>());
        data.put(key, value);
        saveData(name);
    }

    public Object getData(String name, String key) {
        return getData(name).get(key);
    }

    public void removeData(String name, String key) {
        Map<String, Object> data = dataCache.get(name);
        if (data != null) {
            data.remove(key);
            saveData(name);
        }
    }

    public void savePlayerIP(String playerName, String ip) {
        Map<String, Object> players = getData("players");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> playerData = (Map<String, Object>) players.computeIfAbsent(playerName.toLowerCase(), k -> new HashMap<>());
        playerData.put("ip", ip);
        playerData.put("lastSeen", System.currentTimeMillis());
        
        players.put(playerName.toLowerCase(), playerData);
        setData("players", players);
    }

    public String getPlayerIP(String playerName) {
        Map<String, Object> players = getData("players");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> playerData = (Map<String, Object>) players.get(playerName.toLowerCase());
        
        if (playerData != null) {
            return (String) playerData.get("ip");
        }
        
        return null;
    }

    public Map<String, String> getAlternateAccounts(String playerName) {
        Map<String, String> alts = new HashMap<>();
        String targetIP = getPlayerIP(playerName);
        
        if (targetIP == null) {
            return alts;
        }
        
        Map<String, Object> players = getData("players");
        
        for (Map.Entry<String, Object> entry : players.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(playerName)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> playerData = (Map<String, Object>) entry.getValue();
                String ip = (String) playerData.get("ip");
                
                if (targetIP.equals(ip)) {
                    alts.put(entry.getKey(), ip);
                }
            }
        }
        
        return alts;
    }
}
