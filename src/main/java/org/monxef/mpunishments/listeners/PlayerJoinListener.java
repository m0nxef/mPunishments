package org.monxef.mpunishments.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.data.PunishmentData;
import org.monxef.mpunishments.enums.PunishmentType;
import org.monxef.mpunishments.utils.MessagesUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final MPunishments plugin;

    public PlayerJoinListener(MPunishments plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();
        String ipAddress = event.getPlayer().getAddress().getAddress().getHostAddress();

        List<PunishmentData> playerBans = plugin.getCacheManager().getActivePunishments(playerUUID);
        List<PunishmentData> ipBans = plugin.getDatabaseManager().getStorage().getIPBans(ipAddress);

        // Check for active player bans
        if (playerBans != null && !playerBans.isEmpty()) {
            for (PunishmentData punishment : playerBans) {
                if (punishment.getType() == PunishmentType.BAN || punishment.getType() == PunishmentType.TEMPBAN) {
                    String banMessage = plugin.getConfigManager().getBanMessage("banned");
                    event.getPlayer().kickPlayer(MessagesUtils.getFormattedMessage(banMessage,punishment));
                    plugin.getLogger().info(playerName + " tried to join but is banned.");
                    return;
                }
            }
        }

        if (ipBans != null && !ipBans.isEmpty()) {
            for (PunishmentData ipBan : ipBans) {
                if (ipBan.getType() == PunishmentType.IPBAN) {
                    String banMessage = plugin.getConfigManager().getString("banned");
                    event.getPlayer().kickPlayer(MessagesUtils.getFormattedMessage(banMessage,ipBan));
                    plugin.getLogger().info(playerName + " tried to join from banned IP: " + ipAddress);
                    return;
                }
            }
        }
        plugin.getCacheManager().getActivePunishments(playerUUID);
    }

}