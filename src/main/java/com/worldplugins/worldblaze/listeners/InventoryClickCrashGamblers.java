package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.CrashGamblersGui;
import com.worldplugins.worldblaze.guis.CrashGui;
import com.worldplugins.worldblaze.guis.GraphGamblersGui;
import com.worldplugins.worldblaze.guis.GraphGui;
import com.worldplugins.worldblaze.utils.NBTTag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;

public class InventoryClickCrashGamblers implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if(!e.getInventory().getName().equalsIgnoreCase(c(getCrashGamblersGuiYml().getConfig().getString("title")))) return;
        e.setCancelled(true);

        if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        Player p = (Player) e.getWhoClicked();

        String nbtPag = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_crashPag");

        if(nbtPag != null){
            p.openInventory(CrashGamblersGui.invs.get(p).get(Integer.parseInt(nbtPag)));
            return;
        }

        FileConfiguration getConfig = getCrashGamblersGuiYml().getConfig();

        if(e.getSlot() == getConfig.getInt("items.back.slot")){

            getCrashManager().getGamblersGuiOpen().remove(p.getName());

            if(getCrashManager().getCrashInventories().containsKey(p.getName())) p.openInventory(getCrashManager().getCrashInventories().get(p.getName()));
            else CrashGui.create(p);

        }

    }

}
