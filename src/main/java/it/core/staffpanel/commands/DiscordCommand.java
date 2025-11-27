package it.core.staffpanel.commands;

import it.core.staffpanel.StaffPanelPlugin;
import it.core.staffpanel.managers.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DiscordCommand implements CommandExecutor {

    private final StaffPanelPlugin plugin;

    public DiscordCommand(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("discord")) {
            plugin.getMessageManager().send(sender, "general.command-disabled");
            return true;
        }
        
        if (!sender.hasPermission(plugin.getConfigManager().getCommandPermission("discord"))) {
            plugin.getMessageManager().send(sender, "general.no-permission");
            return true;
        }
        
        String discordLink = plugin.getConfigManager().getDiscordLink();
        
        plugin.getMessageManager().send(sender, "discord.message",
                MessageManager.placeholders().discordLink(discordLink).build());
        
        return true;
    }
}
