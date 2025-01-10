package org.monxef.mpunishments.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.data.PunishmentData;
import org.monxef.mpunishments.enums.PunishmentType;
import org.monxef.mpunishments.utils.TimeUtils;

import java.util.Date;
import java.util.UUID;


public class TempmuteCommand implements CommandExecutor {
    private final MPunishments plugin;

    public TempmuteCommand(MPunishments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mpunishments.tempmute")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /tempmute <player> <duration> [reason]");
            return true;
        }

        String targetName = args[0];
        if (targetName == null || targetName.trim().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Player name cannot be empty.");
            return true;
        }

        if (sender instanceof Player && targetName.equals(((Player) sender).getName())) {
            sender.sendMessage(ChatColor.RED + "You cannot tempmute yourself.");
            return true;
        }

        String durationString = args[1];
        if (durationString == null || durationString.trim().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Duration cannot be empty.");
            return true;
        }

        String reason = "No reason provided.";
        if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            reason = sb.toString().trim();
            if (reason.isEmpty()){
                reason = "No reason provided.";
            }
        }

        Date expiry = TimeUtils.parseDuration(durationString);
        if (expiry == null) {
            sender.sendMessage(ChatColor.RED + "Invalid duration format. Use (number)(s/m/h/d/w/M/y).");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetUUID = target.getUniqueId();
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " has never played on this server.");
            return true;
        }

        PunishmentData punishment = new PunishmentData(targetUUID, targetName, sender.getName(), reason, PunishmentType.TEMPMUTE, new Date(), expiry);
        plugin.getDatabaseManager().getStorage().savePunishment(punishment);
        plugin.getCacheManager().addPunishmentToCache(punishment); // Add the punishment to the cache

        sender.sendMessage(ChatColor.GREEN + "Player " + targetName + " has been temporarily muted for " + durationString + " for: " + reason);
        return true;
    }
}