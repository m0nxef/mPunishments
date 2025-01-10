package org.monxef.mpunishments.data.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.monxef.mpunishments.data.DataStorage;
import org.monxef.mpunishments.data.PunishmentData;
import org.monxef.mpunishments.enums.PunishmentType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MongoDBManager implements DataStorage {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final String collectionName = "punishments";

    public MongoDBManager(String mongoURI) {
        this.mongoClient = MongoClients.create(mongoURI);
        String databaseName = getDatabaseNameFromURI(mongoURI);
        this.database = mongoClient.getDatabase(databaseName);
    }

    private String getDatabaseNameFromURI(String mongoURI) {
        try {
            // Extract the database name from the connection string
            String[] parts = mongoURI.split("/");
            if (parts.length > 3) {
                String potentialDbName = parts[3].split("\\?")[0];
                if (!potentialDbName.isEmpty()) {
                    return potentialDbName;
                }
            }
            // Fallback if parsing fails. You might want to throw an exception here in a real application.
            return "mpunishments"; // Default database name
        } catch (Exception e) {
            System.err.println("Error parsing MongoDB URI: " + e.getMessage());
            return "mpunishments";
        }
    }


    @Override
    public boolean connect() {
        // Connection is handled by the MongoClient constructor. Check if the connection is valid by pinging the database.
        try {
            database.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void disconnect() {
        mongoClient.close();
    }

    private MongoCollection<Document> getCollection() {
        return database.getCollection(collectionName);
    }

    @Override
    public void savePunishment(PunishmentData punishment) {
        Document doc = new Document();
        doc.put("_id", punishment.getId().toString()); // Use the provided UUID as a string _id
        doc.put("player_uuid", punishment.getPlayerUUID() != null ? punishment.getPlayerUUID().toString() : null);
        doc.put("player_name", punishment.getPlayerName());
        doc.put("punisher_name", punishment.getPunisherName());
        doc.put("reason", punishment.getReason());
        doc.put("type", punishment.getType().toString());
        doc.put("date", punishment.getDate().getTime());
        doc.put("expiry", punishment.getExpiry() != null ? punishment.getExpiry().getTime() : null);
        doc.put("ip_address", punishment.getIpAddress());
        doc.put("active", punishment.isActive());

        getCollection().insertOne(doc);
    }

    @Override
    public List<PunishmentData> getPunishments(UUID playerUUID) {
        return getPunishmentsByFilter(Filters.eq("player_uuid", playerUUID.toString()));
    }

    @Override
    public List<PunishmentData> getActivePunishments(UUID playerUUID) {
        return getPunishmentsByFilter(Filters.and(Filters.eq("player_uuid", playerUUID.toString()), Filters.eq("active", true)));
    }

    @Override
    public List<PunishmentData> getAllActivePunishments() {
        return getPunishmentsByFilter(Filters.eq("active", true));
    }

    @Override
    public List<PunishmentData> getExpiredPunishments() {
        return getPunishmentsByFilter(Filters.and(Filters.lt("expiry", System.currentTimeMillis()), Filters.eq("active", true)));
    }

    @Override
    public void updatePunishmentActiveStatus(UUID id, boolean active) {
        getCollection().updateOne(Filters.eq("_id", id.toString()), Updates.set("active", active));
    }

    @Override
    public void saveIPBan(PunishmentData punishment) {
        savePunishment(punishment);
    }

    @Override
    public List<PunishmentData> getIPBans(String ipAddress) {
        return getPunishmentsByFilter(Filters.and(Filters.eq("type", PunishmentType.IPBAN.toString()), Filters.eq("ip_address", ipAddress), Filters.eq("active", true)));
    }

    private List<PunishmentData> getPunishmentsByFilter(Bson filter) {
        List<PunishmentData> punishments = new ArrayList<>();
        for (Document doc : getCollection().find(filter)) {
            PunishmentData punishment = createPunishmentFromDocument(doc);
            if (punishment != null) {
                punishments.add(punishment);
            }
        }
        return punishments;
    }


    private PunishmentData createPunishmentFromDocument(Document doc) {
        try {
            UUID id = UUID.fromString(doc.getString("_id"));
            String playerUUIDString = doc.getString("player_uuid");
            UUID playerUUID = playerUUIDString != null ? UUID.fromString(playerUUIDString) : null;
            String playerName = doc.getString("player_name");
            String punisherName = doc.getString("punisher_name");
            String reason = doc.getString("reason");
            String typeString = doc.getString("type");
            long dateLong = doc.getLong("date");
            Date date = new Date(dateLong);
            Long expiryLong = doc.getLong("expiry");
            Date expiry = expiryLong != null && expiryLong != 0 ? new Date(expiryLong) : null;
            String ipAddress = doc.getString("ip_address");
            boolean active = doc.getBoolean("active");

            return new PunishmentData(id, playerUUID, playerName, punisherName, reason, typeString, date, expiry, ipAddress, active);
        } catch (IllegalArgumentException | NullPointerException e) {
            System.err.println("Error creating PunishmentData from document: " + e.getMessage() + " Document: " + doc);
            return null;
        }
    }
}