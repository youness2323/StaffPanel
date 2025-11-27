package it.core.staffpanel.commands;

import it.core.staffpanel.StaffPanelPlugin;
import it.core.staffpanel.managers.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand implements CommandExecutor {

    private final StaffPanelPlugin plugin;

    public ReportCommand(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().send(sender, "general.players-only");
            return true;
        }
        
        if (!plugin.getConfigManager().isCommandEnabled("report")) {
            plugin.getMessageManager().send(sender, "general.command-disabled");
            return true;
        }
        
        if (!sender.hasPermission(plugin.getConfigManager().getCommandPermission("report"))) {
            plugin.getMessageManager().send(sender, "general.no-permission");
            return true;
        }
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/report <tipologia> <motivo>").build());
            return true;
        }
        
        Player player = (Player) sender;
        String type = args[0].toUpperCase();
        
        if (!plugin.getReportManager().isValidType(type)) {
            plugin.getMessageManager().send(sender, "report.invalid-type",
                    MessageManager.placeholders().types(plugin.getReportManager().getValidTypes()).build());
            return true;
        }
        
        if (!plugin.getReportManager().canReport(player)) {
            int remaining = plugin.getReportManager().getRemainingCooldown(player);
            plugin.getMessageManager().send(sender, "general.cooldown",
                    MessageManager.placeholders().time(String.valueOf(remaining)).build());
            return true;
        }
        
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (reasonBuilder.length() > 0) {
                reasonBuilder.append(" ");
            }
            reasonBuilder.append(args[i]);
        }
        String reason = reasonBuilder.toString();
        
        plugin.getReportManager().createReport(player, type, reason);
        
        plugin.getMessageManager().send(sender, "report.success");
        
        return true;
    }
}
