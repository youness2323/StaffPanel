package it.core.staffpanel.listeners;

import it.core.staffpanel.StaffPanelPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final StaffPanelPlugin plugin;

    public PlayerDeathListener(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage();
        Location location = player.getLocation();
        
        String locationStr = String.format("X: %d, Y: %d, Z: %d (%s)",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getWorld().getName()
        );
        
        String cause = "Sconosciuta";
        if (player.getLastDamageCause() != null) {
            cause = player.getLastDamageCause().getCause().name();
        }
        
        plugin.getDiscordWebhook().sendDeath(player.getName(), cause, locationStr);
    }
}
