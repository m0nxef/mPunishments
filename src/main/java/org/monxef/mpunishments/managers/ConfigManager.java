package org.monxef.mpunishments.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.utils.MessagesUtils;

import java.util.List;

public class ConfigManager {

    private final MPunishments plugin;
    private FileConfiguration config;

    public ConfigManager(MPunishments plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public boolean getBoolean(String path){
        return config.getBoolean(path);
    }

    // Messages Section
    public String getMessage(String path) {
        return MessagesUtils.format(getString("messages." + path));
    }

    public String getPrefix() {
        return getMessage("prefix");
    }

    public String getNoPermissionMessage() {
        return getMessage("no_permission");
    }

    public String getSelfTargetMessage() { return getMessage("self_target"); }
    public String getInvalidDurationMessage() { return getMessage("invalid_duration"); }
    public String getDurationTooLongMessage() { return getMessage("duration_too_long"); }
    public String getDurationTooShortMessage() { return getMessage("duration_too_short"); }
    public String getAlreadyPunishedMessage() { return getMessage("already_punished"); }
    public String getPunishmentRemovedMessage() { return getMessage("punishment_removed"); }
    public String getPunishmentAppliedMessage() { return getMessage("punishment_applied"); }
    public String getStaffNotificationMessage() { return getMessage("staff_notification"); }
    public String getPublicNotificationMessage() { return getMessage("public_notification"); }

    // Ban Messages
    public String getBanMessage(String path){ return getMessage("ban."+path);}
    //Mute Messages
    public String getMuteMessage(String path){ return getMessage("mute."+path);}
    //Kick Messages
    public String getKickMessage(String path){ return getMessage("kick."+path);}

    // History Menu Section
    public String getHistoryMenuTitle() {
        return getString("history_menu.title");
    }

    public Material getHistoryMenuItemMaterial() {
        String materialName = getString("history_menu.item_display.material");
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material '" + materialName + "' in history_menu config. Using PAPER as default.");
            return Material.PAPER;
        }
    }

    public String getHistoryMenuItemName() {
        return getString("history_menu.item_display.name");
    }

    public List<String> getHistoryMenuItemLore() {
        return getStringList("history_menu.item_display.lore");
    }

    // Database Configuration
    public String getDatabaseType() {
        return getString("database.type");
    }

    public String getSQLiteFilename() {
        return getString("database.filename");
    }

    public String getMySQLHost() {
        return getString("database.host");
    }

    public int getMySQLPort() {
        return getInt("database.port");
    }

    public String getMySQLDatabase() {
        return getString("database.database");
    }

    public String getMySQLUsername() {
        return getString("database.username");
    }

    public String getMySQLPassword() {
        return getString("database.password");
    }
    public boolean getUseSSL(){
        return getBoolean("database.ssl");
    }

    public String getMongoDBConnectionString() {
        return getString("database.connectionString");
    }
    public String getMongoDBDatabaseName() {
        return getString("database.databaseName");
    }
    public String getMongoDBCollectionName() {
        return getString("database.collectionName");
    }
}