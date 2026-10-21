package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.GraphGamblersGui;
import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.objects.GraphBet;
import com.worldplugins.worldblaze.schedulers.MainTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class  InventoryClickGraph implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if(!e.getInventory().getName().equalsIgnoreCase(getGraphManager().getMenuTitle())) return;
        e.setCancelled(true);

        if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getGraphGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        BlazeUser user = getManager().getBlazeUser(p.getName());

        if(e.getSlot() == getConfig.getInt("Itens.tempo.Slot")){

            if(e.isRightClick()){
                int nValue = user.getGraphTime() - 1;

                if(getInstance().getConfig().getInt("config.graph.min-time-update") > nValue){
                    p.sendMessage(getMessages().CANNOT_DECREASE_UPDATE);
                    return;
                }

                user.setGraphTime(nValue);
            }else{
                int nValue = user.getGraphTime() + 1;

                if(getInstance().getConfig().getInt("config.graph.max-time-update") < nValue){
                    p.sendMessage(getMessages().CANNOT_INCREASE_UPDATE);
                    return;
                }

                user.setGraphTime(nValue);
            }

            Inventory inv = getGraphManager().getGraphInventories().get(p.getName());
            inv.setItem(getGraphManager().getTimeSlot(), getGraphManager().getTimeItem(user));
            inv.setItem(getGraphManager().getBetUpSlot(), getGraphManager().getBetUpItem(user));
            inv.setItem(getGraphManager().getBetDecreaseSlot(), getGraphManager().getBetDecreaseItem(user));
            p.updateInventory();
        }

        if(e.getSlot() == getConfig.getInt("Itens.money.Slot")){
            InventoryClose.bypassClose.add(p.getName());
            p.closeInventory();
            InventoryClose.bypassClose.remove(p.getName());

            getInstance().getConfig().getStringList("messages.graph-typing-money").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.graphMoneyList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.cash.Slot")){
            InventoryClose.bypassClose.add(p.getName());
            p.closeInventory();
            InventoryClose.bypassClose.remove(p.getName());

            getInstance().getConfig().getStringList("messages.graph-typing-cash").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.graphCashList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.apostadores.Slot")){

            InventoryClose.bypassClose.add(p.getName());
            GraphGamblersGui.create(p);
            InventoryClose.bypassClose.remove(p.getName());

            getGraphManager().getGamblersGuiOpen().add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.apostar-aumentar.Slot") || e.getSlot() == getConfig.getInt("Itens.apostar-diminuir.Slot")){

            if(MainTask.graphDelays.containsKey(p.getName())){
                p.sendMessage(c(getMessages().WAIT_BEFORE_BETTING.replace("{tempo}", "" + (getGraphManager().getDelay() - MainTask.graphDelays.get(p.getName())))));
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if(getInstance().isLocked()){
                p.sendMessage(getMessages().GAMES_UNAVAILABLE);
                return;
            }

            if(getGraphManager().getGraphPoints().size() == 0){
                p.sendMessage(getMessages().GRAPH_STARTING);
                return;
            }

            if(user.getGraphMoneyValue() < getInstance().getConfig().getDouble("config.graph.min-money-bet")){
                p.sendMessage(getMessages().MINIMUM_MONEY);
                return;
            }

            if(user.getGraphCashValue() < getInstance().getConfig().getDouble("config.graph.min-cash-bet")){
                p.sendMessage(getMessages().MINIMUM_CASH);
                return;
            }

            if(user.getGraphMoneyValue() > getInstance().getConfig().getDouble("config.graph.max-money-bet")){
                p.sendMessage(getMessages().MAXIMUM_MONEY);
                return;
            }

            if(user.getGraphCashValue() > getInstance().getConfig().getDouble("config.graph.max-cash-bet")){
                p.sendMessage(getMessages().MAXIMUM_CASH);
                return;
            }

            if(!checkValues(p, user.getGraphCashValue(), user.getGraphMoneyValue())) return;

            GraphBet graphBet = new GraphBet(p.getName(), user.getGraphCashValue(), user.getGraphMoneyValue(), getGraphManager().getLastPointValue(),
                    getGraphManager().getPointsNum() - 1, user.getGraphTime(),
                    e.getSlot() == getConfig.getInt("Itens.apostar-aumentar.Slot"), getNewBetId());

            getGraphManager().getGraphBets().add(graphBet);

            getGraphManager().updateTotalBetMoney();
            getGraphManager().updateTotalBetCash();

            getGraphManager().updateNegativeBetAverage();
            getGraphManager().updatePositiveBetAverage();

            for (Map.Entry<String, Inventory> entry : getGraphManager().getGraphInventories().entrySet()) {
                entry.getValue().setItem(getGraphManager().getGamblersSlot(), getGraphManager().getGamblersItem());
                entry.getValue().setItem(getGraphManager().getBetUpSlot(), getGraphManager().getBetUpItem(user));
                entry.getValue().setItem(getGraphManager().getBetDecreaseSlot(), getGraphManager().getBetDecreaseItem(user));
            }

            for (String s : getGraphManager().getGamblersGuiOpen()) {
                Player target = Bukkit.getPlayer(s);

                if(target == null) GraphGamblersGui.create(p);
            }

            removeValues(p, user.getGraphCashValue(), user.getGraphMoneyValue());

            p.sendMessage(c(getMessages().GRAPH_BET_PLACED.replace("{point}",(getGraphManager().getPointsNum() - 1) + "")));

            MainTask.graphDelays.put(p.getName(), 0);
        }

    }

}
