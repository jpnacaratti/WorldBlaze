package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.CrashGamblersGui;
import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.objects.CrashBet;
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

public class InventoryClickCrash implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if(!e.getInventory().getName().equalsIgnoreCase(getCrashManager().getMenuTitle())) return;
        e.setCancelled(true);

        if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getCrashGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        BlazeUser user = getManager().getBlazeUser(p.getName());

        if(e.getSlot() == getConfig.getInt("items.money.slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("messages.crash-typing-money").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.crashMoneyList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("items.cash.slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("messages.crash-typing-cash").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.crashCashList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("items.bettors.slot")){
            CrashGamblersGui.create(p);
        }

        if(e.getSlot() == getConfig.getInt("items.crash-starting.slot") ||
                e.getSlot() == getConfig.getInt("items.crash-started.slot") ||
                e.getSlot() == getConfig.getInt("items.crash-crashed.slot")){

            if(getCrashManager().getCrashBets().containsKey(p.getName())){

                if(getCrashManager().isStarting()){
                    p.sendMessage(getMessages().ALREADY_IN_CRASH);
                    return;
                }

                if(getCrashManager().isCrashed()){
                    p.sendMessage(getMessages().CRASH_RECENTLY_ENDED);
                    return;
                }

                CrashBet crashBet = getCrashManager().getCrashBets().get(p.getName());

                if(crashBet.isLeft()){
                    p.sendMessage(getMessages().ALREADY_EXITED);
                    return;
                }

                crashBet.setLeft(true);
                crashBet.setLeftValue(getCrashManager().getCrashValue());

                double money = crashBet.getMoneyValue() * getCrashManager().getCrashValue();
                double cash = crashBet.getCashValue() * getCrashManager().getCrashValue();

                depositValues(p, cash, money);

                for (String s : getCrashManager().getGamblersGuiOpen()) {
                    Player player = Bukkit.getPlayer(s);

                    if(player != null) CrashGamblersGui.create(player);
                }

                if(getCrashManager().getCrashValue() >= getInstance().getConfig().getDouble("config.crash.alert")){
                    getInstance().getConfig().getStringList("messages.broadcast-cashed-out-crash").forEach(w -> Bukkit.broadcastMessage(c(w.
                            replace("{player}", crashBet.getOwner()).
                            replace("{crash}", String.format("%.2f", crashBet.getLeftValue())).
                            replace("{money}", letterFormatter(money)).
                            replace("{cash}", letterFormatter(cash)))));
                }

                getInstance().getConfig().getStringList("messages.won-crash").forEach(w -> p.sendMessage(c(w.
                        replace("{money}", letterFormatter(money)).
                        replace("{cash}", letterFormatter(cash)))));
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

            if(!getCrashManager().isStarting()){
                p.sendMessage(getMessages().CRASH_STARTED);
                return;
            }

            if(getCrashManager().isCrashed()){
                p.sendMessage(getMessages().CRASH_ENDED);
                return;
            }

            if(user.getCrashMoneyValue() < getInstance().getConfig().getDouble("config.crash.min-money-bet")){
                p.sendMessage(getMessages().MINIMUM_MONEY);
                return;
            }

            if(user.getCrashCashValue() < getInstance().getConfig().getDouble("config.crash.min-cash-bet")){
                p.sendMessage(getMessages().MINIMUM_CASH);
                return;
            }

            if(user.getCrashMoneyValue() > getInstance().getConfig().getDouble("config.crash.max-money-bet")){
                p.sendMessage(getMessages().MAXIMUM_MONEY);
                return;
            }

            if(user.getCrashCashValue() > getInstance().getConfig().getDouble("config.crash.max-cash-bet")){
                p.sendMessage(getMessages().MAXIMUM_CASH);
                return;
            }

            if(!checkValues(p, user.getCrashCashValue(), user.getCrashMoneyValue())) return;

            CrashBet crashBet = new CrashBet(p.getName(), getNewBetId() + "", user.getCrashCashValue(), user.getCrashMoneyValue());

            getCrashManager().getCrashBets().put(p.getName(), crashBet);

            getCrashManager().setTotalBetMoney();
            getCrashManager().setTotalBetCash();

            getCrashManager().setLastPlayer(p.getName());

            for (Map.Entry<String, Inventory> entry : getCrashManager().getCrashInventories().entrySet()) {
                entry.getValue().setItem(getCrashManager().getGamblersSlot(), getCrashManager().getGamblersItem());
            }

            for (String s : getCrashManager().getGamblersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) CrashGamblersGui.create(player);
            }

            removeValues(p, user.getCrashCashValue(), user.getCrashMoneyValue());

            getInstance().getConfig().getStringList("messages.joined-crash").forEach(w -> p.sendMessage(c(w)));
        }

    }

}
