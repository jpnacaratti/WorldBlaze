package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.enums.Animal;
import com.worldplugins.worldblaze.guis.JogoDoBichoBetGui;
import com.worldplugins.worldblaze.guis.JogoDoBichoBichosGui;
import com.worldplugins.worldblaze.guis.JogoDoBichoMainGui;
import com.worldplugins.worldblaze.guis.MenuHolder;
import com.worldplugins.worldblaze.guis.MenuType;
import com.worldplugins.worldblaze.utils.NBTTag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.getBichosGuiYml;
import static com.worldplugins.worldblaze.WorldBlaze.getJogoDoBichoGuiYml;
import static com.worldplugins.worldblaze.utils.Utils.c;

public class InventoryClickJogoDoBichoBichos implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!MenuHolder.is(e.getInventory(), MenuType.ANIMAL_GAME_ANIMALS)) return;
        e.setCancelled(true);
        if (e.getRawSlot() >= e.getInventory().getSize()) return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getBichosGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == getConfig.getInt("items.back.slot")){
            JogoDoBichoMainGui.create(p);
            return;
        }

        String nbt = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_bicho");

        if(nbt == null) return;

        Animal animal;
        try{

            animal = Animal.valueOf(nbt);

        }catch (IllegalArgumentException ex){
            p.sendMessage("§cUnable to bet on this animal, contact Miinero! ("+nbt+")");
            return;
        }

        JogoDoBichoBetGui.create(p, animal);
    }
}