package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.JogoDoBichoBichosGui;
import com.worldplugins.worldblaze.guis.JogoDoBichoGamblersGui;
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

        if (!e.getInventory().getName().equalsIgnoreCase(c(getJogoDoBichoGuiYml().getConfig().getString("Titulo")))) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getJogoDoBichoGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == getConfig.getInt("Itens.bichos.Slot")) JogoDoBichoBichosGui.create(p);

        if (e.getSlot() == getConfig.getInt("Itens.apostadores.Slot")) JogoDoBichoGamblersGui.create(p, null);

    }
}