package org.monxef.mpunishments;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.monxef.mpunishments.commands.*;
import org.monxef.mpunishments.listeners.AsyncPlayerChatListener;
import org.monxef.mpunishments.listeners.InventoryClick;
import org.monxef.mpunishments.listeners.PlayerJoinListener;
import org.monxef.mpunishments.listeners.PlayerQuit;
import org.monxef.mpunishments.managers.CacheManager;
import org.monxef.mpunishments.managers.DatabaseManager;
import org.monxef.mpunishments.managers.ConfigManager;
import org.monxef.mpunishments.tasks.PluginLoadingTask;
@Getter
public class MPunishments extends JavaPlugin {
    @Getter
    private ConfigManager configManager;
    @Setter private DatabaseManager databaseManager;
    @Setter private CacheManager cacheManager;
    @Override
    public void onEnable() {
        getLogger().info("mPunishments has been enabled!");
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        Bukkit.getScheduler().runTaskAsynchronously(this, new PluginLoadingTask(this));
    }
    @Override
    public void onDisable() {
        if(databaseManager != null) databaseManager.disconnect();
        getLogger().info("mPunishments has been disabled!");
    }

    public void registerCommands(){
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("tempban").setExecutor(new TempbanCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("tempmute").setExecutor(new TempmuteCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getCommand("ipban").setExecutor(new IPBanCommand(this));
        getCommand("punishmenthistory").setExecutor(new PunishmentHistoryCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClick(), this);
    }

}