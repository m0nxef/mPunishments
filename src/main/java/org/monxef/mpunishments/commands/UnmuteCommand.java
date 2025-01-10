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

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UnmuteCommand implements CommandExecutor {

    private final MPunishments plugin;

    public UnmuteCommand(MPunishments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mpunishments.unmute")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /unmute <player>");
            return true;
        }

        String targetName = args[0];
        if (targetName == null || targetName.trim().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Player name cannot be empty.");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetUUID = target.getUniqueId();

        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " has never played on this server.");
            return true;
        }

        List<PunishmentData> activePunishments = plugin.getCacheManager().getActivePunishments(targetUUID);

        boolean unmuted = false;
        for (PunishmentData punishment : activePunishments) {
            if (punishment.getType() == PunishmentType.MUTE || punishment.getType() == PunishmentType.TEMPMUTE) {
                punishment.setActive(false);
                plugin.getDatabaseManager().getStorage().updatePunishmentActiveStatus(punishment.getId(), false);
                plugin.getCacheManager().removePunishmentFromCache(punishment);
                unmuted = true;
            }
        }

        if (unmuted) {
            sender.sendMessage(ChatColor.GREEN + "Player " + targetName + " has been unmuted.");
            if (target.isOnline()) {
                target.getPlayer().sendMessage(ChatColor.GREEN + "You have been unmuted.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " is not muted.");
        }

        return true;
    }
}