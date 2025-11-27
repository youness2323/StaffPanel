package it.core.staffpanel.managers;

import it.core.staffpanel.StaffPanelPlugin;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeManager {

    private final StaffPanelPlugin plugin;
    private final Set<UUID> frozenPlayers = new HashSet<>();

    public FreezeManager(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    public void freeze(Player target, String staff) {
        UUID uuid = target.getUniqueId();
        frozenPlayers.add(uuid);
        
        plugin.getMessageManager().send(target, "freeze.target-message",
                MessageManager.placeholders()
                        .staff(staff)
                        .build());
        
        if (plugin.getConfigManager().isCommandLogged("freeze")) {
            plugin.getLogManager().log("freeze", staff, target.getName(), "", "");
        }
        
        plugin.getDiscordWebhook().sendFreeze(target.getName(), staff, "Freezato");
    }

    public void unfreeze(Player target, String staff) {
        UUID uuid = target.getUniqueId();
        frozenPlayers.remove(uuid);
        
        plugin.getMessageManager().send(target, "freeze.unfreeze-target",
                MessageManager.placeholders()
                        .staff(staff)
                        .build());
        
        if (plugin.getConfigManager().isCommandLogged("unfreeze")) {
            plugin.getLogManager().log("unfreeze", staff, target.getName(), "", "");
        }
        
        plugin.getDiscordWebhook().sendFreeze(target.getName(), staff, "Sfreezato");
    }

    public boolean isFrozen(Player player) {
        return frozenPlayers.contains(player.getUniqueId());
    }

    public void removeFrozen(UUID uuid) {
        frozenPlayers.remove(uuid);
    }

    public Set<UUID> getFrozenPlayers() {
        return new HashSet<>(frozenPlayers);
    }
}
