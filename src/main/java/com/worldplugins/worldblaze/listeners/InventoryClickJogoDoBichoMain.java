package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.JogoDoBichoBichosGui;
import com.worldplugins.worldblaze.guis.JogoDoBichoGamblersGui;
import com.worldplugins.worldblaze.guis.MenuHolder;
import com.worldplugins.worldblaze.guis.MenuType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;

public class InventoryClickJogoDoBichoMain implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!MenuHolder.is(e.getInventory(), MenuType.ANIMAL_GAME_MAIN)) return;
        e.setCancelled(true);
        if (e.getRawSlot() >= e.getInventory().getSize()) return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getJogoDoBichoGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == getConfig.getInt("items.bichos.slot")) JogoDoBichoBichosGui.create(p);

        if (e.getSlot() == getConfig.getInt("items.bettors.slot")) JogoDoBichoGamblersGui.create(p, null);

    }
}