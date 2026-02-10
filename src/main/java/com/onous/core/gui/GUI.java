package com.onous.core.gui;

import com.onous.core.OnousCore;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Base GUI Class - Minimalist Design
 */
public abstract class GUI implements InventoryHolder {

    protected final OnousCore plugin;
    protected final Player player;
    protected final Inventory inventory;
    protected final String title;
    protected final int size;

    public GUI(OnousCore plugin, Player player, String title, int rows) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.size = Math.min(6, Math.max(1, rows)) * 9;
        this.inventory = Bukkit.createInventory(this, size, CC.translate(title));
    }

    public abstract void setup();
    public abstract void onClick(int slot);

    public void open() {
        setup();
        player.openInventory(inventory);
        Sounds.open(player);
    }

    public void close() {
        player.closeInventory();
    }

    public void refresh() {
        inventory.clear();
        setup();
    }

    protected void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot < size) {
            inventory.setItem(slot, item);
        }
    }

    protected int getTotalPages(int totalItems, int itemsPerPage) {
        if (totalItems <= 0) return 1;
        return (int) Math.ceil((double) totalItems / itemsPerPage);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }
}