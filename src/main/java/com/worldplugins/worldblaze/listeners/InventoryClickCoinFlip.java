package com.worldplugins.worldblaze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.getCoinFlipManager;

public class InventoryClickCoinFlip implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!e.getInventory().getName().equalsIgnoreCase(getCoinFlipManager().getCoinFlipMenuTitle())) return;
        e.setCancelled(true);

    }
}