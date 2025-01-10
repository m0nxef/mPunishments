package org.monxef.mpunishments.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.data.PunishmentData;
import org.monxef.mpunishments.enums.PunishmentType;
import org.monxef.mpunishments.utils.MessagesUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AsyncPlayerChatListener implements Listener {

    private final MPunishments plugin;

    public AsyncPlayerChatListener(MPunishments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        List<PunishmentData> punishments = plugin.getCacheManager().getActivePunishments(playerUUID);

        if (punishments == null || punishments.isEmpty()) {
            punishments = plugin.getDatabaseManager().getStorage().getPunishments(playerUUID);
            if (punishments != null) {
                for (PunishmentData punishment : punishments) {
                    plugin.getCacheManager().addPunishmentToCache(punishment);
                }
            }
        }

        if (punishments != null) {
            for (PunishmentData punishment : punishments) {
                if ((punishment.getType() == PunishmentType.MUTE || punishment.getType() == PunishmentType.TEMPMUTE)
                        && punishment.isActive()
                        && (punishment.getExpiry() == null || punishment.getExpiry().after(new Date()))) {
                    String muteMessage = plugin.getConfigManager().getMuteMessage("muted");
                    event.getPlayer().sendMessage(MessagesUtils.getFormattedMessage(muteMessage, punishment));
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}