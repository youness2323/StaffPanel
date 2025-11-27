package it.core.staffpanel.listeners;

import it.core.staffpanel.StaffPanelPlugin;
import it.core.staffpanel.managers.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerChatListener implements Listener {

    private final StaffPanelPlugin plugin;
    private final Map<UUID, Long> lastMessage = new HashMap<>();

    public PlayerChatListener(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getMuteManager().isMuted(player)) {
            event.setCancelled(true);
            String remaining = plugin.getMuteManager().getRemainingTime(player);
            plugin.getMessageManager().send(player, "mute.try-speak",
                    MessageManager.placeholders().remaining(remaining).build());
            return;
        }
        
        if (plugin.getConfigManager().isSlowchatEnabled()) {
            if (player.hasPermission("staffpanel.bypass.slowchat")) {
                return;
            }
            
            UUID uuid = player.getUniqueId();
            Long last = lastMessage.get(uuid);
            int delay = plugin.getConfigManager().getSlowchatDelay() * 1000;
            
            if (last != null && System.currentTimeMillis() - last < delay) {
                event.setCancelled(true);
                int remaining = (int) ((delay - (System.currentTimeMillis() - last)) / 1000) + 1;
                plugin.getMessageManager().send(player, "slowchat.wait",
                        MessageManager.placeholders().remaining(String.valueOf(remaining)).build());
                return;
            }
            
            lastMessage.put(uuid, System.currentTimeMillis());
        }
    }
}
