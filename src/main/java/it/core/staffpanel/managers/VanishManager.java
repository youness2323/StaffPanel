package it.core.staffpanel.managers;

import it.core.staffpanel.StaffPanelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final StaffPanelPlugin plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishManager(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    public void vanish(Player player) {
        UUID uuid = player.getUniqueId();
        vanishedPlayers.add(uuid);
        
        player.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        
        if (plugin.getConfigManager().showFakeQuitMessage()) {
            String fakeQuit = plugin.getMessageManager().getMessage("vanish.fake-quit",
                    MessageManager.placeholders()
                            .player(player.getName())
                            .build());
            
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("staffpanel.vanish.see") && !online.equals(player)) {
                    online.sendMessage(fakeQuit);
                    online.hidePlayer(plugin, player);
                }
            }
        } else {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("staffpanel.vanish.see")) {
                    online.hidePlayer(plugin, player);
                }
            }
        }
        
        plugin.getMessageManager().send(player, "vanish.enabled");
    }

    public void unvanish(Player player) {
        UUID uuid = player.getUniqueId();
        vanishedPlayers.remove(uuid);
        
        player.removeMetadata("vanished", plugin);
        
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
        
        if (plugin.getConfigManager().showFakeQuitMessage()) {
            String fakeJoin = plugin.getMessageManager().getMessage("vanish.fake-join",
                    MessageManager.placeholders()
                            .player(player.getName())
                            .build());
            
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("staffpanel.vanish.see") && !online.equals(player)) {
                    online.sendMessage(fakeJoin);
                }
            }
        }
        
        plugin.getMessageManager().send(player, "vanish.disabled");
    }

    public void toggle(Player player) {
        if (isVanished(player)) {
            unvanish(player);
        } else {
            vanish(player);
        }
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public void hideFromPlayer(Player vanished, Player target) {
        if (!target.hasPermission("staffpanel.vanish.see")) {
            target.hidePlayer(plugin, vanished);
        }
    }

    public void showToPlayer(Player vanished, Player target) {
        target.showPlayer(plugin, vanished);
    }

    public Set<UUID> getVanishedPlayers() {
        return new HashSet<>(vanishedPlayers);
    }

    public void removeVanished(UUID uuid) {
        vanishedPlayers.remove(uuid);
    }
}
