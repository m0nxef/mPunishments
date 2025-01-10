package org.monxef.mpunishments.data;

import lombok.Data;
import org.monxef.mpunishments.enums.PunishmentType; // Import the enum

import java.util.Date;
import java.util.UUID;
@Data
public class PunishmentData {
    private UUID id;
    private UUID playerUUID;
    private String playerName;
    private String punisherName;
    private String reason;
    private PunishmentType type;
    private Date date;
    private Date expiry;
    private String ipAddress;
    private boolean active;

    public PunishmentData(UUID playerUUID, String playerName, String punisherName, String reason, PunishmentType type, Date date, Date expiry, String ipAddress) {
        this.id = UUID.randomUUID();
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.punisherName = punisherName;
        this.reason = reason;
        this.type = type;
        this.date = date;
        this.expiry = expiry;
        this.ipAddress = ipAddress;
        this.active = true;
    }
    public PunishmentData(UUID playerUUID, String playerName, String punisherName, String reason, PunishmentType type, Date date, Date expiry) {
        this(playerUUID, playerName, punisherName, reason, type, date, expiry, null);
    }
    public PunishmentData(UUID id, UUID playerUUID, String playerName, String punisherName, String reason, String typeString, Date date, Date expiry, String ipAddress, boolean active) { // typeString
        this.id = id;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.punisherName = punisherName;
        this.reason = reason;
        try {
            this.type = PunishmentType.valueOf(typeString); // Convert String to enum
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid PunishmentType in database: " + typeString);
            this.type = null;
        }
        this.date = date;
        this.expiry = expiry;
        this.ipAddress = ipAddress;
        this.active = active;
    }
    public boolean isExpired() {
        return expiry != null && expiry.before(new Date());
    }
}