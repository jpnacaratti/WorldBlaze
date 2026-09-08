package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.CrashGamblersGui;
import com.worldplugins.worldblaze.guis.CrashGui;
import com.worldplugins.worldblaze.guis.DoubleGamblersGui;
import com.worldplugins.worldblaze.guis.DoubleGui;
import com.worldplugins.worldblaze.utils.NBTTag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;

public class InventoryClickDoubleGamblers implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if(!e.getInventory().getName().equalsIgnoreCase(c(getDoubleGamblersGuiYml().getConfig().getString("Titulo")))) return;
        e.setCancelled(true);

        if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        Player p = (Player) e.getWhoClicked();

        String nbtPag = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_doublePag");

        if(nbtPag != null){
            p.openInventory(DoubleGamblersGui.invs.get(p).get(Integer.parseInt(nbtPag)));
            return;
        }

        FileConfiguration getConfig = getDoubleGamblersGuiYml().getConfig();

        if(e.getSlot() == getConfig.getInt("Itens.voltar.Slot")){

            getDoubleManager().getGamblersGuiOpen().remove(p.getName());

            if(getDoubleManager().getDoubleInventories().containsKey(p.getName())) p.openInventory(getDoubleManager().getDoubleInventories().get(p.getName()));
            else DoubleGui.create(p);

        }

    }

}
