package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.MenuHolder;
import com.worldplugins.worldblaze.guis.MenuType;
import com.worldplugins.worldblaze.objects.CoinFlipBet;
import com.worldplugins.worldblaze.objects.MineBet;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashSet;
import java.util.Set;

import static com.worldplugins.worldblaze.WorldBlaze.*;

public class InventoryClose implements Listener {

    public static Set<String> bypassClose = new HashSet<>();

    @EventHandler
    public void onClose(InventoryCloseEvent e){

        getGraphManager().getGamblersGuiOpen().remove(e.getPlayer().getName());
        getCrashManager().getGamblersGuiOpen().remove(e.getPlayer().getName());
        getDoubleManager().getGamblersGuiOpen().remove(e.getPlayer().getName());
        getMineManager().getPlayersGuiOpen().remove(e.getPlayer().getName());
        getCoinFlipManager().getCoinFlipGamblersGuiOpen().remove(e.getPlayer().getName());

        if(bypassClose.contains(e.getPlayer().getName())) return;

        getAnimalGameManager().getSeeingMap().remove(e.getPlayer().getName());

        if(MenuHolder.is(e.getInventory(), MenuType.GRAPH)){

            getGraphManager().getGraphInventories().remove(e.getPlayer().getName());

        }

        if(MenuHolder.is(e.getInventory(), MenuType.CRASH)){

            getCrashManager().getCrashInventories().remove(e.getPlayer().getName());

        }

        if(MenuHolder.is(e.getInventory(), MenuType.DOUBLE)){

            getDoubleManager().getDoubleInventories().remove(e.getPlayer().getName());

        }

        if(MenuHolder.is(e.getInventory(), MenuType.MINE_MAIN)){

            getMineManager().getMainInventories().remove(e.getPlayer().getName());

        }

        if(MenuHolder.is(e.getInventory(), MenuType.MINE_GAME)){
            MineBet mineBet = getMineManager().getWhoPlayerIsSeen(e.getPlayer().getName());

            if(mineBet != null){
                mineBet.getSpectators().remove(e.getPlayer().getName());

                mineBet.getGameInventory().setItem(getMineManager().getLeftSlot(), getMineManager().getLeftItem(mineBet));

                return;
            }

            MineBet mineBetOwner = getMineManager().getMineBet(e.getPlayer().getName());

            if(mineBetOwner != null){
                Bukkit.getScheduler().runTaskLater(getInstance(), ()-> e.getPlayer().openInventory(e.getInventory()), 2);
                e.getPlayer().sendMessage(getMessages().CANNOT_CLOSE_GAME_MENU);
            }
        }

        if(MenuHolder.is(e.getInventory(), MenuType.COINFLIP_MAIN)){

            getCoinFlipManager().getMainInventories().remove(e.getPlayer().getName());

        }

        if(MenuHolder.is(e.getInventory(), MenuType.COINFLIP_GAME)) {
            CoinFlipBet bet = getCoinFlipManager().getCoinFlipBetClose(e.getPlayer().getName());

            if (bet != null) {
                if(bet.getOpponent() != null && bet.getOwner() != null){
                    Bukkit.getScheduler().runTaskLater(getInstance(), ()->e.getPlayer().openInventory(e.getInventory()), 2);
                    e.getPlayer().sendMessage(getMessages().CANNOT_CLOSE_GAME_MENU);
                }
            }

        }
    }

}
