package org.monxef.mpunishments.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.data.PunishmentData;
import org.monxef.mpunishments.enums.PunishmentType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CacheManager {

    private final MPunishments plugin;
    private final ConcurrentHashMap<UUID, Boolean> activePunishmentStatusCache = new ConcurrentHashMap<>();
    private final Cache<UUID, PunishmentData> recentPunishmentCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(500)
            .build();

    public CacheManager(MPunishments plugin) {
        this.plugin = plugin;
        startExpiryTask();
    }

    public List<PunishmentData> getActivePunishments(UUID playerUUID) {
        if (activePunishmentStatusCache.containsKey(playerUUID) && !activePunishmentStatusCache.get(playerUUID)) {
            return List.of();
        }

        PunishmentData cachedPunishment = recentPunishmentCache.getIfPresent(playerUUID);
        if (cachedPunishment != null && !cachedPunishment.isExpired()) {
            return List.of(cachedPunishment);
        }

        List<PunishmentData> punishments = plugin.getDatabaseManager().getStorage().getActivePunishments(playerUUID);
        if (!punishments.isEmpty()) {
            updateCaches(punishments, playerUUID);
        } else {
            activePunishmentStatusCache.put(playerUUID, false);
        }
        return punishments;
    }

    public void addPunishmentToCache(PunishmentData punishment) {
        List<PunishmentData> punishments = plugin.getDatabaseManager().getStorage().getActivePunishments(punishment.getPlayerUUID());
        updateCaches(punishments, punishment.getPlayerUUID());
    }

    public void removePunishmentFromCache(PunishmentData punishment) {
        recentPunishmentCache.invalidate(punishment.getPlayerUUID());
        List<PunishmentData> punishments = plugin.getDatabaseManager().getStorage().getActivePunishments(punishment.getPlayerUUID());
        updateCaches(punishments, punishment.getPlayerUUID());
    }

    private void updateCaches(List<PunishmentData> punishments, UUID playerUUID) {
        activePunishmentStatusCache.put(playerUUID, !punishments.isEmpty());

        if (!punishments.isEmpty()) {
            PunishmentData mostRecent = punishments.stream()
                    .filter(p -> !p.isExpired())
                    .max(Comparator.comparing(PunishmentData::getDate))
                    .orElse(null);

            if (mostRecent != null) {
                recentPunishmentCache.put(playerUUID, mostRecent);
            }
        } else {
            recentPunishmentCache.invalidate(playerUUID);
        }
    }

    private void startExpiryTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            List<PunishmentData> expiredPunishments = plugin.getDatabaseManager().getStorage().getExpiredPunishments();
            for (PunishmentData punishment : expiredPunishments) {
                plugin.getDatabaseManager().getStorage().updatePunishmentActiveStatus(punishment.getId(), false);
                removePunishmentFromCache(punishment);
            }
        }, 20L * 15, 20L * 15);
    }
}