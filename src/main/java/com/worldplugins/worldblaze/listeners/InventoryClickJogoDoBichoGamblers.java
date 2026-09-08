package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.JogoDoBichoGamblersGui;
import com.worldplugins.worldblaze.guis.JogoDoBichoMainGui;
import com.worldplugins.worldblaze.utils.NBTTag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;

public class InventoryClickJogoDoBichoGamblers implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if(!e.getInventory().getName().equalsIgnoreCase(c(getJogoDoBichoGamblersGuiYml().getConfig().getString("Titulo")))) return;
        e.setCancelled(true);

        if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        Player p = (Player) e.getWhoClicked();

        String nbtPag = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_bichosPag");

        if(nbtPag != null){
            p.openInventory(JogoDoBichoGamblersGui.invs.get(p).get(Integer.parseInt(nbtPag)));
            return;
        }

        FileConfiguration getConfig = getJogoDoBichoGamblersGuiYml().getConfig();

        if(e.getSlot() == getConfig.getInt("Itens.voltar.Slot")) {

            String nbt = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_bichoVoltar");

            if(nbt != null) p.openInventory(getJogoDoBichoManager().getSeeingMap().get(p.getName()));
            else JogoDoBichoMainGui.create(p);

        }

    }

}
