package org.monxef.mpunishments.tasks;

import org.bukkit.Bukkit;
import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.managers.CacheManager;
import org.monxef.mpunishments.managers.DatabaseManager;

public class PluginLoadingTask implements Runnable {

    private final MPunishments plugin;

    public PluginLoadingTask(MPunishments plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        DatabaseManager databaseManager = new DatabaseManager(plugin);
        if (!databaseManager.connect()) {
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
        plugin.setDatabaseManager(databaseManager);
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().info("Database connected successfully.");
            plugin.registerCommands();
        });
        plugin.setCacheManager(new CacheManager(plugin));

    }
}