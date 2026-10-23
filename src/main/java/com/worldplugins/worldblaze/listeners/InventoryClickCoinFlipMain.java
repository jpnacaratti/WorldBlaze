package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.CoinFlipCreateGameGui;
import com.worldplugins.worldblaze.guis.CoinFlipGamblersGui;
import com.worldplugins.worldblaze.guis.MenuHolder;
import com.worldplugins.worldblaze.guis.MenuType;
import com.worldplugins.worldblaze.objects.CoinFlipBet;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.depositValues;

public class InventoryClickCoinFlipMain implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!MenuHolder.is(e.getInventory(), MenuType.COINFLIP_MAIN)) return;
        e.setCancelled(true);
        if (e.getRawSlot() >= e.getInventory().getSize()) return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getCoinFlipMainGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == getConfig.getInt("items.create-game.slot")){
            CoinFlipBet coinFlipBet = getCoinFlipManager().getCoinFlipBet(p.getName());
            if(coinFlipBet != null){
                if(coinFlipBet.getOpponent() != null){
                    p.sendMessage(getMessages().ALREADY_IN_GAME);
                    return;
                }

                p.closeInventory();

                getCoinFlipManager().getCoinFlipBets().remove(coinFlipBet);

                getCoinFlipManager().updateMainInventories();
                getCoinFlipManager().updateGamblersInventories();

                depositValues(p, coinFlipBet.getCashValue(), coinFlipBet.getMoneyValue());

                p.sendMessage(getMessages().BET_CANCELLED);
                return;
            }

            CoinFlipCreateGameGui.create(p);
        }

        if (e.getSlot() == getConfig.getInt("items.games.slot")) CoinFlipGamblersGui.create(p);

    }
}
