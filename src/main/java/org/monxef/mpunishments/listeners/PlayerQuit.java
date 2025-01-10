package org.monxef.mpunishments.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.data.PunishmentData;
import org.monxef.mpunishments.managers.CacheManager;

import java.util.List;

public class PlayerQuit implements Listener {

    private MPunishments plugin;
    public PlayerQuit(MPunishments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        CacheManager cm = plugin.getCacheManager();
        List<PunishmentData> punishments = cm.getActivePunishments(e.getPlayer().getUniqueId());
        if (punishments.isEmpty()) return;
        for (PunishmentData punishment : punishments) {
            cm.removePunishmentFromCache(punishment);
        }
    }

}
