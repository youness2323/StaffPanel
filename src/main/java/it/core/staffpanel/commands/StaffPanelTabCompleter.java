package it.core.staffpanel.commands;

import it.core.staffpanel.StaffPanelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StaffPanelTabCompleter implements TabCompleter {

    private final StaffPanelPlugin plugin;
    
    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "help", "reload",
            "ban", "tempban", "unban",
            "mute", "unmute",
            "kick", "warn", "warns", "clearwarns",
            "freeze", "unfreeze",
            "vanish", "fly",
            "heal", "feed",
            "tp", "tphere", "back", "wild",
            "gamemode", "gm",
            "ip", "alts",
            "clearchat", "cc", "slowchat", "sc"
    );
    
    private static final List<String> GAMEMODES = Arrays.asList(
            "0", "1", "2", "3",
            "survival", "creative", "adventure", "spectator"
    );
    
    private static final List<String> DURATIONS = Arrays.asList(
            "1m", "5m", "10m", "30m",
            "1h", "2h", "6h", "12h",
            "1d", "3d", "7d", "14d", "30d",
            "1w", "2w", "1mo"
    );

    public StaffPanelTabCompleter(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            completions = SUBCOMMANDS.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .filter(cmd -> hasPermissionForCommand(sender, cmd))
                    .collect(Collectors.toList());
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "ban":
                case "tempban":
                case "unban":
                case "mute":
                case "unmute":
                case "kick":
                case "warn":
                case "warns":
                case "clearwarns":
                case "freeze":
                case "unfreeze":
                case "heal":
                case "feed":
                case "tp":
                case "tphere":
                case "ip":
                case "alts":
                    if (args.length == 2) {
                        completions = getPlayerCompletions(args[1]);
                    } else if ((subCommand.equals("ban") || subCommand.equals("tempban") || 
                               subCommand.equals("mute")) && args.length == 3) {
                        completions = getDurationCompletions(args[args.length - 1]);
                    }
                    break;
                    
                case "gamemode":
                case "gm":
                    if (args.length == 2) {
                        completions = getGamemodeCompletions(args[1]);
                    } else if (args.length == 3) {
                        completions = getPlayerCompletions(args[2]);
                    }
                    break;
                    
                case "slowchat":
                case "sc":
                    if (args.length == 2) {
                        completions = Arrays.asList("1", "3", "5", "10", "15", "30", "60");
                    }
                    break;
            }
        }
        
        return completions;
    }
    
    private List<String> getPlayerCompletions(String input) {
        String lowerInput = input.toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(lowerInput))
                .collect(Collectors.toList());
    }
    
    private List<String> getGamemodeCompletions(String input) {
        String lowerInput = input.toLowerCase();
        return GAMEMODES.stream()
                .filter(gm -> gm.startsWith(lowerInput))
                .collect(Collectors.toList());
    }
    
    private List<String> getDurationCompletions(String input) {
        String lowerInput = input.toLowerCase();
        return DURATIONS.stream()
                .filter(dur -> dur.startsWith(lowerInput))
                .collect(Collectors.toList());
    }
    
    private boolean hasPermissionForCommand(CommandSender sender, String command) {
        String permission = plugin.getConfigManager().getCommandPermission(command);
        return sender.hasPermission(permission) || sender.hasPermission("staffpanel.*");
    }
}
