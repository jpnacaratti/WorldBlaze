package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.enums.Animal;
import com.worldplugins.worldblaze.guis.AnimalGameBetGui;
import com.worldplugins.worldblaze.guis.AnimalGameAnimalsGui;
import com.worldplugins.worldblaze.guis.AnimalGameMainGui;
import com.worldplugins.worldblaze.guis.MenuHolder;
import com.worldplugins.worldblaze.guis.MenuType;
import com.worldplugins.worldblaze.utils.NBTTag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.getAnimalsGuiYml;
import static com.worldplugins.worldblaze.WorldBlaze.getAnimalGameGuiYml;
import static com.worldplugins.worldblaze.utils.Utils.c;

public class InventoryClickAnimalGameAnimals implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!MenuHolder.handleClick(e, MenuType.ANIMAL_GAME_ANIMALS)) return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getAnimalsGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == getConfig.getInt("items.back.slot")){
            AnimalGameMainGui.create(p);
            return;
        }

        String nbt = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_animal");

        if(nbt == null) return;

        Animal animal;
        try{

            animal = Animal.valueOf(nbt);

        }catch (IllegalArgumentException ex){
            p.sendMessage("§cUnable to bet on this animal, contact Miinero! ("+nbt+")");
            return;
        }

        AnimalGameBetGui.create(p, animal);
    }
}