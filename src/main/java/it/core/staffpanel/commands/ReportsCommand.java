package it.core.staffpanel.commands;

import it.core.staffpanel.StaffPanelPlugin;
import it.core.staffpanel.managers.MessageManager;
import it.core.staffpanel.managers.ReportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReportsCommand implements CommandExecutor {

    private final StaffPanelPlugin plugin;
    private static final int REPORTS_PER_PAGE = 10;

    public ReportsCommand(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("reports")) {
            plugin.getMessageManager().send(sender, "general.command-disabled");
            return true;
        }
        
        if (!sender.hasPermission(plugin.getConfigManager().getCommandPermission("reports"))) {
            plugin.getMessageManager().send(sender, "general.no-permission");
            return true;
        }
        
        int page = 1;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("clear")) {
                plugin.getReportManager().clearReports();
                sender.sendMessage(MessageManager.colorize("&aReport cancellati con successo!"));
                return true;
            }
            
            if (args[0].equalsIgnoreCase("delete") && args.length > 1) {
                try {
                    int id = Integer.parseInt(args[1]);
                    plugin.getReportManager().deleteReport(id);
                    plugin.getMessageManager().send(sender, "report.deleted",
                            MessageManager.placeholders().id(id).build());
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageManager.colorize("&cID non valido!"));
                }
                return true;
            }
            
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }
        
        List<ReportManager.ReportData> reports = plugin.getReportManager().getReports(page, REPORTS_PER_PAGE);
        int totalPages = plugin.getReportManager().getTotalPages(REPORTS_PER_PAGE);
        
        if (reports.isEmpty()) {
            plugin.getMessageManager().send(sender, "report.list-empty");
            return true;
        }
        
        plugin.getMessageManager().sendRaw(sender, "report.list-header",
                MessageManager.placeholders()
                        .page(page)
                        .maxpage(Math.max(1, totalPages))
                        .build());
        
        for (ReportManager.ReportData report : reports) {
            sender.sendMessage(plugin.getMessageManager().getMessage("report.list-entry",
                    MessageManager.placeholders()
                            .id(report.getId())
                            .player(report.getPlayer())
                            .type(report.getType())
                            .reason(report.getReason())
                            .date(plugin.getReportManager().formatDate(report.getDate()))
                            .build()));
        }
        
        return true;
    }
}
