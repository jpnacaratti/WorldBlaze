package com.worldplugins.worldblaze.managers;

import com.worldplugins.worldblaze.guis.CoinFlipGamblersGui;
import com.worldplugins.worldblaze.objects.CoinFlipBet;
import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;
import static com.worldplugins.worldblaze.utils.Utils.letterFormatter;

public class CoinFlipManager {

    // VARIABLES THAT WILL NOT CHANGE UNDER ANY CIRCUMSTANCES
    private final List<CoinFlipBet> coinFlipBets;
    private final Set<String> coinFlipGamblersGuiOpen;
    private final Map<String, Inventory> mainInventories;
    private final double startingTime, flippingTime, moneyAlert, cashAlert;
    private final int breakTime;
    private final String mainMenuTitle, coinFlipMenuTitle;
    private final Random rand;

    // VARIABLES THAT WILL NOT CHANGE UNDER ANY CIRCUMSTANCES, ITEMS
    private final ItemBuilder mainGamesItem, startingItem, headsItem, tailsItem;
    private final String mainGamesName, startingName, headsName, tailsName, finishName, colorChoice, colorNoChoice;
    private final List<String> mainGamesLore, startingLore, headsLore, tailsLore, finishLore;
    private final int mainGamesSlot, choiceSlot;

    public CoinFlipManager(){

        this.coinFlipBets = new ArrayList<>();
        this.mainInventories = new HashMap<>();
        this.coinFlipGamblersGuiOpen = new HashSet<>();

        this.startingTime = getInstance().getConfig().getDouble("config.coinflip.starting-time");
        this.flippingTime = getInstance().getConfig().getDouble("config.coinflip.flipping-time");

        this.breakTime = getInstance().getConfig().getInt("config.coinflip.swap-interval");

        this.moneyAlert = getInstance().getConfig().getDouble("config.coinflip.money-alert");
        this.cashAlert = getInstance().getConfig().getDouble("config.coinflip.cash-alert");

        this.rand = new Random();

        FileConfiguration getConfig = getCoinFlipGuiYml().getConfig();

        this.coinFlipMenuTitle = c(getConfig.getString("title"));

        this.colorChoice = c(getConfig.getString("chosen-color"));
        this.colorNoChoice = c(getConfig.getString("not-chosen-color"));

        this.finishName = c(getConfig.getString("finished.name"));
        this.finishLore = getConfig.getStringList("finished.lore");

        if (getConfig.getBoolean("tails.use-head")) tailsItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("tails.texture")));
        else tailsItem = new ItemBuilder(Material.getMaterial(getConfig.getString("tails.id")), 1, (byte) getConfig.getInt("tails.data"));
        tailsItem.setGlow(getConfig.getBoolean("tails.glow"));

        this.tailsLore = getConfig.getStringList("tails.lore");
        this.tailsName = c(getConfig.getString("tails.name"));

        if (getConfig.getBoolean("heads.use-head")) headsItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("heads.texture")));
        else headsItem = new ItemBuilder(Material.getMaterial(getConfig.getString("heads.id")), 1, (byte) getConfig.getInt("heads.data"));
        headsItem.setGlow(getConfig.getBoolean("heads.glow"));

        this.headsLore = getConfig.getStringList("heads.lore");
        this.headsName = c(getConfig.getString("heads.name"));

        if (getConfig.getBoolean("starting.use-head")) startingItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("starting.texture")));
        else startingItem = new ItemBuilder(Material.getMaterial(getConfig.getString("starting.id")), 1, (byte) getConfig.getInt("starting.data"));
        startingItem.setGlow(getConfig.getBoolean("starting.glow"));

        this.startingLore = getConfig.getStringList("starting.lore");
        this.startingName = c(getConfig.getString("starting.name"));

        this.choiceSlot = getConfig.getInt("coin-slot");

        FileConfiguration getMainConfig = getCoinFlipMainGuiYml().getConfig();

        this.mainMenuTitle = c(getMainConfig.getString("title"));

        if (getMainConfig.getBoolean("items.games.use-head")) mainGamesItem = new ItemBuilder(SkullCreator.itemFromUrl(getMainConfig.getString("items.games.texture")));
        else mainGamesItem = new ItemBuilder(Material.getMaterial(getMainConfig.getString("items.games.id")), 1, (byte) getMainConfig.getInt("items.games.data"));
        mainGamesItem.setGlow(getMainConfig.getBoolean("items.games.glow"));

        this.mainGamesLore = getMainConfig.getStringList("items.games.lore");
        this.mainGamesName = c(getMainConfig.getString("items.games.name"));

        this.mainGamesSlot = getMainConfig.getInt("items.games.slot");

    }

    public List<CoinFlipBet> getCoinFlipBets() {
        return coinFlipBets;
    }

    public Map<String, Inventory> getMainInventories() {
        return mainInventories;
    }

    public Set<String> getCoinFlipGamblersGuiOpen() {
        return coinFlipGamblersGuiOpen;
    }

    public String getMainMenuTitle() {
        return mainMenuTitle;
    }

    public String getCoinFlipMenuTitle() {
        return coinFlipMenuTitle;
    }

    public int getBreakTime() {
        return breakTime;
    }

    public double getMoneyAlert() {
        return moneyAlert;
    }

    public double getCashAlert() {
        return cashAlert;
    }

    public ItemStack getMainGamesItem(String player){
        ItemBuilder clone = mainGamesItem.clone();

        clone.setName(mainGamesName);

        mainGamesLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{ongoing}", getNumClosedGames() + "").
                replace("{waiting}", getNumOpenGames() + "").
                replace("{specific}", getNumSpecificGames(player) + ""))));

        return clone.toItemStack();
    }

    public ItemStack getStartingItem(CoinFlipBet bet) {
        ItemBuilder clone = startingItem.clone();

        clone.setName(startingName);

        startingLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{time}", String.format("%.2f", (startingTime - bet.getSubtract()))).
                replace("{money}", letterFormatter(bet.getMoneyValue())).
                replace("{cash}", letterFormatter(bet.getCashValue())).
                replace("{owner-player}", bet.getOwner()).
                replace("{opponent-player}", bet.getOpponent()).
                replace("{owner-choice}",bet.isHeads() ? "Heads" : "Tails").
                replace("{opponent-choice}",bet.isHeads() ? "Tails" : "Heads"))));

        return clone.toItemStack();
    }

    public ItemStack getHeadsItem(CoinFlipBet bet) {
        ItemBuilder clone = headsItem.clone();

        clone.setName(headsName);

        for (String s : headsLore) {
            if(s.contains("{color}")){

                boolean change = false;
                if(s.contains("{owner-player}")){

                    if(bet.isHeads() && bet.isChoiceHeads()) change = true;
                    else if (!bet.isHeads() && !bet.isChoiceHeads()) change = true;

                }else if(s.contains("{opponent-player}")){

                    if(bet.isHeads() && !bet.isChoiceHeads()) change = true;
                    else if (!bet.isHeads() && bet.isChoiceHeads()) change = true;

                }

                clone.addLoreLine(c(s.
                        replace("{color}", change ? colorChoice : colorNoChoice).
                        replace("{money}", letterFormatter(bet.getMoneyValue())).
                        replace("{cash}", letterFormatter(bet.getCashValue())).
                        replace("{owner-player}", bet.getOwner()).
                        replace("{opponent-player}", bet.getOpponent()).
                        replace("{owner-choice}",bet.isHeads() ? "Heads" : "Tails").
                        replace("{opponent-choice}",bet.isHeads() ? "Tails" : "Heads")));

            }else{
                clone.addLoreLine(c(s.
                        replace("{money}", letterFormatter(bet.getMoneyValue())).
                        replace("{cash}", letterFormatter(bet.getCashValue())).
                        replace("{owner-player}", bet.getOwner()).
                        replace("{opponent-player}", bet.getOpponent()).
                        replace("{owner-choice}",bet.isHeads() ? "Heads" : "Tails").
                        replace("{opponent-choice}",bet.isHeads() ? "Tails" : "Heads")));
            }
        }

        return clone.toItemStack();
    }

    public ItemStack getTailsItem(CoinFlipBet bet) {
        ItemBuilder clone = tailsItem.clone();

        clone.setName(tailsName);

        for (String s : tailsLore) {
            if(s.contains("{color}")){

                boolean change = false;
                if(s.contains("{owner-player}")){

                    if(bet.isHeads() && bet.isChoiceHeads()) change = true;
                    else if (!bet.isHeads() && !bet.isChoiceHeads()) change = true;

                }else if(s.contains("{opponent-player}")){

                    if(bet.isHeads() && !bet.isChoiceHeads()) change = true;
                    else if (!bet.isHeads() && bet.isChoiceHeads()) change = true;

                }

                clone.addLoreLine(c(s.
                        replace("{color}", change ? colorChoice : colorNoChoice).
                        replace("{money}", letterFormatter(bet.getMoneyValue())).
                        replace("{cash}", letterFormatter(bet.getCashValue())).
                        replace("{owner-player}", bet.getOwner()).
                        replace("{opponent-player}", bet.getOpponent()).
                        replace("{owner-choice}",bet.isHeads() ? "Heads" : "Tails").
                        replace("{opponent-choice}",bet.isHeads() ? "Tails" : "Heads")));

            }else{
                clone.addLoreLine(c(s.
                        replace("{money}", letterFormatter(bet.getMoneyValue())).
                        replace("{cash}", letterFormatter(bet.getCashValue())).
                        replace("{owner-player}", bet.getOwner()).
                        replace("{opponent-player}", bet.getOpponent()).
                        replace("{owner-choice}",bet.isHeads() ? "Heads" : "Tails").
                        replace("{opponent-choice}",bet.isHeads() ? "Tails" : "Heads")));
            }
        }

        return clone.toItemStack();
    }

    public ItemStack getFinishItem(CoinFlipBet bet){

        ItemBuilder item;
        if (bet.isChoiceHeads()) item = headsItem.clone();
        else item = tailsItem.clone();

        item.setName(finishName);

        String winner = "-/-";
        if(bet.isHeads() && bet.isChoiceHeads()) winner = bet.getOwner();
        else if(!bet.isHeads() && !bet.isChoiceHeads()) winner = bet.getOwner();
        else if(bet.isHeads() && !bet.isChoiceHeads()) winner = bet.getOpponent();
        else if(!bet.isHeads() && bet.isChoiceHeads()) winner = bet.getOpponent();

        String finalWinner = winner;
        finishLore.forEach(w -> item.addLoreLine(c(w.replace("{final-choice}", bet.isChoiceHeads() ? "Heads" : "Tails").
                replace("{winner}", finalWinner).
                replace("{money}", letterFormatter(bet.getMoneyValue() * 2)).
                replace("{cash}", letterFormatter(bet.getCashValue() * 2))
        )));

        return item.toItemStack();
    }

    public int getMainGamesSlot() {
        return mainGamesSlot;
    }

    public int getChoiceSlot() {
        return choiceSlot;
    }

    public double getStartingTime() {
        return startingTime;
    }

    public double getFlippingTime() {
        return flippingTime;
    }

    public int getNumOpenGames(){
        int i = 0;

        for (CoinFlipBet coinFlipBet : coinFlipBets) {
            if(coinFlipBet.getOpponent() == null) i++;
        }

        return i;
    }

    public int getNumClosedGames(){
        int i = 0;

        for (CoinFlipBet coinFlipBet : coinFlipBets) {
            if(coinFlipBet.getOpponent() != null) i++;
        }

        return i;
    }

    public int getNumSpecificGames(String p){
        int i = 0;

        for (CoinFlipBet coinFlipBet : coinFlipBets) {
            if(coinFlipBet.getOpponent() != null && coinFlipBet.getOpponent().equalsIgnoreCase(p)) i++;
        }

        return i;
    }

    public boolean isPlayerParticipating(String player){
        return coinFlipBets.stream().filter(w -> w.getOwner().equalsIgnoreCase(player)).findFirst().orElse(null) != null;
    }

    public CoinFlipBet getCoinFlipBet(String name){
        return coinFlipBets.stream().filter(w -> w.getOwner().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public CoinFlipBet getCoinFlipBetClose(String name){
        return coinFlipBets.stream().filter(w -> w.getOwner().equalsIgnoreCase(name) || (w.getOpponent() != null && w.getOpponent().equalsIgnoreCase(name))).findFirst().orElse(null);
    }

    public void updateMainInventories(){
        Bukkit.getScheduler().runTask(getInstance(), ()->{
            for (Map.Entry<String, Inventory> entry : getCoinFlipManager().getMainInventories().entrySet()) {
                entry.getValue().setItem(getCoinFlipManager().getMainGamesSlot(), getCoinFlipManager().getMainGamesItem(entry.getKey()));
            }
        });
    }

    public void updateGamblersInventories(){
        Bukkit.getScheduler().runTask(getInstance(), ()-> {
            for (String s : getCoinFlipManager().getCoinFlipGamblersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if (player != null) CoinFlipGamblersGui.create(player);
            }
        });
    }

    public List<CoinFlipBet> getCoinFlipStartedGames(){
        return coinFlipBets.stream().filter(w -> w.getOpponent() != null && w.getOwner() != null).collect(Collectors.toList());
    }

    public boolean getRandomChoice(){
        return rand.nextBoolean();
    }

    public boolean isOwnerWin(CoinFlipBet bet){

        if(bet.isHeads() && bet.isChoiceHeads()) return true;
        else if(!bet.isHeads() && !bet.isChoiceHeads()) return true;
        else if(bet.isHeads() && !bet.isChoiceHeads()) return false;
        else if(!bet.isHeads() && bet.isChoiceHeads()) return false;

        return false;
    }
}
