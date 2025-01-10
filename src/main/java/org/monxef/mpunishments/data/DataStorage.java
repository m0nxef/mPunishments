package org.monxef.mpunishments.data;

import java.util.List;
import java.util.UUID;

public interface DataStorage {
    boolean connect();
    void disconnect();
    void savePunishment(PunishmentData punishment);
    List<PunishmentData> getPunishments(UUID playerUUID);
    List<PunishmentData> getActivePunishments(UUID playerUUID);
    List<PunishmentData> getAllActivePunishments();
    List<PunishmentData> getExpiredPunishments();
    void updatePunishmentActiveStatus(UUID id, boolean active);
    void saveIPBan(PunishmentData punishment);
    List<PunishmentData> getIPBans(String ipAddress);
}