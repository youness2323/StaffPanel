package it.core.staffpanel.listeners;

import it.core.staffpanel.StaffPanelPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandListener implements Listener {

    private final StaffPanelPlugin plugin;

    public PlayerCommandListener(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        String command = event.getMessage();
        
        if (command.toLowerCase().startsWith("/staffpanel") || 
            command.toLowerCase().startsWith("/sp ") ||
            command.toLowerCase().startsWith("/staff ")) {
            
            if (player.hasPermission("staffpanel.use")) {
                plugin.getDiscordWebhook().sendStaffCommand(player.getName(), command);
            }
        }
        
        if (plugin.getFreezeManager().isFrozen(player)) {
            if (!player.hasPermission("staffpanel.bypass.freeze")) {
                if (!command.toLowerCase().startsWith("/msg") && 
                    !command.toLowerCase().startsWith("/r") &&
                    !command.toLowerCase().startsWith("/reply")) {
                    event.setCancelled(true);
                    plugin.getMessageManager().send(player, "freeze.try-move");
                }
            }
        }
    }
}
