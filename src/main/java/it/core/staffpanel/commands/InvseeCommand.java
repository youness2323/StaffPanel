
package it.core.staffpanel.commands;

import it.core.staffpanel.StaffPanelPlugin;
import it.core.staffpanel.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvseeCommand implements CommandExecutor {

    private final StaffPanelPlugin plugin;

    public InvseeCommand(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().send(sender, "general.players-only");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("staffpanel.invsee")) {
            plugin.getMessageManager().send(player, "general.no-permission");
            return true;
        }

        if (args.length < 1) {
            plugin.getMessageManager().send(player, "general.invalid-usage",
                    MessageManager.placeholders().usage("/invsee <player>").build());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().send(player, "general.player-not-found",
                    MessageManager.placeholders().player(args[0]).build());
            return true;
        }

        player.openInventory(target.getInventory());
        plugin.getMessageManager().send(player, "invsee.opened",
                MessageManager.placeholders().player(target.getName()).build());

        if (plugin.getConfigManager().isCommandLogged("invsee")) {
            plugin.getLogManager().log("invsee", player.getName(), target.getName(), "", "");
        }

        return true;
    }
}
