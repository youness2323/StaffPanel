package it.core.staffpanel.listeners;

import it.core.staffpanel.StaffPanelPlugin;
import it.core.staffpanel.managers.BanManager;
import it.core.staffpanel.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuitListener implements Listener {

    private final StaffPanelPlugin plugin;

    public PlayerJoinQuitListener(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (plugin.getBanManager().isBanned(player)) {
            BanManager.BanData banData = plugin.getBanManager().getBanData(player);
            if (banData != null) {
                String duration = banData.isPermanent() ? "Permanente" :
                        it.core.staffpanel.utils.TimeUtils.formatDuration(banData.getRemainingTime());

                String kickMessage = plugin.getMessageManager().getMessage("ban.kick-message",
                        MessageManager.placeholders()
                                .reason(banData.getReason())
                                .staff(banData.getStaff())
                                .duration(duration)
                                .build());

                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMessage);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        String ip = player.getAddress().getAddress().getHostAddress();
        plugin.getDataManager().savePlayerIP(player.getName(), ip);

        for (Player vanished : Bukkit.getOnlinePlayers()) {
            if (plugin.getVanishManager().isVanished(vanished)) {
                plugin.getVanishManager().hideFromPlayer(vanished, player);
            }
        }

        if (plugin.getVanishManager().isVanished(player)) {
            if (plugin.getConfigManager().hideVanishJoinMessage()) {
                event.setJoinMessage(null);
            }

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("staffpanel.vanish.see")) {
                    online.hidePlayer(plugin, player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getFreezeManager().isFrozen(player)) {
            plugin.getMessageManager().broadcastStaff("freeze.try-quit", "staffpanel.freeze",
                    MessageManager.placeholders().player(player.getName()).build());
        }

        plugin.getFreezeManager().removeFrozen(player.getUniqueId());

        if (plugin.getVanishManager().isVanished(player)) {
            event.setQuitMessage(null);
            plugin.getVanishManager().removeVanished(player.getUniqueId());
        }
    }
}