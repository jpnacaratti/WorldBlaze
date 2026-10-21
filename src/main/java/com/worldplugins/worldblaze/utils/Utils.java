package com.worldplugins.worldblaze.utils;

import com.grandemc.cash.GrandeCash;
import com.grandemc.cash.api.GrandeCashAPI;
import com.worldplugins.worldblaze.guis.CrashGamblersGui;
import com.worldplugins.worldblaze.guis.DoubleGamblersGui;
import com.worldplugins.worldblaze.guis.GraphGamblersGui;
import com.worldplugins.worldblaze.objects.CoinFlipBet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.text.DecimalFormat;
import java.util.*;

import static com.worldplugins.worldblaze.WorldBlaze.*;

public class Utils {

    public static String[] formats = getInstance().getConfig().getStringList("formats").toArray(new String[0]);

    public static String letterFormatter(Object bigDecimal) {

        String val = new DecimalFormat("#,###").format(bigDecimal).replace(".", ",");
        Integer ii = val.indexOf(","), i = val.split(",").length;
        if (ii == -1)
            return val;
        return (val.substring(0, ii + 2) + formats[i]).replace(",0", "");
    }

    public static double passLetterToNumber(String text){

        try {
            return Double.parseDouble(text);
        }catch (NumberFormatException ignored) {}

        int index = 0;
        String toRem = null;
        for (String format : formats) {
            if(text.endsWith(format) && !format.isEmpty()){
                toRem = format;
                break;
            }
            index++;
        }

        if(toRem == null) return Double.parseDouble(text);

        String str = text.replace(toRem, "");
        double d = Double.parseDouble(str.replace(",", "."));

        return d * Math.pow(1000, (index - 1));
    }

    public static String c(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static boolean checkValues(Player p, double cash, double money){

        if(econ.getBalance(p) < money){
            p.sendMessage(getMessages().INSUFFICIENT_MONEY);
            return false;
        }

        if(Bukkit.getPluginManager().getPlugin("GrandeCash") != null) {
            GrandeCashAPI api = GrandeCash.api();
            if (!api.has(p.getUniqueId(), cash)) {
                p.sendMessage(getMessages().INSUFFICIENT_CASH);
                return false;
            }
        }

        return true;
    }

    public static void removeValues(Player p, double cash, double money){
        econ.withdrawPlayer(p, money);

        if(Bukkit.getPluginManager().getPlugin("GrandeCash") != null) {
            GrandeCashAPI api = GrandeCash.api();
            api.withdraw(p.getUniqueId(), cash);
        }
    }

    public static void depositValues(OfflinePlayer p, double cash, double money){
        econ.depositPlayer(p, money);

        if(Bukkit.getPluginManager().getPlugin("GrandeCash") != null) {
            GrandeCashAPI api = GrandeCash.api();
            api.deposit(p.getUniqueId(), cash);
        }
    }

    public static void sendDebugMessage(String msg){
        if (!send) return;

        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public static int getNewBetId(){

        int toR = getDataYml().getConfig().getInt("aposta-id");

        getDataYml().getConfig().set("aposta-id", toR + 1);
        getDataYml().saveFile();

        return toR;
    }

    public static void main(String[] args) {
        for (int a = 7; a >= 0; a--) {
            System.out.println(a);
        }

        System.out.println("----");

        for (int i = 0; i < 4; i++) {
            System.out.println(i);
        }

        System.out.println("----");

        int colum = 7;
        for (int i = 1; i <= 8 - colum; i++) {
            for (int a = 0; a < 5; a++) {
                System.out.println((colum + i) + (a * 9));
            }
        }

        System.out.println("----");

        for (int a = 5; a >= 0; a--){
            for (int i = 0; i < 5; i++) {
                System.out.println((a + (9 * i)));
            }
        }

        System.out.println("----");

        int lastValue = 37;
        for (int i = 0; i < 5; i++) {
            int cache = lastValue - 9;
            if(cache < 0) break;
            lastValue = cache;
        }

        System.out.println(lastValue);

        System.out.println("----");

        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            l.add(i);
        }

        for (int i = 0; i < 15; i++) {
            System.out.println(l.get((l.size() - 9) - i));
        }

        System.out.println("----");

        List<Integer> ints = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ints.add(i);
        }

        System.out.println("Antigamente:");
        ints.forEach(System.out::println);

        System.out.println("Novo:");
        Collections.sort(ints);
        Collections.reverse(ints);
        ints.forEach(System.out::println);

        System.out.println("----");
        System.out.println(passLetterToNumber("1,5B"));
        System.out.println(letterFormatter(passLetterToNumber("1,5B")));
    }

    public static boolean isAvailableForLockOrReload(){

        boolean crash = getCrashManager().getCrashBets().isEmpty();
        boolean graph = getGraphManager().getGraphBets().isEmpty();
        boolean doubleB = getDoubleManager().getDoubleBets().isEmpty();
        boolean mine = getMineManager().getMineBets().isEmpty();

        return crash && graph && doubleB && mine;
    }

    public static void closeAllInventories(){
        Bukkit.getScheduler().runTask(getInstance(), ()->{
            for (Map.Entry<String, Inventory> entry : getGraphManager().getGraphInventories().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if(player != null) player.closeInventory();
            }
            getGraphManager().getGraphInventories().clear();

            for (String s : getGraphManager().getGamblersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) player.closeInventory();
            }
            getGraphManager().getGamblersGuiOpen().clear();

            for (Map.Entry<String, Inventory> entry : getCrashManager().getCrashInventories().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if(player != null) player.closeInventory();
            }
            getCrashManager().getCrashInventories().clear();

            for (String s : getCrashManager().getGamblersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) player.closeInventory();
            }
            getCrashManager().getGamblersGuiOpen().clear();

            for (Map.Entry<String, Inventory> entry : getDoubleManager().getDoubleInventories().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if(player != null) player.closeInventory();
            }
            getDoubleManager().getDoubleInventories().clear();

            for (String s : getDoubleManager().getGamblersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) player.closeInventory();
            }
            getDoubleManager().getGamblersGuiOpen().clear();

            for (Map.Entry<String, Inventory> entry : getMineManager().getPrincipalInventories().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if(player != null) player.closeInventory();
            }
            getMineManager().getPrincipalInventories().clear();

            for (String s : getMineManager().getPlayersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) player.closeInventory();
            }
            getMineManager().getPlayersGuiOpen().clear();

            for (Map.Entry<String, Inventory> entry : getCoinFlipManager().getPrincipalInventories().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if(player != null) player.closeInventory();
            }
            getCoinFlipManager().getPrincipalInventories().clear();

            for (String s : getCoinFlipManager().getCoinFlipGamblersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) player.closeInventory();
            }
            getCoinFlipManager().getCoinFlipGamblersGuiOpen().clear();
        });
    }

    public static void createCrashGamblersGui(){
        Bukkit.getScheduler().runTask(getInstance(), ()->{
            for (String s : getCrashManager().getGamblersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) CrashGamblersGui.create(player);
            }
        });
    }

    public static void createGraphGamblersGui(){
        Bukkit.getScheduler().runTask(getInstance(), ()-> {
            for (String s : getGraphManager().getGamblersGuiOpen()) {
                Player p = Bukkit.getPlayer(s);

                if (p != null) GraphGamblersGui.create(p);
            }
        });
    }

    public static void createDoubleGamblersGui(){
        Bukkit.getScheduler().runTask(getInstance(), ()->{
            for (String s : getDoubleManager().getGamblersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) DoubleGamblersGui.create(player);
            }
        });
    }

    public static void giveCoinFlipCoins(){
        Bukkit.getScheduler().runTask(getInstance(), ()->{
            for (CoinFlipBet coinFlipBet : getCoinFlipManager().getCoinFlipBets()) {
                Player p = Bukkit.getPlayer(coinFlipBet.getOwner());
                p.closeInventory();

                depositValues(Bukkit.getOfflinePlayer(coinFlipBet.getOwner()), coinFlipBet.getCashValue(), coinFlipBet.getMoneyValue());
                if(coinFlipBet.getOpponent() != null){
                    Player opponent = Bukkit.getPlayer(coinFlipBet.getOpponent());
                    opponent.closeInventory();

                    depositValues(Bukkit.getOfflinePlayer(coinFlipBet.getOpponent()), coinFlipBet.getCashValue(), coinFlipBet.getMoneyValue());
                }
            }
        });
    }

}
