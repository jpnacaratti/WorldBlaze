package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.AnimalGameGamblersGui;
import com.worldplugins.worldblaze.guis.AnimalGameMainGui;
import com.worldplugins.worldblaze.guis.MenuHolder;
import com.worldplugins.worldblaze.guis.MenuType;
import com.worldplugins.worldblaze.utils.NBTTag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;

public class InventoryClickAnimalGameGamblers implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if (!MenuHolder.handleClick(e, MenuType.ANIMAL_GAME_BETTORS)) return;

        if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        Player p = (Player) e.getWhoClicked();

        String nbtPage = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_animalPage");

        if(nbtPage != null){
            p.openInventory(AnimalGameGamblersGui.invs.get(p).get(Integer.parseInt(nbtPage)));
            return;
        }

        FileConfiguration getConfig = getAnimalGameGamblersGuiYml().getConfig();

        if(e.getSlot() == getConfig.getInt("items.back.slot")) {

            String nbt = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_animalBack");

            if(nbt != null) p.openInventory(getAnimalGameManager().getSeeingMap().get(p.getName()));
            else AnimalGameMainGui.create(p);

        }

    }

}
