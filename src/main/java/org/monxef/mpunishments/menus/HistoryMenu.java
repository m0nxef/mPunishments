package org.monxef.mpunishments.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.monxef.mpunishments.MPunishments;
import org.monxef.mpunishments.data.PunishmentData;
import org.monxef.mpunishments.enums.PunishmentType;
import org.monxef.mpunishments.managers.ConfigManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryMenu implements org.bukkit.inventory.InventoryHolder {

    private final MPunishments plugin;
    private final OfflinePlayer target;
    private final List<PunishmentData> punishments;
    private Inventory inventory;
    private final ConfigManager configManager;
    private int currentPage;
    private int totalPages;
    private PunishmentType currentFilterType = null;
    private Boolean showOnlyActive = null;

    public HistoryMenu(MPunishments plugin, OfflinePlayer target) {
        this.plugin = plugin;
        this.target = target;
        this.configManager = plugin.getConfigManager();
        this.punishments = plugin.getDatabaseManager().getStorage().getPunishments(target.getUniqueId());
        this.currentPage = 1;
        this.totalPages = (int) Math.ceil((double) punishments.size() / 45);
        initializeInventory();
        initializeItems();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void initializeInventory() {
        String title = configManager.getString("history_menu.title").replace("%player%", target.getName());
        this.inventory = Bukkit.createInventory(this, 54, ChatColor.translateAlternateColorCodes('&', title));
    }

    private void initializeItems() {
        inventory.clear();
        List<PunishmentData> filteredPunishments = new ArrayList<>(punishments);

        if (currentFilterType != null) {
            filteredPunishments = filteredPunishments.stream()
                    .filter(p -> p.getType() == currentFilterType)
                    .collect(Collectors.toList());
        }

        if (showOnlyActive != null) {
            filteredPunishments = filteredPunishments.stream()
                    .filter(p -> showOnlyActive == p.isActive())
                    .collect(Collectors.toList());
        }

        totalPages = (int) Math.ceil((double) filteredPunishments.size() / 45);

        int startIndex = (currentPage - 1) * 45;
        int endIndex = Math.min(startIndex + 45, filteredPunishments.size());

        for (int i = startIndex; i < endIndex; i++) {
            if (i >= filteredPunishments.size()) break;
            PunishmentData punishment = filteredPunishments.get(i);

            ItemStack punishmentItem = new ItemStack(Material.PAPER);
            ItemMeta meta = punishmentItem.getItemMeta();
            meta.setDisplayName(ChatColor.RED + punishment.getType().toString());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            List<String> lore = List.of(
                    ChatColor.GRAY + "Punisher: " + ChatColor.WHITE + punishment.getPunisherName(),
                    ChatColor.GRAY + "Reason: " + ChatColor.WHITE + punishment.getReason(),
                    ChatColor.GRAY + "Date: " + ChatColor.WHITE + dateFormat.format(punishment.getDate()),
                    ChatColor.GRAY + "Status: " + ChatColor.WHITE + (punishment.isActive() ? "Active" : "Expired"),
                    punishment.getExpiry() != null ? ChatColor.GRAY + "Expiry: " + ChatColor.WHITE + dateFormat.format(punishment.getExpiry()) : ChatColor.GRAY + "Expiry: " + ChatColor.WHITE + "Permanent"
            );
            meta.setLore(lore);
            punishmentItem.setItemMeta(meta);

            inventory.setItem(i - startIndex, punishmentItem);
        }

        ItemStack previousPage = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previousPage.getItemMeta();
        previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
        previousPage.setItemMeta(previousMeta);

        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        nextPage.setItemMeta(nextMeta);

        if (totalPages > 1) {
            if (currentPage > 1) {
                inventory.setItem(45, previousPage);
            }
            if (currentPage < totalPages) {
                inventory.setItem(53, nextPage);
            }
        }

        ItemStack filterTypeButton = new ItemStack(Material.BOOK);
        ItemMeta filterTypeMeta = filterTypeButton.getItemMeta();
        filterTypeMeta.setDisplayName(ChatColor.BLUE + "Filter Type: " + (currentFilterType == null ? "All" : currentFilterType.toString()));
        filterTypeButton.setItemMeta(filterTypeMeta);
        inventory.setItem(47, filterTypeButton);

        ItemStack activeExpiredButton = new ItemStack(Material.CLOCK);
        ItemMeta activeExpiredMeta = activeExpiredButton.getItemMeta();
        String activeExpiredText = showOnlyActive == null ? "All" : (showOnlyActive ? "Active" : "Expired");
        activeExpiredMeta.setDisplayName(ChatColor.GOLD + "Show: " + activeExpiredText);
        activeExpiredButton.setItemMeta(activeExpiredMeta);
        inventory.setItem(49, activeExpiredButton);
    }

    public void handleInventoryClick(Player player, ItemStack clickedItem) {
        if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

        String displayName = clickedItem.getItemMeta().getDisplayName();

        if (displayName.equals(ChatColor.GREEN + "Previous Page")) {
            if (currentPage > 1) {
                currentPage--;
                initializeItems();
                player.openInventory(inventory);
            }
        } else if (displayName.equals(ChatColor.GREEN + "Next Page")) {
            if (currentPage < totalPages) {
                currentPage++;
                initializeItems();
                player.openInventory(inventory);
            }
        } else if (displayName.startsWith(ChatColor.BLUE + "Filter Type:")) {
            if (currentFilterType == null) {
                currentFilterType = PunishmentType.values()[0];
            } else {
                int currentIndex = java.util.Arrays.asList(PunishmentType.values()).indexOf(currentFilterType);
                currentFilterType = PunishmentType.values()[(currentIndex + 1) % PunishmentType.values().length];
                if (currentFilterType == PunishmentType.values()[0]) {
                    currentFilterType = null;
                }
            }
            currentPage = 1;
            initializeItems();
            player.openInventory(inventory);
        } else if (displayName.startsWith(ChatColor.GOLD + "Show:")) {
            if (showOnlyActive == null) {
                showOnlyActive = true;
            } else if (showOnlyActive) {
                showOnlyActive = false;
            } else {
                showOnlyActive = null;
            }
            currentPage = 1;
            initializeItems();
            player.openInventory(inventory);
        }
    }
}