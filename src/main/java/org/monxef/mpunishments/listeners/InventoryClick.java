package org.monxef.mpunishments.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.monxef.mpunishments.menus.HistoryMenu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryClick implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof HistoryMenu) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
                return;
            }

            ((HistoryMenu) holder).handleInventoryClick(player, clickedItem);
        }
    }

}
