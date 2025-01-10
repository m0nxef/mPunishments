package org.monxef.mpunishments.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.menus.HistoryMenu;

public class PunishmentHistoryCommand implements CommandExecutor {
    private final MPunishments plugin;

    public PunishmentHistoryCommand(MPunishments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mpunishments.history")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /punishmenthistory <player>");
            return true;
        }

        String targetName = args[0];
        if (targetName == null || targetName.trim().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Player name cannot be empty.");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if(!target.hasPlayedBefore()){
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " has never played before.");
            return true;
        }

        Player onlineTarget = target.getPlayer();

        if(onlineTarget == null && !sender.hasPermission("mpunishments.history.offline")){
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " is not online. You need mpunishments.history.offline to check offline players.");
            return true;
        }

        HistoryMenu menu = new HistoryMenu(plugin, target);

        menu.open((Player) sender);
        return true;
    }
}