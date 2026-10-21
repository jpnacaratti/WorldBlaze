package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.enums.Bicho;
import com.worldplugins.worldblaze.guis.*;
import com.worldplugins.worldblaze.objects.BlazeUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class AsyncPlayerChat implements Listener {

    public static Set<String> graphMoneyList = new HashSet<>();
    public static Set<String> graphCashList = new HashSet<>();

    public static Set<String> crashMoneyList = new HashSet<>();
    public static Set<String> crashCashList = new HashSet<>();

    public static Set<String> doubleMoneyList = new HashSet<>();
    public static Set<String> doubleCashList = new HashSet<>();

    public static Set<String> mineMoneyList = new HashSet<>();
    public static Set<String> mineCashList = new HashSet<>();

    public static Map<String, Bicho> jogoDoBichoMoneyList = new HashMap<>();
    public static Map<String, Bicho> jogoDoBichoCashList = new HashMap<>();

    public static Set<String> coinFlipMoneyList = new HashSet<>();
    public static Set<String> coinFlipCashList = new HashSet<>();
    public static Set<String> coinFlipOpponentList = new HashSet<>();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){

        String msg = e.getMessage();
        Player p = e.getPlayer();

        boolean nan = msg.equalsIgnoreCase("nan") || msg.equalsIgnoreCase("+nan") || msg.equalsIgnoreCase("-nan");

        if(graphMoneyList.contains(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                graphMoneyList.remove(p.getName());
                getGraphManager().getGraphInventories().remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                graphMoneyList.remove(p.getName());
                getGraphManager().getGraphInventories().remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                graphMoneyList.remove(p.getName());
                getGraphManager().getGraphInventories().remove(p.getName());
                return;
            }

            if(!checkValues(p, 0, val)){
                graphMoneyList.remove(p.getName());
                return;
            }

            user.setGraphMoneyValue(val);

            if(getGraphManager().getGraphInventories().containsKey(p.getName())){
                Inventory inventory = getGraphManager().getGraphInventories().get(p.getName());

                inventory.setItem(getGraphManager().getMoneySlot(), getGraphManager().getMoneyItem(user));
                inventory.setItem(getGraphManager().getBetUpSlot(), getGraphManager().getBetUpItem(user));
                inventory.setItem(getGraphManager().getBetDecreaseSlot(), getGraphManager().getBetDecreaseItem(user));

                p.openInventory(inventory);
            }else GraphGui.create(p);

            graphMoneyList.remove(p.getName());
        }

        if(graphCashList.contains(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                graphCashList.remove(p.getName());
                getGraphManager().getGraphInventories().remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                graphCashList.remove(p.getName());
                getGraphManager().getGraphInventories().remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex) {
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                graphCashList.remove(p.getName());
                getGraphManager().getGraphInventories().remove(p.getName());
                return;
            }

            if(!checkValues(p, val, 0)){
                graphCashList.remove(p.getName());
                return;
            }

            user.setGraphCashValue(val);

            if(getGraphManager().getGraphInventories().containsKey(p.getName())){
                Inventory inventory = getGraphManager().getGraphInventories().get(p.getName());

                inventory.setItem(getGraphManager().getCashSlot(), getGraphManager().getCashItem(user));
                inventory.setItem(getGraphManager().getBetUpSlot(), getGraphManager().getBetUpItem(user));
                inventory.setItem(getGraphManager().getBetDecreaseSlot(), getGraphManager().getBetDecreaseItem(user));

                p.openInventory(inventory);
            }else GraphGui.create(p);

            graphCashList.remove(p.getName());
        }

        if(crashMoneyList.contains(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                crashMoneyList.remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                crashMoneyList.remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                crashMoneyList.remove(p.getName());
                return;
            }

            if(!checkValues(p, 0, val)){
                crashMoneyList.remove(p.getName());
                return;
            }

            user.setCrashMoneyValue(val);

            CrashGui.create(p);

            crashMoneyList.remove(p.getName());
        }

        if(crashCashList.contains(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                crashCashList.remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                crashCashList.remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex) {
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                crashCashList.remove(p.getName());
                return;
            }

            if(!checkValues(p, val, 0)){
                crashCashList.remove(p.getName());
                return;
            }

            user.setCrashCashValue(val);

            CrashGui.create(p);

            crashCashList.remove(p.getName());
        }

        if(doubleMoneyList.contains(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                doubleMoneyList.remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                doubleMoneyList.remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                doubleMoneyList.remove(p.getName());
                return;
            }

            if(!checkValues(p, 0, val)){
                doubleMoneyList.remove(p.getName());
                return;
            }

            user.setDoubleMoneyValue(val);

            DoubleGui.create(p);

            doubleMoneyList.remove(p.getName());
        }

        if(doubleCashList.contains(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                doubleCashList.remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                doubleCashList.remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                doubleCashList.remove(p.getName());
                return;
            }

            if(!checkValues(p, val, 0)){
                doubleCashList.remove(p.getName());
                return;
            }

            user.setDoubleCashValue(val);

            DoubleGui.create(p);

            doubleCashList.remove(p.getName());
        }

        if(mineMoneyList.contains(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                mineMoneyList.remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                mineMoneyList.remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                mineMoneyList.remove(p.getName());
                return;
            }

            if(!checkValues(p, 0, val)){
                mineMoneyList.remove(p.getName());
                return;
            }

            user.setMineMoneyValue(val);

            MineCreateGui.create(p);

            mineMoneyList.remove(p.getName());
        }

        if(mineCashList.contains(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                mineCashList.remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                mineCashList.remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                mineCashList.remove(p.getName());
                return;
            }

            if(!checkValues(p, val, 0)){
                mineCashList.remove(p.getName());
                return;
            }

            user.setMineCashValue(val);

            MineCreateGui.create(p);

            mineCashList.remove(p.getName());
        }

        if(jogoDoBichoMoneyList.containsKey(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                jogoDoBichoMoneyList.remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                jogoDoBichoMoneyList.remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                jogoDoBichoMoneyList.remove(p.getName());
                return;
            }

            if(!checkValues(p, 0, val)){
                jogoDoBichoMoneyList.remove(p.getName());
                return;
            }

            user.setJogoDoBichoMoneyValue(val);

            JogoDoBichoBetGui.create(p,jogoDoBichoMoneyList.get(p.getName()));

            jogoDoBichoMoneyList.remove(p.getName());
        }

        if(jogoDoBichoCashList.containsKey(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                jogoDoBichoCashList.remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                jogoDoBichoCashList.remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                jogoDoBichoCashList.remove(p.getName());
                return;
            }

            if(!checkValues(p, val, 0)){
                jogoDoBichoCashList.remove(p.getName());
                return;
            }

            user.setJogoDoBichoCashValue(val);

            JogoDoBichoBetGui.create(p,jogoDoBichoCashList.get(p.getName()));

            jogoDoBichoCashList.remove(p.getName());
        }

        if(coinFlipMoneyList.contains(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                coinFlipMoneyList.remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                coinFlipMoneyList.remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                coinFlipMoneyList.remove(p.getName());
                return;
            }

            if(!checkValues(p, 0, val)){
                coinFlipMoneyList.remove(p.getName());
                return;
            }

            user.setCoinFlipMoneyValue(val);

            CoinFlipCreateGameGui.create(p);

            coinFlipMoneyList.remove(p.getName());
        }

        if(coinFlipCashList.contains(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                coinFlipCashList.remove(p.getName());
                return;
            }

            if(nan){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                coinFlipCashList.remove(p.getName());
                Bukkit.getScheduler().runTask(getInstance(), ()->p.kickPlayer("Duplication attempt"));
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            double val;
            try {
                val = passLetterToNumber(msg);
            }catch (NumberFormatException ex){
                p.sendMessage(getMessages().INVALID_CHAT_NUMBER);
                coinFlipCashList.remove(p.getName());
                return;
            }

            if(!checkValues(p, val, 0)){
                coinFlipCashList.remove(p.getName());
                return;
            }

            user.setCoinFlipCashValue(val);

            CoinFlipCreateGameGui.create(p);

            coinFlipCashList.remove(p.getName());
        }

        if(coinFlipOpponentList.contains(p.getName())){
            e.setCancelled(true);

            if(msg.equalsIgnoreCase("cancel")){
                p.sendMessage(getMessages().CHAT_CANCELLED);
                coinFlipOpponentList.remove(p.getName());
                return;
            }

            if(msg.equalsIgnoreCase(p.getName())){
                p.sendMessage(getMessages().CHALLENGED_SELF);
                coinFlipOpponentList.remove(p.getName());
                return;
            }

            BlazeUser user = getManager().getBlazeUser(p.getName());

            OfflinePlayer target = null;
            for (OfflinePlayer offp : Bukkit.getOfflinePlayers()) {
                if(offp.getName().equalsIgnoreCase(msg)) target = offp;
            }

            if(target == null){
                p.sendMessage(getMessages().NONEXISTENT_CHAT_PLAYER);
                coinFlipOpponentList.remove(p.getName());
                return;
            }

            user.setCoinFlipSpecific(target.getName());

            CoinFlipCreateGameGui.create(p);

            coinFlipOpponentList.remove(p.getName());
        }
    }

}
