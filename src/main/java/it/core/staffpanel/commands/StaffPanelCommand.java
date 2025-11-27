package it.core.staffpanel.commands;

import it.core.staffpanel.StaffPanelPlugin;
import it.core.staffpanel.managers.MessageManager;
import it.core.staffpanel.managers.WarnManager;
import it.core.staffpanel.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class StaffPanelCommand implements CommandExecutor {

    private final StaffPanelPlugin plugin;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Long> wildCooldowns = new HashMap<>();

    public StaffPanelCommand(StaffPanelPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                showHelp(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "ban":
                handleBan(sender, args);
                break;
            case "tempban":
                handleTempBan(sender, args);
                break;
            case "unban":
                handleUnban(sender, args);
                break;
            case "mute":
                handleMute(sender, args);
                break;
            case "unmute":
                handleUnmute(sender, args);
                break;
            case "kick":
                handleKick(sender, args);
                break;
            case "warn":
                handleWarn(sender, args);
                break;
            case "warns":
                handleWarns(sender, args);
                break;
            case "clearwarns":
                handleClearWarns(sender, args);
                break;
            case "freeze":
                handleFreeze(sender, args);
                break;
            case "unfreeze":
                handleUnfreeze(sender, args);
                break;
            case "vanish":
                handleVanish(sender);
                break;
            case "fly":
                handleFly(sender);
                break;
            case "heal":
                handleHeal(sender, args);
                break;
            case "feed":
                handleFeed(sender, args);
                break;
            case "tp":
                handleTp(sender, args);
                break;
            case "tphere":
                handleTpHere(sender, args);
                break;
            case "back":
                handleBack(sender);
                break;
            case "wild":
                handleWild(sender);
                break;
            case "gamemode":
            case "gm":
                handleGamemode(sender, args);
                break;
            case "ip":
                handleIp(sender, args);
                break;
            case "alts":
                handleAlts(sender, args);
                break;
            case "clearchat":
            case "cc":
                handleClearChat(sender);
                break;
            case "slowchat":
            case "sc":
                handleSlowChat(sender, args);
                break;
            default:
                plugin.getMessageManager().send(sender, "general.invalid-usage",
                        MessageManager.placeholders().usage("/staffpanel help").build());
                break;
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().getMessage("help.header"));
        sender.sendMessage(plugin.getMessageManager().getMessage("help.commands"));
        sender.sendMessage(plugin.getMessageManager().getMessage("help.footer"));
    }

    private void handleReload(CommandSender sender) {
        if (!checkPermission(sender, "reload")) return;
        
        plugin.reload();
        plugin.getMessageManager().send(sender, "general.reload-success");
    }

    private void handleBan(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "ban")) return;
        if (!checkEnabled("ban", sender)) return;
        
        if (args.length < 3) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel ban <player> <duration> <reason>").build());
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        long duration = -1;
        String reason;
        
        if (TimeUtils.isValidTimeFormat(args[2])) {
            duration = TimeUtils.parseTime(args[2]);
            reason = joinArgs(args, 3, args.length - 1);
        } else {
            reason = joinArgs(args, 2, args.length - 1);
        }
        
        if (plugin.getBanManager().isBanned(target)) {
            plugin.getMessageManager().send(sender, "ban.already-banned",
                    MessageManager.placeholders().player(target.getName()).build());
            return;
        }
        
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        plugin.getBanManager().ban(target, staffName, reason, duration);
        
        plugin.getMessageManager().send(sender, "ban.success",
                MessageManager.placeholders()
                        .player(target.getName())
                        .reason(reason)
                        .build());
        
        if (plugin.getConfigManager().shouldNotifyStaff("ban")) {
            plugin.getMessageManager().broadcastStaff("ban.broadcast", "staffpanel.ban",
                    MessageManager.placeholders()
                            .player(target.getName())
                            .staff(staffName)
                            .reason(reason)
                            .build());
        }
    }

    private void handleTempBan(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "tempban")) return;
        if (!checkEnabled("tempban", sender)) return;
        
        if (args.length < 4) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel tempban <player> <duration> <reason>").build());
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String durationStr = args[2];
        String reason = joinArgs(args, 3, args.length - 1);
        
        if (!TimeUtils.isValidTimeFormat(durationStr)) {
            plugin.getMessageManager().send(sender, "general.invalid-duration");
            return;
        }
        
        long duration = TimeUtils.parseTime(durationStr);
        
        if (plugin.getBanManager().isBanned(target)) {
            plugin.getMessageManager().send(sender, "ban.already-banned",
                    MessageManager.placeholders().player(target.getName()).build());
            return;
        }
        
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        plugin.getBanManager().ban(target, staffName, reason, duration);
        
        plugin.getMessageManager().send(sender, "ban.success",
                MessageManager.placeholders()
                        .player(target.getName())
                        .reason(reason)
                        .build());
        
        if (plugin.getConfigManager().shouldNotifyStaff("tempban")) {
            plugin.getMessageManager().broadcastStaff("ban.broadcast", "staffpanel.ban",
                    MessageManager.placeholders()
                            .player(target.getName())
                            .staff(staffName)
                            .reason(reason)
                            .build());
        }
    }

    private void handleUnban(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "unban")) return;
        if (!checkEnabled("unban", sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel unban <player>").build());
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        
        if (!plugin.getBanManager().isBanned(target)) {
            plugin.getMessageManager().send(sender, "ban.not-banned",
                    MessageManager.placeholders().player(target.getName()).build());
            return;
        }
        
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        plugin.getBanManager().unban(target, staffName);
        
        plugin.getMessageManager().send(sender, "ban.unban-success",
                MessageManager.placeholders().player(target.getName()).build());
        
        if (plugin.getConfigManager().shouldNotifyStaff("unban")) {
            plugin.getMessageManager().broadcastStaff("ban.unban-broadcast", "staffpanel.ban",
                    MessageManager.placeholders()
                            .player(target.getName())
                            .staff(staffName)
                            .build());
        }
    }

    private void handleMute(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "mute")) return;
        if (!checkEnabled("mute", sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel mute <player> [duration]").build());
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageManager().send(sender, "general.player-not-found",
                    MessageManager.placeholders().player(args[1]).build());
            return;
        }
        
        if (plugin.getMuteManager().isMuted(target)) {
            plugin.getMessageManager().send(sender, "mute.already-muted",
                    MessageManager.placeholders().player(target.getName()).build());
            return;
        }
        
        String durationStr = args.length > 2 ? args[2] : plugin.getConfigManager().getDefaultMuteDuration();
        long duration = TimeUtils.parseTime(durationStr);
        
        if (duration <= 0) {
            plugin.getMessageManager().send(sender, "general.invalid-duration");
            return;
        }
        
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        plugin.getMuteManager().mute(target, staffName, duration);
        
        plugin.getMessageManager().send(sender, "mute.success",
                MessageManager.placeholders()
                        .player(target.getName())
                        .duration(TimeUtils.formatDuration(duration))
                        .build());
        
        if (plugin.getConfigManager().shouldNotifyStaff("mute")) {
            plugin.getMessageManager().broadcastStaff("mute.broadcast", "staffpanel.mute",
                    MessageManager.placeholders()
                            .player(target.getName())
                            .staff(staffName)
                            .duration(TimeUtils.formatDuration(duration))
                            .build());
        }
    }

    private void handleUnmute(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "unmute")) return;
        if (!checkEnabled("unmute", sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel unmute <player>").build());
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        
        if (!plugin.getMuteManager().isMuted(target)) {
            plugin.getMessageManager().send(sender, "mute.not-muted",
                    MessageManager.placeholders().player(target.getName()).build());
            return;
        }
        
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        plugin.getMuteManager().unmute(target, staffName);
        
        plugin.getMessageManager().send(sender, "mute.unmute-success",
                MessageManager.placeholders().player(target.getName()).build());
        
        if (plugin.getConfigManager().shouldNotifyStaff("unmute")) {
            plugin.getMessageManager().broadcastStaff("mute.unmute-broadcast", "staffpanel.mute",
                    MessageManager.placeholders()
                            .player(target.getName())
                            .staff(staffName)
                            .build());
        }
    }

    private void handleKick(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "kick")) return;
        if (!checkEnabled("kick", sender)) return;
        
        if (args.length < 3) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel kick <player> <reason>").build());
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageManager().send(sender, "general.player-not-found",
                    MessageManager.placeholders().player(args[1]).build());
            return;
        }
        
        String reason = joinArgs(args, 2, args.length - 1);
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        
        String kickMessage = plugin.getMessageManager().getMessage("kick.kick-message",
                MessageManager.placeholders()
                        .reason(reason)
                        .staff(staffName)
                        .build());
        
        target.kickPlayer(kickMessage);
        
        plugin.getMessageManager().send(sender, "kick.success",
                MessageManager.placeholders()
                        .player(target.getName())
                        .reason(reason)
                        .build());
        
        if (plugin.getConfigManager().shouldNotifyStaff("kick")) {
            plugin.getMessageManager().broadcastStaff("kick.broadcast", "staffpanel.kick",
                    MessageManager.placeholders()
                            .player(target.getName())
                            .staff(staffName)
                            .reason(reason)
                            .build());
        }
        
        if (plugin.getConfigManager().isCommandLogged("kick")) {
            plugin.getLogManager().log("kick", staffName, target.getName(), reason, "");
        }
        
        plugin.getDiscordWebhook().sendKick(target.getName(), staffName, reason);
    }

    private void handleWarn(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "warn")) return;
        if (!checkEnabled("warn", sender)) return;
        
        if (args.length < 3) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel warn <player> <reason>").build());
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String reason = joinArgs(args, 2, args.length - 1);
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        
        int currentWarns = plugin.getWarnManager().getWarnCount(target) + 1;
        int maxWarns = plugin.getConfigManager().getMaxWarns();
        
        plugin.getWarnManager().warn(target, staffName, reason);
        
        plugin.getMessageManager().send(sender, "warn.success",
                MessageManager.placeholders()
                        .player(target.getName())
                        .reason(reason)
                        .current(currentWarns)
                        .max(maxWarns)
                        .build());
        
        if (plugin.getConfigManager().shouldNotifyStaff("warn")) {
            plugin.getMessageManager().broadcastStaff("warn.broadcast", "staffpanel.warn",
                    MessageManager.placeholders()
                            .player(target.getName())
                            .staff(staffName)
                            .reason(reason)
                            .build());
        }
    }

    private void handleWarns(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "warns")) return;
        if (!checkEnabled("warns", sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel warns <player>").build());
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        List<WarnManager.WarnData> warns = plugin.getWarnManager().getWarns(target);
        
        if (warns.isEmpty()) {
            plugin.getMessageManager().send(sender, "warn.list-empty",
                    MessageManager.placeholders().player(target.getName()).build());
            return;
        }
        
        plugin.getMessageManager().sendRaw(sender, "warn.list-header",
                MessageManager.placeholders().player(target.getName()).build());
        
        int id = 1;
        for (WarnManager.WarnData warn : warns) {
            sender.sendMessage(plugin.getMessageManager().getMessage("warn.list-entry",
                    MessageManager.placeholders()
                            .id(id++)
                            .reason(warn.getReason())
                            .staff(warn.getStaff())
                            .date(plugin.getWarnManager().formatDate(warn.getDate()))
                            .build()));
        }
    }

    private void handleClearWarns(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "clearwarns")) return;
        if (!checkEnabled("clearwarns", sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel clearwarns <player>").build());
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        
        plugin.getWarnManager().clearWarns(target, staffName);
        
        plugin.getMessageManager().send(sender, "warn.clear-success",
                MessageManager.placeholders().player(target.getName()).build());
    }

    private void handleFreeze(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "freeze")) return;
        if (!checkEnabled("freeze", sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel freeze <player>").build());
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageManager().send(sender, "general.player-not-found",
                    MessageManager.placeholders().player(args[1]).build());
            return;
        }
        
        if (plugin.getFreezeManager().isFrozen(target)) {
            plugin.getMessageManager().send(sender, "freeze.already-frozen",
                    MessageManager.placeholders().player(target.getName()).build());
            return;
        }
        
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        plugin.getFreezeManager().freeze(target, staffName);
        
        plugin.getMessageManager().send(sender, "freeze.success",
                MessageManager.placeholders().player(target.getName()).build());
        
        if (plugin.getConfigManager().shouldNotifyStaff("freeze")) {
            plugin.getMessageManager().broadcastStaff("freeze.broadcast", "staffpanel.freeze",
                    MessageManager.placeholders()
                            .player(target.getName())
                            .staff(staffName)
                            .build());
        }
    }

    private void handleUnfreeze(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "unfreeze")) return;
        if (!checkEnabled("unfreeze", sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel unfreeze <player>").build());
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageManager().send(sender, "general.player-not-found",
                    MessageManager.placeholders().player(args[1]).build());
            return;
        }
        
        if (!plugin.getFreezeManager().isFrozen(target)) {
            plugin.getMessageManager().send(sender, "freeze.not-frozen",
                    MessageManager.placeholders().player(target.getName()).build());
            return;
        }
        
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        plugin.getFreezeManager().unfreeze(target, staffName);
        
        plugin.getMessageManager().send(sender, "freeze.unfreeze-success",
                MessageManager.placeholders().player(target.getName()).build());
    }

    private void handleVanish(CommandSender sender) {
        if (!checkPermission(sender, "vanish")) return;
        if (!checkEnabled("vanish", sender)) return;
        if (!isPlayer(sender)) return;
        
        Player player = (Player) sender;
        plugin.getVanishManager().toggle(player);
    }

    private void handleFly(CommandSender sender) {
        if (!checkPermission(sender, "fly")) return;
        if (!checkEnabled("fly", sender)) return;
        if (!isPlayer(sender)) return;
        
        Player player = (Player) sender;
        boolean flying = !player.getAllowFlight();
        player.setAllowFlight(flying);
        player.setFlying(flying);
        
        if (flying) {
            plugin.getMessageManager().send(sender, "fly.enabled");
        } else {
            plugin.getMessageManager().send(sender, "fly.disabled");
        }
    }

    private void handleHeal(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "heal")) return;
        if (!checkEnabled("heal", sender)) return;
        
        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                plugin.getMessageManager().send(sender, "general.player-not-found",
                        MessageManager.placeholders().player(args[1]).build());
                return;
            }
        } else {
            if (!isPlayer(sender)) return;
            target = (Player) sender;
        }
        
        target.setHealth(target.getMaxHealth());
        target.setFoodLevel(20);
        target.setSaturation(20);
        target.setFireTicks(0);
        
        if (target.equals(sender)) {
            plugin.getMessageManager().send(sender, "heal.self");
        } else {
            String staffName = sender instanceof Player ? sender.getName() : "Console";
            plugin.getMessageManager().send(sender, "heal.success",
                    MessageManager.placeholders().player(target.getName()).build());
            plugin.getMessageManager().send(target, "heal.target",
                    MessageManager.placeholders().staff(staffName).build());
        }
    }

    private void handleFeed(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "feed")) return;
        if (!checkEnabled("feed", sender)) return;
        
        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                plugin.getMessageManager().send(sender, "general.player-not-found",
                        MessageManager.placeholders().player(args[1]).build());
                return;
            }
        } else {
            if (!isPlayer(sender)) return;
            target = (Player) sender;
        }
        
        target.setFoodLevel(20);
        target.setSaturation(20);
        
        if (target.equals(sender)) {
            plugin.getMessageManager().send(sender, "feed.self");
        } else {
            String staffName = sender instanceof Player ? sender.getName() : "Console";
            plugin.getMessageManager().send(sender, "feed.success",
                    MessageManager.placeholders().player(target.getName()).build());
            plugin.getMessageManager().send(target, "feed.target",
                    MessageManager.placeholders().staff(staffName).build());
        }
    }

    private void handleTp(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "tp")) return;
        if (!checkEnabled("tp", sender)) return;
        if (!isPlayer(sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel tp <player>").build());
            return;
        }
        
        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[1]);
        
        if (target == null) {
            plugin.getMessageManager().send(sender, "general.player-not-found",
                    MessageManager.placeholders().player(args[1]).build());
            return;
        }
        
        lastLocations.put(player.getUniqueId(), player.getLocation());
        player.teleport(target);
        
        plugin.getMessageManager().send(sender, "tp.success",
                MessageManager.placeholders().player(target.getName()).build());
    }

    private void handleTpHere(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "tphere")) return;
        if (!checkEnabled("tphere", sender)) return;
        if (!isPlayer(sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel tphere <player>").build());
            return;
        }
        
        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[1]);
        
        if (target == null) {
            plugin.getMessageManager().send(sender, "general.player-not-found",
                    MessageManager.placeholders().player(args[1]).build());
            return;
        }
        
        lastLocations.put(target.getUniqueId(), target.getLocation());
        target.teleport(player);
        
        plugin.getMessageManager().send(sender, "tphere.success",
                MessageManager.placeholders().player(target.getName()).build());
        plugin.getMessageManager().send(target, "tphere.target",
                MessageManager.placeholders().staff(player.getName()).build());
    }

    private void handleBack(CommandSender sender) {
        if (!checkPermission(sender, "back")) return;
        if (!checkEnabled("back", sender)) return;
        if (!isPlayer(sender)) return;
        
        Player player = (Player) sender;
        Location lastLocation = lastLocations.get(player.getUniqueId());
        
        if (lastLocation == null) {
            plugin.getMessageManager().send(sender, "back.no-location");
            return;
        }
        
        lastLocations.put(player.getUniqueId(), player.getLocation());
        player.teleport(lastLocation);
        
        plugin.getMessageManager().send(sender, "back.success");
    }

    private void handleWild(CommandSender sender) {
        if (!checkPermission(sender, "wild")) return;
        if (!checkEnabled("wild", sender)) return;
        if (!isPlayer(sender)) return;
        
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        
        Long lastUse = wildCooldowns.get(uuid);
        int cooldown = plugin.getConfigManager().getWildCooldown() * 1000;
        
        if (lastUse != null && System.currentTimeMillis() - lastUse < cooldown) {
            int remaining = (int) ((cooldown - (System.currentTimeMillis() - lastUse)) / 1000);
            plugin.getMessageManager().send(sender, "general.cooldown",
                    MessageManager.placeholders().time(String.valueOf(remaining)).build());
            return;
        }
        
        plugin.getMessageManager().sendRaw(sender, "wild.searching");
        
        int maxDistance = plugin.getConfigManager().getWildMaxDistance();
        int minDistance = plugin.getConfigManager().getWildMinDistance();
        Random random = new Random();
        
        Location center = player.getWorld().getSpawnLocation();
        int attempts = 0;
        Location safeLocation = null;
        
        while (attempts < 50 && safeLocation == null) {
            int distance = minDistance + random.nextInt(maxDistance - minDistance);
            double angle = random.nextDouble() * 2 * Math.PI;
            
            int x = (int) (center.getX() + distance * Math.cos(angle));
            int z = (int) (center.getZ() + distance * Math.sin(angle));
            int y = player.getWorld().getHighestBlockYAt(x, z);
            
            Location testLoc = new Location(player.getWorld(), x, y + 1, z);
            
            if (isSafeLocation(testLoc)) {
                safeLocation = testLoc;
            }
            
            attempts++;
        }
        
        if (safeLocation == null) {
            plugin.getMessageManager().send(sender, "wild.failed");
            return;
        }
        
        lastLocations.put(uuid, player.getLocation());
        player.teleport(safeLocation);
        wildCooldowns.put(uuid, System.currentTimeMillis());
        
        plugin.getMessageManager().send(sender, "wild.success");
    }

    private boolean isSafeLocation(Location location) {
        if (location.getBlock().getType().isSolid()) {
            return false;
        }
        
        String blockBelow = location.clone().subtract(0, 1, 0).getBlock().getType().name();
        List<String> unsafeBlocks = plugin.getConfigManager().getUnsafeBlocks();
        
        return !unsafeBlocks.contains(blockBelow);
    }

    private void handleGamemode(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "gamemode")) return;
        if (!checkEnabled("gamemode", sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel gamemode <0/1/2/3> [player]").build());
            return;
        }
        
        GameMode gameMode = parseGameMode(args[1]);
        if (gameMode == null) {
            plugin.getMessageManager().send(sender, "gamemode.invalid");
            return;
        }
        
        Player target;
        if (args.length > 2) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                plugin.getMessageManager().send(sender, "general.player-not-found",
                        MessageManager.placeholders().player(args[2]).build());
                return;
            }
        } else {
            if (!isPlayer(sender)) return;
            target = (Player) sender;
        }
        
        target.setGameMode(gameMode);
        
        if (target.equals(sender)) {
            plugin.getMessageManager().send(sender, "gamemode.changed",
                    MessageManager.placeholders().gamemode(gameMode.name()).build());
        } else {
            String staffName = sender instanceof Player ? sender.getName() : "Console";
            plugin.getMessageManager().send(sender, "gamemode.changed-other",
                    MessageManager.placeholders()
                            .player(target.getName())
                            .gamemode(gameMode.name())
                            .build());
            plugin.getMessageManager().send(target, "gamemode.target",
                    MessageManager.placeholders()
                            .staff(staffName)
                            .gamemode(gameMode.name())
                            .build());
        }
    }

    private GameMode parseGameMode(String input) {
        switch (input.toLowerCase()) {
            case "0":
            case "survival":
            case "s":
                return GameMode.SURVIVAL;
            case "1":
            case "creative":
            case "c":
                return GameMode.CREATIVE;
            case "2":
            case "adventure":
            case "a":
                return GameMode.ADVENTURE;
            case "3":
            case "spectator":
            case "sp":
                return GameMode.SPECTATOR;
            default:
                return null;
        }
    }

    private void handleIp(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "ip")) return;
        if (!checkEnabled("ip", sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel ip <player>").build());
            return;
        }
        
        String targetName = args[1];
        Player onlineTarget = Bukkit.getPlayer(targetName);
        String ip;
        
        if (onlineTarget != null) {
            ip = onlineTarget.getAddress().getAddress().getHostAddress();
        } else {
            ip = plugin.getDataManager().getPlayerIP(targetName);
            if (ip == null) {
                plugin.getMessageManager().send(sender, "general.player-not-found",
                        MessageManager.placeholders().player(targetName).build());
                return;
            }
        }
        
        plugin.getMessageManager().send(sender, "ip.info",
                MessageManager.placeholders()
                        .player(targetName)
                        .ip(ip)
                        .build());
    }

    private void handleAlts(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "alts")) return;
        if (!checkEnabled("alts", sender)) return;
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "general.invalid-usage",
                    MessageManager.placeholders().usage("/staffpanel alts <player>").build());
            return;
        }
        
        String targetName = args[1];
        Map<String, String> alts = plugin.getDataManager().getAlternateAccounts(targetName);
        
        if (alts.isEmpty()) {
            plugin.getMessageManager().send(sender, "alts.none",
                    MessageManager.placeholders().player(targetName).build());
            return;
        }
        
        plugin.getMessageManager().sendRaw(sender, "alts.header",
                MessageManager.placeholders().player(targetName).build());
        
        for (String alt : alts.keySet()) {
            sender.sendMessage(plugin.getMessageManager().getMessage("alts.entry",
                    MessageManager.placeholders().alt(alt).build()));
        }
    }

    private void handleClearChat(CommandSender sender) {
        if (!checkPermission(sender, "clearchat")) return;
        if (!checkEnabled("clearchat", sender)) return;
        
        int lines = plugin.getConfigManager().getClearChatLines();
        String staffName = sender instanceof Player ? sender.getName() : "Console";
        
        for (int i = 0; i < lines; i++) {
            Bukkit.broadcastMessage("");
        }
        
        plugin.getMessageManager().broadcast("clearchat.success",
                MessageManager.placeholders().staff(staffName).build());
        
        if (plugin.getConfigManager().isCommandLogged("clearchat")) {
            plugin.getLogManager().log("clearchat", staffName, "", "", "");
        }
    }

    private void handleSlowChat(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "slowchat")) return;
        if (!checkEnabled("slowchat", sender)) return;
        
        boolean currentlyEnabled = plugin.getConfigManager().isSlowchatEnabled();
        
        if (currentlyEnabled) {
            plugin.getConfigManager().setSlowchatEnabled(false);
            plugin.getMessageManager().broadcast("slowchat.disabled");
        } else {
            int delay = plugin.getConfigManager().getDefaultSlowchatDelay();
            
            if (args.length > 1) {
                try {
                    delay = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {}
            }
            
            plugin.getConfigManager().setSlowchatDelay(delay);
            plugin.getConfigManager().setSlowchatEnabled(true);
            plugin.getMessageManager().broadcast("slowchat.enabled",
                    MessageManager.placeholders().delay(delay).build());
        }
    }

    private boolean checkPermission(CommandSender sender, String command) {
        String permission = plugin.getConfigManager().getCommandPermission(command);
        if (!sender.hasPermission(permission)) {
            plugin.getMessageManager().send(sender, "general.no-permission");
            return false;
        }
        return true;
    }

    private boolean checkEnabled(String command, CommandSender sender) {
        if (!plugin.getConfigManager().isCommandEnabled(command)) {
            plugin.getMessageManager().send(sender, "general.command-disabled");
            return false;
        }
        return true;
    }

    private boolean isPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().send(sender, "general.players-only");
            return false;
        }
        return true;
    }

    private String joinArgs(String[] args, int start, int end) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i <= end && i < args.length; i++) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }

    public void saveLastLocation(Player player) {
        lastLocations.put(player.getUniqueId(), player.getLocation());
    }
}
