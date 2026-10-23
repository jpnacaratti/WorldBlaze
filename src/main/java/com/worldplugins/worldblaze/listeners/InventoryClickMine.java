package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.MenuHolder;
import com.worldplugins.worldblaze.guis.MenuType;
import com.worldplugins.worldblaze.guis.MinePlayersGui;
import com.worldplugins.worldblaze.objects.MineBet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Random;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class InventoryClickMine implements Listener {

    private final Random rand = new Random();

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!MenuHolder.is(e.getInventory(), MenuType.MINE_GAME)) return;
        e.setCancelled(true);
        if (e.getRawSlot() >= e.getInventory().getSize()) return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        Player p = (Player) e.getWhoClicked();

        MineBet mineBet = getMineManager().getMineBet(p.getName());

        if(mineBet == null) return;

        if(!p.getName().equalsIgnoreCase(mineBet.getOwner())) return;

        if(e.getSlot() == getMineManager().getLeftSlot()){

            if(mineBet.getClickedSlots().size() < getMineManager().getMinSlots()){
                p.sendMessage(getMessages().MINIMUM_SLOTS);
                return;
            }

            double wonMoney = mineBet.getMoneyValue() * mineBet.getMultiplier();
            double wonCash = mineBet.getCashValue() * mineBet.getMultiplier();

            if((wonMoney > 0 && wonMoney >= getMineManager().getAlertMoney()) || (wonCash > 0 && wonCash >= getMineManager().getAlertCash())){
                getInstance().getConfig().getStringList("messages.broadcast-won-mine").forEach(w -> Bukkit.broadcastMessage(c(w.
                        replace("{player}", p.getName()).
                        replace("{multiplier}",String.format("%.2f",mineBet.getMultiplier())).
                        replace("{money}",letterFormatter(wonMoney)).
                        replace("{cash}",letterFormatter(wonCash)))));
            }

            getInstance().getConfig().getStringList("messages.won-mine").forEach(w -> p.sendMessage(c(w.
                    replace("{multiplier}",String.format("%.2f",mineBet.getMultiplier())).
                    replace("{money}",letterFormatter(wonMoney)).
                    replace("{cash}",letterFormatter(wonCash)))));

            depositValues(p, wonCash, wonMoney);

            getMineManager().getMineBets().remove(mineBet);

            getMineManager().setTotalBetMoney();
            getMineManager().setTotalBetCash();

            for (String spectator : mineBet.getSpectators()) {
                Player player = Bukkit.getPlayer(spectator);

                if(player != null) getInstance().getConfig().getStringList("messages.spectators-game-ended").forEach(w -> player.sendMessage(c(w)));

            }

            for (String s : getMineManager().getPlayersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) MinePlayersGui.create(player);
            }

            for (Map.Entry<String, Inventory> entry : getMineManager().getMainInventories().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if(player != null){
                    entry.getValue().setItem(getMineManager().getMainPlayersSlot(), getMineManager().getMainPlayersItem());
                    player.updateInventory();
                }
            }

            p.closeInventory();
            return;
        }

        if(mineBet.getClickedSlots().contains(e.getSlot())) return;

        mineBet.getClickedSlots().add(e.getSlot());

        if(mineBet.getBombSlots().contains(e.getSlot())){

            mineBet.getBombSlots().add(getMineManager().getRandomAvailableSlot(mineBet));

            getMineManager().minedBomb(p, mineBet, e.getInventory());

        }else{

            if(rand.nextDouble() * 100 <= ((1.0 / (double) (getMineManager().getAvailableSlots().size() - mineBet.getClickedSlots().size())) * 100.0) * mineBet.getChanceMultiplier()){
                mineBet.getBombSlots().add(e.getSlot());
                getMineManager().minedBomb(p, mineBet, e.getInventory());
                return;
            }

            e.getInventory().setItem(e.getSlot(), getMineManager().getMinedItem());

            mineBet.setMultiplier(mineBet.getMultiplier() + mineBet.getMultiplierSum());

            e.getInventory().setItem(getMineManager().getLeftSlot(), getMineManager().getLeftItem(mineBet));

            for (String s : getMineManager().getPlayersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) MinePlayersGui.create(player);
            }
        }

    }
}
