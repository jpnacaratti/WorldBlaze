package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;

public class InventoryClickMineMain implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!MenuHolder.is(e.getInventory(), MenuType.MINE_MAIN)) return;
        e.setCancelled(true);
        if (e.getRawSlot() >= e.getInventory().getSize()) return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getMainMineGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == getConfig.getInt("items.create.slot")) MineCreateGui.create(p);

        if (e.getSlot() == getConfig.getInt("items.players.slot")) MinePlayersGui.create(p);

    }
}