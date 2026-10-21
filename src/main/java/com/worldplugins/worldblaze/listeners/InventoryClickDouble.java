package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.enums.DoubleColor;
import com.worldplugins.worldblaze.guis.DoubleGamblersGui;
import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.objects.DoubleBet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class InventoryClickDouble implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if(!e.getInventory().getName().equalsIgnoreCase(getDoubleManager().getMenuTitle())) return;
        e.setCancelled(true);

        if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getDoubleGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        BlazeUser user = getManager().getBlazeUser(p.getName());

        if(e.getSlot() == getConfig.getInt("Itens.money.Slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("Messages.Double-typing-money").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.doubleMoneyList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.cash.Slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("Messages.Double-typing-cash").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.doubleCashList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.apostadores.Slot")) DoubleGamblersGui.create(p);

        if(e.getSlot() == getConfig.getInt("Itens.apostar-vermelho.Slot") ||
                e.getSlot() == getConfig.getInt("Itens.apostar-branco.Slot") ||
                e.getSlot() == getConfig.getInt("Itens.apostar-preto.Slot")){

            DoubleColor chose;
            if(e.getSlot() == getConfig.getInt("Itens.apostar-vermelho.Slot")) chose = DoubleColor.RED;
            else if(e.getSlot() == getConfig.getInt("Itens.apostar-branco.Slot")) chose = DoubleColor.WHITE;
            else chose = DoubleColor.BLACK;

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if(getInstance().isLocked()){
                p.sendMessage(getMessages().GAMES_UNAVAILABLE);
                return;
            }

            if(!getDoubleManager().isStarting()){
                p.sendMessage(getMessages().ROUND_ENDING);
                return;
            }

            if(getDoubleManager().isStopped()){
                p.sendMessage(getMessages().ROUND_ENDING);
                return;
            }

            if(getDoubleManager().isPlayerParticipating(p.getName(), chose)){
                p.sendMessage(getMessages().ALREADY_BET_ON_COLOR);
                return;
            }

            if(user.getDoubleMoneyValue() < getInstance().getConfig().getDouble("Config.Double.Min-money-aposta")){
                p.sendMessage(getMessages().MINIMUM_MONEY);
                return;
            }

            if(user.getDoubleCashValue() < getInstance().getConfig().getDouble("Config.Double.Min-cash-aposta")){
                p.sendMessage(getMessages().MINIMUM_CASH);
                return;
            }

            if(user.getDoubleMoneyValue() > getInstance().getConfig().getDouble("Config.Double.Max-money-aposta")){
                p.sendMessage(getMessages().MAXIMUM_MONEY);
                return;
            }

            if(user.getDoubleCashValue() > getInstance().getConfig().getDouble("Config.Double.Max-cash-aposta")){
                p.sendMessage(getMessages().MAXIMUM_CASH);
                return;
            }

            if(!checkValues(p, user.getDoubleCashValue(), user.getDoubleMoneyValue())) return;

            DoubleBet doubleBet = new DoubleBet(p.getName(), getNewBetId() + "", chose, user.getDoubleCashValue(), user.getDoubleMoneyValue());

            getDoubleManager().getDoubleBets().add(doubleBet);

            getDoubleManager().setTotalBetMoney();
            getDoubleManager().setTotalBetCash();

            getDoubleManager().setRedAverage();
            getDoubleManager().setWhiteAverage();
            getDoubleManager().setBlackAverage();

            getDoubleManager().updateInventories(true, false, true);

            for (String s : getDoubleManager().getGamblersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) DoubleGamblersGui.create(player);
            }

            removeValues(p, user.getDoubleCashValue(), user.getDoubleMoneyValue());

            getInstance().getConfig().getStringList("Messages.Bet-placed-double").forEach(w -> p.sendMessage(c(w.replace("{cor}", chose.getNameStr()))));
        }

    }

}
