package org.monxef.mpunishments.data.impl;

import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.data.DataStorage;
import org.monxef.mpunishments.data.PunishmentData;
import org.monxef.mpunishments.enums.PunishmentType;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SQLiteManager implements DataStorage {

    private final MPunishments plugin;
    private Connection connection;

    public SQLiteManager(MPunishments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean connect() {
        File dbFile = new File(plugin.getDataFolder(), plugin.getConfigManager().getSQLiteFilename());
        if (!dbFile.exists()) {
            try {
                if (!dbFile.createNewFile()) return false;
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create SQLite database file: " + e.getMessage());
                return false;
            }
        }

        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            this.connection = DriverManager.getConnection(url);
            createTables();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to connect to SQLite database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void createTables() throws SQLException {
        String punishmentsTableSQL = "CREATE TABLE IF NOT EXISTS punishments (" +
                "id TEXT PRIMARY KEY," +
                "player_uuid TEXT," +
                "player_name TEXT NOT NULL," +
                "punisher_name TEXT NOT NULL," +
                "reason TEXT," +
                "type TEXT NOT NULL," +
                "date INTEGER NOT NULL," +
                "expiry INTEGER," +
                "ip_address TEXT," +
                "active BOOLEAN NOT NULL DEFAULT TRUE" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(punishmentsTableSQL);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing SQLite connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void savePunishment(PunishmentData punishment) {
        // First deactivate existing active punishments of the same type
        String updateSql = "UPDATE punishments SET active = FALSE WHERE player_uuid = ? AND type = ? AND active = TRUE";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
            updateStatement.setString(1, punishment.getPlayerUUID().toString());
            updateStatement.setString(2, punishment.getType().toString());
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error deactivating old punishments: " + e.getMessage());
            e.printStackTrace();
        }

        // Then save the new punishment
        String insertSql = "INSERT INTO punishments (id, player_uuid, player_name, punisher_name, reason, type, date, expiry, ip_address, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setString(1, punishment.getId().toString());
            statement.setString(2, punishment.getPlayerUUID() != null ? punishment.getPlayerUUID().toString() : null);
            statement.setString(3, punishment.getPlayerName());
            statement.setString(4, punishment.getPunisherName());
            statement.setString(5, punishment.getReason());
            statement.setString(6, punishment.getType().toString());
            statement.setLong(7, punishment.getDate().getTime());
            Long expiryTime = punishment.getExpiry() != null ? punishment.getExpiry().getTime() : null;
            statement.setLong(8, expiryTime != null ? expiryTime : 0);
            statement.setString(9, punishment.getIpAddress());
            statement.setBoolean(10, punishment.isActive());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error saving punishment to SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public List<PunishmentData> getPunishments(UUID playerUUID) {
        List<PunishmentData> punishments = new ArrayList<>();
        String sql = "SELECT * FROM punishments WHERE player_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PunishmentData punishment = createPunishmentFromResultSet(resultSet);
                    if (punishment != null) {
                        punishments.add(punishment);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting punishments from SQLite: " + e.getMessage());
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public List<PunishmentData> getActivePunishments(UUID playerUUID) {
        List<PunishmentData> punishments = new ArrayList<>();
        String sql = "SELECT * FROM punishments WHERE player_uuid = ? AND active = TRUE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PunishmentData punishment = createPunishmentFromResultSet(resultSet);
                    if (punishment != null) {
                        punishments.add(punishment);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting active punishments from SQLite: " + e.getMessage());
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public List<PunishmentData> getAllActivePunishments() {
        List<PunishmentData> punishments = new ArrayList<>();
        String sql = "SELECT * FROM punishments WHERE active = TRUE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PunishmentData punishment = createPunishmentFromResultSet(resultSet);
                    if (punishment != null) {
                        punishments.add(punishment);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting all active punishments from SQLite: " + e.getMessage());
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public List<PunishmentData> getExpiredPunishments() {
        List<PunishmentData> punishments = new ArrayList<>();
        String sql = "SELECT * FROM punishments WHERE expiry < ? AND active = TRUE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, System.currentTimeMillis());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PunishmentData punishment = createPunishmentFromResultSet(resultSet);
                    if (punishment != null&&punishment.getExpiry()!=null) {
                        punishments.add(punishment);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting expired punishments from SQLite: " + e.getMessage());
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public void updatePunishmentActiveStatus(UUID id, boolean active) {
        String sql = "UPDATE punishments SET active = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setString(2, id.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating punishment active status in SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveIPBan(PunishmentData punishment) {
        savePunishment(punishment);
    }

    @Override
    public List<PunishmentData> getIPBans(String ipAddress) {
        List<PunishmentData> ipBans = new ArrayList<>();
        String sql = "SELECT * FROM punishments WHERE type = ? AND ip_address = ? AND active = TRUE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, PunishmentType.IPBAN.toString());
            statement.setString(2, ipAddress);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PunishmentData punishment = createPunishmentFromResultSet(resultSet);
                    if (punishment != null) {
                        ipBans.add(punishment);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting IP bans from SQLite: " + e.getMessage());
            e.printStackTrace();
        }
        return ipBans;
    }

    private PunishmentData createPunishmentFromResultSet(ResultSet resultSet) throws SQLException {
        try {
            String idString = resultSet.getString("id");
            if (idString == null) {
                plugin.getLogger().warning("Null ID found in database.");
                return null;
            }
            UUID id = UUID.fromString(idString);

            String playerUUIDString = resultSet.getString("player_uuid");
            UUID playerUUID = null;
            if (playerUUIDString != null) {
                try {
                    playerUUID = UUID.fromString(playerUUIDString);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid player UUID found in database: " + playerUUIDString);
                }
            }

            String playerName = resultSet.getString("player_name");
            if (playerName == null) {
                plugin.getLogger().warning("Null playerName found in database.");
                return null;
            }

            String punisherName = resultSet.getString("punisher_name");
            if (punisherName == null) {
                plugin.getLogger().warning("Null punisherName found in database.");
                return null;
            }
            String reason = resultSet.getString("reason");

            String typeString = resultSet.getString("type");
            if (typeString == null) {
                plugin.getLogger().warning("Null PunishmentType found in database.");
                return null;
            }

            long dateLong = resultSet.getLong("date");
            Date date = new Date(dateLong);

            long expiryLong = resultSet.getLong("expiry");
            Date expiry = (expiryLong == 0) ? null : new Date(expiryLong);

            String ipAddress = resultSet.getString("ip_address");
            boolean active = resultSet.getBoolean("active");

            return new PunishmentData(id, playerUUID, playerName, punisherName, reason, typeString, date, expiry, ipAddress, active);

        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating PunishmentData from ResultSet: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}