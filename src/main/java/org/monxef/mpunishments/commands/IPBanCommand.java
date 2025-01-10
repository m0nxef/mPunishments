package org.monxef.mpunishments.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.data.PunishmentData;
import org.monxef.mpunishments.enums.PunishmentType;
import org.monxef.mpunishments.managers.ConfigManager;
import org.monxef.mpunishments.utils.MessagesUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;


public class IPBanCommand implements CommandExecutor {
    private final MPunishments plugin;

    public IPBanCommand(MPunishments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cm = plugin.getConfigManager();

        if (!sender.hasPermission("mpunishments.ipban")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /ipban <player/ip> [reason]");
            return true;
        }

        String target = args[0];
        if (target == null || target.trim().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Player/IP cannot be empty.");
            return true;
        }
        String reason = "No reason provided.";
        if (args.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            reason = sb.toString().trim();
            if (reason.isEmpty()){
                reason = "No reason provided.";
            }
        }
        String ipAddress;
        Player player = Bukkit.getPlayer(target);
        if (player != null) {
            ipAddress = player.getAddress().getAddress().getHostAddress();
        } else {
            try {
                InetAddress address = InetAddress.getByName(target);
                ipAddress = address.getHostAddress();
            } catch (UnknownHostException e) {
                sender.sendMessage(ChatColor.RED + "Invalid player or IP address.");
                return true;
            }
        }
        String punisherName = sender.getName();
        PunishmentData punishment = new PunishmentData(null, target, punisherName, reason, PunishmentType.IPBAN, new Date(), null, ipAddress);
        plugin.getDatabaseManager().getStorage().savePunishment(punishment);
        if (player.isOnline()) {
            player.kickPlayer(MessagesUtils.getFormattedMessage(cm.getBanMessage("banned"),punishment));
        }
        Bukkit.broadcastMessage(MessagesUtils.getFormattedMessage(cm.getPublicNotificationMessage(),punishment));

        plugin.getCacheManager().addPunishmentToCache(punishment);

        sender.sendMessage(ChatColor.GREEN + "IP address " + ipAddress + " has been banned for: " + reason);
        return true;
    }
}