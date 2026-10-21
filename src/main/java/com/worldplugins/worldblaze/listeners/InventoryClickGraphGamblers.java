package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.GraphGamblersGui;
import com.worldplugins.worldblaze.guis.GraphGui;
import com.worldplugins.worldblaze.utils.NBTTag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class InventoryClickGraphGamblers implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if(!e.getInventory().getName().equalsIgnoreCase(c(getGraphGamblersGuiYml().getConfig().getString("title")))) return;
        e.setCancelled(true);

        if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        Player p = (Player) e.getWhoClicked();

        String nbtPag = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_graphPag");

        if(nbtPag != null){
            p.openInventory(GraphGamblersGui.invs.get(p).get(Integer.parseInt(nbtPag)));
            return;
        }

        FileConfiguration getConfig = getGraphGamblersGuiYml().getConfig();

        if(e.getSlot() == getConfig.getInt("items.back.slot")){

            getGraphManager().getGamblersGuiOpen().remove(p.getName());

            if(getGraphManager().getGraphInventories().containsKey(p.getName())) p.openInventory(getGraphManager().getGraphInventories().get(p.getName()));
            else GraphGui.create(p);

        }

    }

}
