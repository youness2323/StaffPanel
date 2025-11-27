package it.core.staffpanel.managers;

import it.core.staffpanel.StaffPanelPlugin;
import it.core.staffpanel.utils.TimeUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MuteManager {

    private final StaffPanelPlugin plugin;
    private final Map<UUID, MuteData> mutes = new HashMap<>();

    public MuteManager(StaffPanelPlugin plugin) {
        this.plugin = plugin;
        loadMutes();
    }

    private void loadMutes() {
        if (plugin.getDataManager() != null) {
            Map<String, Object> mutesData = plugin.getDataManager().getData("mutes");
            if (mutesData != null) {
                for (Map.Entry<String, Object> entry : mutesData.entrySet()) {
                    try {
                        UUID uuid = UUID.fromString(entry.getKey());
                        if (entry.getValue() instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> data = (Map<String, Object>) entry.getValue();
                            String staff = (String) data.get("staff");
                            long expiry = ((Number) data.get("expiry")).longValue();
                            long created = ((Number) data.get("created")).longValue();
                            
                            if (expiry > System.currentTimeMillis()) {
                                mutes.put(uuid, new MuteData(staff, expiry, created));
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load mute for: " + entry.getKey());
                    }
                }
            }
        }
    }

    public void saveMutes() {
        Map<String, Object> mutesData = new HashMap<>();
        for (Map.Entry<UUID, MuteData> entry : mutes.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            data.put("staff", entry.getValue().getStaff());
            data.put("expiry", entry.getValue().getExpiry());
            data.put("created", entry.getValue().getCreated());
            mutesData.put(entry.getKey().toString(), data);
        }
        plugin.getDataManager().setData("mutes", mutesData);
    }

    public void mute(OfflinePlayer target, String staff, long duration) {
        UUID uuid = target.getUniqueId();
        long expiry = System.currentTimeMillis() + duration;
        
        mutes.put(uuid, new MuteData(staff, expiry, System.currentTimeMillis()));
        
        if (target.isOnline()) {
            Player player = target.getPlayer();
            if (player != null) {
                plugin.getMessageManager().send(player, "mute.target-message",
                        MessageManager.placeholders()
                                .duration(TimeUtils.formatDuration(duration))
                                .staff(staff)
                                .build());
            }
        }
        
        saveMutes();
        
        if (plugin.getConfigManager().isCommandLogged("mute")) {
            plugin.getLogManager().log("mute", staff, target.getName(), "", TimeUtils.formatDuration(duration));
        }
        
        plugin.getDiscordWebhook().sendMute(target.getName(), staff, TimeUtils.formatDuration(duration));
    }

    public void unmute(OfflinePlayer target, String staff) {
        UUID uuid = target.getUniqueId();
        mutes.remove(uuid);
        
        if (target.isOnline()) {
            Player player = target.getPlayer();
            if (player != null) {
                plugin.getMessageManager().send(player, "mute.unmute-target",
                        MessageManager.placeholders()
                                .staff(staff)
                                .build());
            }
        }
        
        saveMutes();
        
        if (plugin.getConfigManager().isCommandLogged("unmute")) {
            plugin.getLogManager().log("unmute", staff, target.getName(), "", "");
        }
        
        plugin.getDiscordWebhook().sendUnmute(target.getName(), staff);
    }

    public boolean isMuted(OfflinePlayer target) {
        UUID uuid = target.getUniqueId();
        
        if (mutes.containsKey(uuid)) {
            MuteData data = mutes.get(uuid);
            if (data.getExpiry() < System.currentTimeMillis()) {
                mutes.remove(uuid);
                saveMutes();
                return false;
            }
            return true;
        }
        
        return false;
    }

    public MuteData getMuteData(OfflinePlayer target) {
        return mutes.get(target.getUniqueId());
    }

    public String getRemainingTime(OfflinePlayer target) {
        MuteData data = getMuteData(target);
        if (data == null) return "0s";
        return TimeUtils.formatDuration(data.getRemainingTime());
    }

    public static class MuteData {
        private final String staff;
        private final long expiry;
        private final long created;

        public MuteData(String staff, long expiry, long created) {
            this.staff = staff;
            this.expiry = expiry;
            this.created = created;
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

        public long getRemainingTime() {
            return Math.max(0, expiry - System.currentTimeMillis());
        }
    }
}
