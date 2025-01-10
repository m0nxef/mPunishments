package org.monxef.mpunishments.managers;

import lombok.Getter;
import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.data.DataStorage;
import org.monxef.mpunishments.data.impl.MongoDBManager;
import org.monxef.mpunishments.data.impl.MySQLManager;
import org.monxef.mpunishments.data.impl.SQLiteManager;

public class DatabaseManager {
    private MPunishments plugin;
    @Getter
    private DataStorage storage;

    public DatabaseManager(MPunishments plugin) {
        this.plugin = plugin;
        String type = plugin.getConfigManager().getDatabaseType().toUpperCase();
        switch (type) {
            case "SQLITE":
                this.storage = new SQLiteManager(plugin);
                break;
            case "MYSQL":
                this.storage = new MySQLManager(plugin);
                break;
            case "MONGODB":
                java.util.logging.Logger.getLogger("org.mongodb.driver.connection").setLevel(java.util.logging.Level.OFF);
                java.util.logging.Logger.getLogger("org.mongodb.driver.management").setLevel(java.util.logging.Level.OFF);
                java.util.logging.Logger.getLogger("org.mongodb.driver.cluster").setLevel(java.util.logging.Level.OFF);
                java.util.logging.Logger.getLogger("org.mongodb.driver.protocol.insert").setLevel(java.util.logging.Level.OFF);
                java.util.logging.Logger.getLogger("org.mongodb.driver.protocol.query").setLevel(java.util.logging.Level.OFF);
                java.util.logging.Logger.getLogger("org.mongodb.driver.protocol.update").setLevel(java.util.logging.Level.OFF);
               this.storage = new MongoDBManager(plugin.getConfigManager().getMongoDBConnectionString());
                break;
            default:
                plugin.getLogger().severe("Invalid database type specified in config.yml");
                return;
        }
    }

    public boolean connect() {
        return storage.connect();
    }

    public void disconnect() {
        storage.disconnect();
    }

}