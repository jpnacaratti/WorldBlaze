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

        if(e.getSlot() == getConfig.getInt("Itens.money.Slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("Mensagens.Crash-digitando-money").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.crashMoneyList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.cash.Slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("Mensagens.Crash-digitando-cash").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.crashCashList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.apostadores.Slot")){
            CrashGamblersGui.create(p);
        }

        if(e.getSlot() == getConfig.getInt("Itens.crash-iniciando.Slot") ||
                e.getSlot() == getConfig.getInt("Itens.crash-iniciado.Slot") ||
                e.getSlot() == getConfig.getInt("Itens.crash-crashou.Slot")){

            if(getCrashManager().getCrashBets().containsKey(p.getName())){

                if(getCrashManager().isStarting()){
                    p.sendMessage(getMessages().CRASH_JA_PARTICIPANDO);
                    return;
                }

                if(getCrashManager().isCrashed()){
                    p.sendMessage(getMessages().CRASH_JA_ACABADO);
                    return;
                }

                CrashBet crashBet = getCrashManager().getCrashBets().get(p.getName());

                if(crashBet.isLeft()){
                    p.sendMessage(getMessages().JA_SAIU);
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

                if(getCrashManager().getCrashValue() >= getInstance().getConfig().getDouble("Config.Crash.Alerta")){
                    getInstance().getConfig().getStringList("Mensagens.Broadcast-saiu-crash").forEach(w -> Bukkit.broadcastMessage(c(w.
                            replace("{player}", crashBet.getOwner()).
                            replace("{crash}", String.format("%.2f", crashBet.getLeftValue())).
                            replace("{money}", letterFormatter(money)).
                            replace("{cash}", letterFormatter(cash)))));
                }

                getInstance().getConfig().getStringList("Mensagens.Ganhou-crash").forEach(w -> p.sendMessage(c(w.
                        replace("{money}", letterFormatter(money)).
                        replace("{cash}", letterFormatter(cash)))));
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if(getInstance().isLocked()){
                p.sendMessage(getMessages().JOGOS_LOCKED);
                return;
            }

            if(!getCrashManager().isStarting()){
                p.sendMessage(getMessages().CRASH_INICIADO);
                return;
            }

            if(getCrashManager().isCrashed()){
                p.sendMessage(getMessages().CRASH_ACABADO);
                return;
            }

            if(user.getCrashMoneyValue() < getInstance().getConfig().getDouble("Config.Crash.Min-money-aposta")){
                p.sendMessage(getMessages().MINIMO_MONEY);
                return;
            }

            if(user.getCrashCashValue() < getInstance().getConfig().getDouble("Config.Crash.Min-cash-aposta")){
                p.sendMessage(getMessages().MINIMO_CASH);
                return;
            }

            if(user.getCrashMoneyValue() > getInstance().getConfig().getDouble("Config.Crash.Max-money-aposta")){
                p.sendMessage(getMessages().MAXIMO_MONEY);
                return;
            }

            if(user.getCrashCashValue() > getInstance().getConfig().getDouble("Config.Crash.Max-cash-aposta")){
                p.sendMessage(getMessages().MAXIMO_CASH);
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

            getInstance().getConfig().getStringList("Mensagens.Participou-crash").forEach(w -> p.sendMessage(c(w)));
        }

    }

}
