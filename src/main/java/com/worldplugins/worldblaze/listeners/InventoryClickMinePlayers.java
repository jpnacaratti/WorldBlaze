package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.MineMainGui;
import com.worldplugins.worldblaze.guis.MinePlayersGui;
import com.worldplugins.worldblaze.objects.MineBet;
import com.worldplugins.worldblaze.utils.NBTTag;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;

public class InventoryClickMinePlayers implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if(!e.getInventory().getName().equalsIgnoreCase(c(getMinePlayersGuiYml().getConfig().getString("title")))) return;
        e.setCancelled(true);

        if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        Player p = (Player) e.getWhoClicked();

        String nbtPage = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_minePag");
        String minePlayer = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_mineJogador");

        if(nbtPage != null){
            p.openInventory(MinePlayersGui.invs.get(p).get(Integer.parseInt(nbtPage)));
            return;
        }

        FileConfiguration getConfig = getMinePlayersGuiYml().getConfig();

        if(e.getSlot() == getConfig.getInt("items.back.slot")){

            getMineManager().getPlayersGuiOpen().remove(p.getName());

            if(getMineManager().getMainInventories().containsKey(p.getName())) p.openInventory(getMineManager().getMainInventories().get(p.getName()));
            else MineMainGui.create(p);

        }

        if(minePlayer == null) return;

        MineBet mineBet = getMineManager().getMineBet(minePlayer);

        mineBet.getSpectators().add(p.getName());

        p.openInventory(mineBet.getGameInventory());

        mineBet.getGameInventory().setItem(getMineManager().getLeftSlot(), getMineManager().getLeftItem(mineBet));

        for (String s : getMineManager().getPlayersGuiOpen()) {
            Player player = Bukkit.getPlayer(s);

            if(player != null) MinePlayersGui.create(player);
        }
    }

}
