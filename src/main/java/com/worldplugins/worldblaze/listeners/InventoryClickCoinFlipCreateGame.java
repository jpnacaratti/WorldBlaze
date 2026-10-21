package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.CoinFlipCreateGameGui;
import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.objects.CoinFlipBet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class InventoryClickCoinFlipCreateGame implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!e.getInventory().getName().equalsIgnoreCase(c(getCoinFlipCreateGameGuiYml().getConfig().getString("Titulo")))) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getCoinFlipCreateGameGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        BlazeUser user = getManager().getBlazeUser(p.getName());

        if(e.getSlot() == getConfig.getInt("Itens.money.Slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("Messages.CoinFlip-typing-money").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.coinFlipMoneyList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.cash.Slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("Messages.CoinFlip-typing-cash").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.coinFlipCashList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.definiradversario.Slot")){
            if(e.isRightClick()){
                if(user.getCoinFlipSpecific() == null) return;

                user.setCoinFlipSpecific(null);
                CoinFlipCreateGameGui.create(p);
                return;
            }

            p.closeInventory();

            getInstance().getConfig().getStringList("Messages.CoinFlip-typing-opponent").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.coinFlipOpponentList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.escolha.Slot")){

            user.setCoinFlipCara(!user.isCoinFlipCara());

            CoinFlipCreateGameGui.create(p);
        }

        if(e.getSlot() == getConfig.getInt("Itens.iniciar.Slot")){

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if(getInstance().isLocked()){
                p.sendMessage(getMessages().GAMES_UNAVAILABLE);
                return;
            }

            if(getCoinFlipManager().isPlayerParticipating(p.getName())){
                p.closeInventory();
                p.sendMessage("§cVocê já está em uma partida, contate um staff!");
                return;
            }

            if(user.getCoinFlipMoneyValue() < getInstance().getConfig().getDouble("Config.Coinflip.Min-money-aposta")){
                p.sendMessage(getMessages().MINIMUM_MONEY);
                return;
            }

            if(user.getCoinFlipCashValue() < getInstance().getConfig().getDouble("Config.Coinflip.Min-cash-aposta")){
                p.sendMessage(getMessages().MINIMUM_CASH);
                return;
            }

            if(user.getCoinFlipMoneyValue() > getInstance().getConfig().getDouble("Config.Coinflip.Max-money-aposta")){
                p.sendMessage(getMessages().MAXIMUM_MONEY);
                return;
            }

            if(user.getCoinFlipCashValue() > getInstance().getConfig().getDouble("Config.Coinflip.Max-cash-aposta")){
                p.sendMessage(getMessages().MAXIMUM_CASH);
                return;
            }

            if(!checkValues(p, user.getCoinFlipCashValue(), user.getCoinFlipMoneyValue())) return;

            CoinFlipBet bet = new CoinFlipBet(p.getName(), getNewBetId() + "", user.getCoinFlipCashValue(), user.getCoinFlipMoneyValue(), user.getCoinFlipSpecific()
            , user.isCoinFlipCara());

            getCoinFlipManager().getCoinFlipBets().add(bet);

            removeValues(p, user.getCoinFlipCashValue(), user.getCoinFlipMoneyValue());

            if(bet.getSpecific() != null) {

                getInstance().getConfig().getStringList("Messages.Challenged-player-coinflip").forEach(w -> p.sendMessage(c(w)));

                Player player = Bukkit.getPlayer(bet.getSpecific());

                if(player != null){
                    getInstance().getConfig().getStringList("Messages.Player-challenged-coinflip").forEach(w -> player.sendMessage(c(w.
                            replace("{player}",p.getName()).
                            replace("{money}",letterFormatter(user.getCoinFlipMoneyValue())).
                            replace("{cash}", letterFormatter(user.getCoinFlipCashValue())))));
                }

            }else getInstance().getConfig().getStringList("Messages.Challenged-coinflip").forEach(w -> p.sendMessage(c(w)));

            p.closeInventory();

            getCoinFlipManager().updatePrincipalInventories();
            getCoinFlipManager().updateGamblersInventories();

        }

    }
}
