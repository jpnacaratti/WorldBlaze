package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.CoinFlipCreateGameGui;
import com.worldplugins.worldblaze.guis.CoinFlipGamblersGui;
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

        if (!e.getInventory().getName().equalsIgnoreCase(getCoinFlipManager().getPrincipalMenuTitle())) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getCoinFlipPrincipalGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == getConfig.getInt("Itens.criarjogo.Slot")){
            CoinFlipBet coinFlipBet = getCoinFlipManager().getCoinFlipBet(p.getName());
            if(coinFlipBet != null){
                if(coinFlipBet.getOpponent() != null){
                    p.sendMessage(getMessages().JA_EM_X1);
                    return;
                }

                p.closeInventory();

                getCoinFlipManager().getCoinFlipBets().remove(coinFlipBet);

                getCoinFlipManager().updatePrincipalInventories();
                getCoinFlipManager().updateGamblersInventories();

                depositValues(p, coinFlipBet.getCashValue(), coinFlipBet.getMoneyValue());

                p.sendMessage(getMessages().CANCELOU_APOSTA);
                return;
            }

            CoinFlipCreateGameGui.create(p);
        }

        if (e.getSlot() == getConfig.getInt("Itens.jogos.Slot")) CoinFlipGamblersGui.create(p);

    }
}