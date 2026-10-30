package com.worldplugins.worldblaze.guis;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {

    private final MenuType type;
    private Inventory inventory;

    private MenuHolder(MenuType type) {
        this.type = type;
    }

    public static Inventory create(MenuType type, int size, String title) {
        MenuHolder holder = new MenuHolder(type);
        holder.inventory = Bukkit.createInventory(holder, size, title);
        return holder.inventory;
    }

    public static boolean is(Inventory inventory, MenuType type) {
        if (inventory == null) return false;
        return inventory.getHolder() instanceof MenuHolder && ((MenuHolder) inventory.getHolder()).type == type;
    }

    // cancels the click and tells if it happened in the menu itself, not in the player's inventory
    public static boolean handleClick(InventoryClickEvent e, MenuType type) {
        if (!is(e.getInventory(), type)) return false;
        e.setCancelled(true);
        return e.getRawSlot() < e.getInventory().getSize();
    }

    public MenuType getType() {
        return type;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

}
