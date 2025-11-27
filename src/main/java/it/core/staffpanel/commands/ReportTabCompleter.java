package it.core.staffpanel.commands;

import it.core.staffpanel.StaffPanelPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportTabCompleter implements TabCompleter {

    private final StaffPanelPlugin plugin;

    public ReportTabCompleter(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String input = args[0].toUpperCase();
            completions = plugin.getConfigManager().getReportTypes().stream()
                    .filter(type -> type.startsWith(input))
                    .collect(Collectors.toList());
        }
        
        return completions;
    }
}
