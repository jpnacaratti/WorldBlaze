package com.worldplugins.worldblaze.managers;

import com.worldplugins.worldblaze.enums.DoubleColor;
import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.objects.DoubleAnimationSpot;
import com.worldplugins.worldblaze.objects.DoubleBet;
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
import static com.worldplugins.worldblaze.utils.Utils.*;

public class DoubleManager {

    // VARIABLES THAT WILL NOT CHANGE UNDER ANY CIRCUMSTANCES
    private final Set<String> gamblersGuiOpen;
    private final List<DoubleBet> doubleBets;
    private List<DoubleAnimationSpot> doubleAnimationSpots;
    private final Map<String, Inventory> doubleInventories;
    private final Map<ItemStack, List<Integer>> finalItems;
    private final String menuTitle;
    private final double riggingMoney, riggingCash, riggingChance, startingTime, stoppedTime, animationTotalTime, breakChangeTime, startRedChance, startWhiteChance, startBlackChance, moneyAlert, cashAlert;
    private final int maxHistoryValues, startBreak, finalBreak;
    private final boolean rigging;
    private final Random rand;

    // VARIABLES THAT WILL CHANGE AS THE DOUBLE PROGRESSES
    private boolean starting, stopped;
    private double totalBetCash, totalBetMoney, redAverage, whiteAverage, blackAverage, startingRemainingTime, stoppedRemainingTime, animationRemainingTime, animationSpeedRemainingTime, animationSpeedTime, whiteChance, redChance, blackChance;
    private int numOfItem;
    private DoubleColor roundChosenColor;
    private final List<DoubleAnimationSpot> setAnimationSpots;
    private List<DoubleColor> history;
    private Map<Integer, DoubleColor> colorCache;

    // VARIABLES THAT WILL NOT CHANGE UNDER ANY CIRCUMSTANCES
    private final ItemBuilder redItem, whiteItem, blackItem, moneyItem, cashItem, betRedItem, betWhiteItem, betBlackItem, historyItem, gamblersItem;
    private final List<String> moneyLore, cashLore, betRedLore, betWhiteLore, betBlackLore, historyLore, gamblersLore;
    private final String redStatusBet, redStatusNoBet, redRoundStatusStarting, redRoundStatusStarted, whiteStatusBet, whiteStatusNoBet, whiteRoundStatusStarting, whiteRoundStatusStarted, blackStatusBet, blackStatusNoBet, blackRoundStatusStarting, blackRoundStatusStarted, moneyName, cashName, betRedName, betWhiteName, betBlackName, historyName, gamblersName, redDisplay, whiteDisplay, blackDisplay;
    private final boolean gamblersUsePlayersHead;
    private final int moneySlot, cashSlot, betRedSlot, betWhiteSlot, betBlackSlot, historySlot, gamblersSlot;

    public DoubleManager() {

        FileConfiguration getConfig = getDoubleGuiYml().getConfig();

        this.menuTitle = c(getConfig.getString("title"));

        this.gamblersGuiOpen = new HashSet<>();
        this.setAnimationSpots = new ArrayList<>();

        this.doubleInventories = new HashMap<>();
        this.colorCache = new HashMap<>();

        this.doubleAnimationSpots = new ArrayList<>();
        this.doubleBets = new ArrayList<>();
        this.history = new ArrayList<>();

        this.maxHistoryValues = getInstance().getConfig().getInt("config.double.max-history-values");

        this.rand = new Random();

        this.startingTime = getInstance().getConfig().getDouble("config.double.starting-time");
        this.stoppedTime = getInstance().getConfig().getDouble("config.double.finished-time");

        this.startingRemainingTime = startingTime;
        this.stoppedRemainingTime = stoppedTime;

        this.starting = true;

        this.breakChangeTime = getInstance().getConfig().getDouble("config.double.interval-change-time");
        this.startBreak = getInstance().getConfig().getInt("config.double.initial-interval");
        this.finalBreak = getInstance().getConfig().getInt("config.double.final-interval");

        this.rigging = getInstance().getConfig().getBoolean("config.double.rigging.enabled");
        this.riggingMoney = getInstance().getConfig().getDouble("config.double.rigging.money");
        this.riggingCash = getInstance().getConfig().getDouble("config.double.rigging.cash");
        this.riggingChance = getInstance().getConfig().getDouble("config.double.rigging.chance");

        this.animationSpeedTime = startBreak;
        this.animationSpeedRemainingTime = animationSpeedTime;

        this.animationTotalTime = getInstance().getConfig().getDouble("config.double.animation-time");
        this.animationRemainingTime = animationTotalTime;

        this.startRedChance = getInstance().getConfig().getDouble("config.double.initial-red-chance");
        this.startWhiteChance = getInstance().getConfig().getDouble("config.double.initial-white-chance");
        this.startBlackChance = getInstance().getConfig().getDouble("config.double.initial-black-chance");

        redChance = startRedChance;
        whiteChance = startWhiteChance;
        blackChance = startBlackChance;

        this.moneyAlert = getInstance().getConfig().getDouble("config.double.money-alert");
        this.cashAlert = getInstance().getConfig().getDouble("config.double.cash-alert");

        for (String s : getConfig.getConfigurationSection("starting-animation").getKeys(false)) doubleAnimationSpots.add(new DoubleAnimationSpot(s));
        doubleAnimationSpots = doubleAnimationSpots.stream().sorted(Comparator.comparingDouble(DoubleAnimationSpot::getTime).reversed()).collect(Collectors.toList());

        this.finalItems = new HashMap<>();
        for (String s : getConfig.getConfigurationSection("final-glass").getKeys(false)) {
            finalItems.put(new ItemStack(Material.getMaterial(getConfig.getString("final-glass."+s+".id")),
                    1, (short) getConfig.getInt("final-glass."+s+".data")), getConfig.getIntegerList("final-glass."+s+".slot"));
        }

        if (getConfig.getBoolean("red.use-head")) redItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("red.texture")));
        else redItem = new ItemBuilder(Material.getMaterial(getConfig.getString("red.id")), 1, (byte) getConfig.getInt("red.data"));
        redItem.setGlow(getConfig.getBoolean("red.glow")).setName(c(getConfig.getString("red.name")));

        getConfig.getStringList("red.lore").forEach(w -> redItem.addLoreLine(c(w)));

        if (getConfig.getBoolean("white.use-head")) whiteItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("white.texture")));
        else whiteItem = new ItemBuilder(Material.getMaterial(getConfig.getString("white.id")), 1, (byte) getConfig.getInt("white.data"));
        whiteItem.setGlow(getConfig.getBoolean("white.glow")).setName(c(getConfig.getString("white.name")));

        getConfig.getStringList("white.lore").forEach(w -> whiteItem.addLoreLine(c(w)));

        if (getConfig.getBoolean("black.use-head")) blackItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("black.texture")));
        else blackItem = new ItemBuilder(Material.getMaterial(getConfig.getString("black.id")), 1, (byte) getConfig.getInt("black.data"));
        blackItem.setGlow(getConfig.getBoolean("black.glow")).setName(c(getConfig.getString("black.name")));

        getConfig.getStringList("black.lore").forEach(w -> blackItem.addLoreLine(c(w)));

        if (getConfig.getBoolean("items.money.use-head")) moneyItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.money.texture")));
        else moneyItem = new ItemBuilder(Material.getMaterial(getConfig.getString("items.money.id")), 1, (byte) getConfig.getInt("items.money.data"));
        moneyItem.setGlow(getConfig.getBoolean("items.money.glow"));

        this.moneyLore = getConfig.getStringList("items.money.lore");
        this.moneyName = c(getConfig.getString("items.money.name"));

        this.moneySlot = getConfig.getInt("items.money.slot");

        if (getConfig.getBoolean("items.cash.use-head")) cashItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.cash.texture")));
        else cashItem = new ItemBuilder(Material.getMaterial(getConfig.getString("items.cash.id")), 1, (byte) getConfig.getInt("items.cash.data"));
        cashItem.setGlow(getConfig.getBoolean("items.cash.glow"));

        this.cashLore = getConfig.getStringList("items.cash.lore");
        this.cashName = c(getConfig.getString("items.cash.name"));

        this.cashSlot = getConfig.getInt("items.cash.slot");

        if (getConfig.getBoolean("items.bet-red.use-head")) betRedItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.bet-red.texture")));
        else betRedItem = new ItemBuilder(Material.getMaterial(getConfig.getString("items.bet-red.id")), 1, (byte) getConfig.getInt("items.bet-red.data"));
        betRedItem.setGlow(getConfig.getBoolean("items.bet-red.glow"));

        this.betRedLore = getConfig.getStringList("items.bet-red.lore");
        this.betRedName = c(getConfig.getString("items.bet-red.name"));

        this.betRedSlot = getConfig.getInt("items.bet-red.slot");

        this.redStatusBet = c(getConfig.getString("items.bet-red.status-bet"));
        this.redStatusNoBet = c(getConfig.getString("items.bet-red.status-not-bet"));

        this.redRoundStatusStarted = c(getConfig.getString("items.bet-red.round-status-started"));
        this.redRoundStatusStarting = c(getConfig.getString("items.bet-red.round-status-not-started"));

        if (getConfig.getBoolean("items.bet-white.use-head")) betWhiteItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.bet-white.texture")));
        else betWhiteItem = new ItemBuilder(Material.getMaterial(getConfig.getString("items.bet-white.id")), 1, (byte) getConfig.getInt("items.bet-white.data"));
        betWhiteItem.setGlow(getConfig.getBoolean("items.bet-white.glow"));

        this.betWhiteLore = getConfig.getStringList("items.bet-white.lore");
        this.betWhiteName = c(getConfig.getString("items.bet-white.name"));

        this.betWhiteSlot = getConfig.getInt("items.bet-white.slot");

        this.whiteStatusBet = c(getConfig.getString("items.bet-white.status-bet"));
        this.whiteStatusNoBet = c(getConfig.getString("items.bet-white.status-not-bet"));

        this.whiteRoundStatusStarted = c(getConfig.getString("items.bet-white.round-status-started"));
        this.whiteRoundStatusStarting = c(getConfig.getString("items.bet-white.round-status-not-started"));

        if (getConfig.getBoolean("items.bet-black.use-head")) betBlackItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.bet-black.texture")));
        else betBlackItem = new ItemBuilder(Material.getMaterial(getConfig.getString("items.bet-black.id")), 1, (byte) getConfig.getInt("items.bet-black.data"));
        betBlackItem.setGlow(getConfig.getBoolean("items.bet-black.glow"));

        this.betBlackLore = getConfig.getStringList("items.bet-black.lore");
        this.betBlackName = c(getConfig.getString("items.bet-black.name"));

        this.betBlackSlot = getConfig.getInt("items.bet-black.slot");

        this.blackStatusBet = c(getConfig.getString("items.bet-black.status-bet"));
        this.blackStatusNoBet = c(getConfig.getString("items.bet-black.status-not-bet"));

        this.blackRoundStatusStarted = c(getConfig.getString("items.bet-black.round-status-started"));
        this.blackRoundStatusStarting = c(getConfig.getString("items.bet-black.round-status-not-started"));

        if (getConfig.getBoolean("items.history.use-head")) historyItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.history.texture")));
        else historyItem = new ItemBuilder(Material.getMaterial(getConfig.getString("items.history.id")), 1, (byte) getConfig.getInt("items.history.data"));
        historyItem.setGlow(getConfig.getBoolean("items.history.glow"));

        this.historyLore = getConfig.getStringList("items.history.lore");
        this.historyName = c(getConfig.getString("items.history.name"));

        this.historySlot = getConfig.getInt("items.history.slot");

        this.redDisplay = c(getConfig.getString("items.history.display-red"));
        this.whiteDisplay = c(getConfig.getString("items.history.display-white"));
        this.blackDisplay = c(getConfig.getString("items.history.display-black"));

        if (getConfig.getBoolean("items.bettors.use-head-last-player")) gamblersItem = new ItemBuilder(SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654"));
        else if (getConfig.getBoolean("items.bettors.use-head")) gamblersItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.bettors.texture")));
        else gamblersItem = new ItemBuilder(Material.getMaterial(getConfig.getString("items.bettors.id")), 1, (byte) getConfig.getInt("items.bettors.data"));
        gamblersItem.setGlow(getConfig.getBoolean("items.bettors.glow"));

        this.gamblersLore = getConfig.getStringList("items.bettors.lore");
        this.gamblersName = c(getConfig.getString("items.bettors.name"));

        this.gamblersSlot = getConfig.getInt("items.bettors.slot");

        this.gamblersUsePlayersHead = getConfig.getBoolean("items.bettors.use-head-last-player");
    }

    public ItemStack getRedItem() {
        return redItem.toItemStack();
    }

    public ItemStack getWhiteItem() {
        return whiteItem.toItemStack();
    }

    public ItemStack getBlackItem() {
        return blackItem.toItemStack();
    }

    public ItemStack getMoneyItem(BlazeUser user) {
        ItemBuilder clone = moneyItem.clone();

        clone.setName(moneyName);

        moneyLore.forEach(w -> clone.addLoreLine(c(w.replace("{money}", letterFormatter(user.getDoubleMoneyValue())))));

        return clone.toItemStack();
    }

    public ItemStack getCashItem(BlazeUser user) {
        ItemBuilder clone = cashItem.clone();

        clone.setName(cashName);

        cashLore.forEach(w -> clone.addLoreLine(c(w.replace("{cash}", letterFormatter(user.getDoubleCashValue())))));

        return clone.toItemStack();
    }

    public ItemStack getBetRedItem(BlazeUser user) {
        ItemBuilder clone = betRedItem.clone();

        clone.setName(betRedName);

        betRedLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{red-average}", String.format("%.2f", redAverage)).
                replace("{money}", letterFormatter(user.getDoubleMoneyValue())).
                replace("{cash}", letterFormatter(user.getDoubleCashValue())).
                replace("{status}", getRedStatusBet(user.getOwner())).
                replace("{round-status}",getRedRoundStatus()))));

        return clone.toItemStack();
    }

    public ItemStack getBetWhiteItem(BlazeUser user) {
        ItemBuilder clone = betWhiteItem.clone();

        clone.setName(betWhiteName);

        betWhiteLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{white-average}", String.format("%.2f", whiteAverage)).
                replace("{money}", letterFormatter(user.getDoubleMoneyValue())).
                replace("{cash}", letterFormatter(user.getDoubleCashValue())).
                replace("{status}", getWhiteStatusBet(user.getOwner())).
                replace("{round-status}",getWhiteRoundStatus()))));

        return clone.toItemStack();
    }

    public ItemStack getBetBlackItem(BlazeUser user) {
        ItemBuilder clone = betBlackItem.clone();

        clone.setName(betBlackName);

        betBlackLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{black-average}", String.format("%.2f", blackAverage)).
                replace("{money}", letterFormatter(user.getDoubleMoneyValue())).
                replace("{cash}", letterFormatter(user.getDoubleCashValue())).
                replace("{status}", getBlackStatusBet(user.getOwner())).
                replace("{round-status}",getBlackRoundStatus()))));

        return clone.toItemStack();
    }

    public ItemStack getHistoryItem() {
        ItemBuilder clone = historyItem.clone();

        clone.setName(historyName);

        List<DoubleColor> copy = new ArrayList<>(history);
        Collections.reverse(copy);

        for (String s : historyLore) {
            if(s.contains("{history}")){
                for (DoubleColor doubleColor : copy){
                    if (doubleColor == DoubleColor.RED) clone.addLoreLine(c(s.replace("{history}", redDisplay)));
                    else if (doubleColor == DoubleColor.WHITE) clone.addLoreLine(c(s.replace("{history}", whiteDisplay)));
                    else clone.addLoreLine(c(s.replace("{history}", blackDisplay)));
                }
            }else clone.addLoreLine(c(s));
        }

        return clone.toItemStack();
    }

    private double getColorTotalBetCash(DoubleColor color){
        final double[] d = {0};

        doubleBets.stream().filter(w -> w.getColor() == color).forEach(w -> d[0] += w.getCashValue());

        return d[0];
    }

    private double getColorTotalBetMoney(DoubleColor color){
        final double[] d = {0};

        doubleBets.stream().filter(w -> w.getColor() == color).forEach(w -> d[0] += w.getMoneyValue());

        return d[0];
    }

    public void setRoundChosenColor(){

        if(rigging && (totalBetCash >= riggingCash || totalBetMoney >= riggingMoney)){
            if (rand.nextDouble() * 100 <= riggingChance) {

                double red;
                double white;
                double black;

                if (totalBetCash > 0) {
                    red = getColorTotalBetCash(DoubleColor.RED);
                    white = getColorTotalBetCash(DoubleColor.WHITE);
                    black = getColorTotalBetCash(DoubleColor.BLACK);
                    sendDebugMessage("[Double] Rigged (CASH)");
                } else {
                    red = getColorTotalBetMoney(DoubleColor.RED);
                    white = getColorTotalBetMoney(DoubleColor.WHITE);
                    black = getColorTotalBetMoney(DoubleColor.BLACK);
                    sendDebugMessage("[Double] Rigged (MONEY)");
                }

                Map<DoubleColor, Double> sorted = new HashMap<>();
                sorted.put(DoubleColor.RED, red);
                sorted.put(DoubleColor.WHITE, white);
                sorted.put(DoubleColor.BLACK, black);

                List<Map.Entry<DoubleColor, Double>> list = new ArrayList<>(sorted.entrySet());
                list.sort(Map.Entry.comparingByValue());

                Collections.reverse(list);

                DoubleColor colorOne = list.get(1).getKey();

                if(colorOne == DoubleColor.RED){
                    if (rand.nextDouble() * 100 <= startRedChance * 2) this.roundChosenColor = DoubleColor.RED;
                    else this.roundChosenColor = DoubleColor.WHITE;
                }else if(colorOne == DoubleColor.BLACK){
                    if (rand.nextDouble() * 100 <= startBlackChance * 2) this.roundChosenColor = DoubleColor.BLACK;
                    else this.roundChosenColor = DoubleColor.WHITE;
                }
            }
        }else this.roundChosenColor = getRandomDefinitiveColor();

        sendDebugMessage("[Double] The next color will be: " + roundChosenColor);

    }

    public ItemStack getGamblersItem() {
        ItemBuilder clone;
        if(!gamblersUsePlayersHead || doubleBets.size() == 0) clone = gamblersItem.clone();
        else clone = new ItemBuilder(SkullCreator.itemFromName(getLastPlayer()));

        clone.setName(gamblersName);

        gamblersLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{players}", doubleBets.size()+"").
                replace("{last-player}", getLastPlayer()).
                replace("{red-average}", String.format("%.2f", redAverage)).
                replace("{white-average}", String.format("%.2f", whiteAverage)).
                replace("{black-average}", String.format("%.2f", blackAverage)).
                replace("{total-money}", letterFormatter(totalBetMoney)).
                replace("{total-cash}", letterFormatter(totalBetCash)))));

        return clone.toItemStack();
    }

    public int getMoneySlot() {
        return moneySlot;
    }

    public int getCashSlot() {
        return cashSlot;
    }

    public int getBetRedSlot() {
        return betRedSlot;
    }

    public int getBetWhiteSlot() {
        return betWhiteSlot;
    }

    public int getBetBlackSlot() {
        return betBlackSlot;
    }

    public int getHistorySlot() {
        return historySlot;
    }

    public int getGamblersSlot() {
        return gamblersSlot;
    }

    public void setRedAverage() {
        if(doubleBets.size() == 0){
            redAverage = 0;
            return;
        }

        double numOfRed = 0;
        for (DoubleBet doubleBet : doubleBets) if(doubleBet.getColor() == DoubleColor.RED) numOfRed++;

        redAverage = (numOfRed / doubleBets.size()) * 100;
    }

    public void setWhiteAverage() {
        if(doubleBets.size() == 0){
            whiteAverage = 0;
            return;
        }

        double numOfWhite = 0;
        for (DoubleBet doubleBet : doubleBets) if(doubleBet.getColor() == DoubleColor.WHITE) numOfWhite++;

        whiteAverage = (numOfWhite / doubleBets.size()) * 100;
    }

    public void setBlackAverage() {
        if(doubleBets.size() == 0){
            blackAverage = 0;
            return;
        }

        double numOfBlack = 0;
        for (DoubleBet doubleBet : doubleBets) if(doubleBet.getColor() == DoubleColor.BLACK) numOfBlack++;

        blackAverage = (numOfBlack / doubleBets.size()) * 100;
    }

    public String getLastPlayer(){
        if(doubleBets.size() == 0) return  "-/-";

        return doubleBets.get(doubleBets.size() - 1).getOwner();
    }

    public void setTotalBetMoney(){
        if(getDoubleBets().size() == 0){
            totalBetMoney = 0;
            return;
        }

        final double[] var = {0};
        doubleBets.forEach(w -> var[0] += w.getMoneyValue());

        totalBetMoney = var[0];
    }

    public void setTotalBetCash(){
        if(getDoubleBets().size() == 0){
            totalBetCash = 0;
            return;
        }

        final double[] var = {0};
        doubleBets.forEach(w -> var[0] += w.getCashValue());

        totalBetCash = var[0];
    }

    public Set<String> getGamblersGuiOpen() {
        return gamblersGuiOpen;
    }

    public List<DoubleBet> getDoubleBets() {
        return doubleBets;
    }

    public Map<String, Inventory> getDoubleInventories() {
        return doubleInventories;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public void addHistory(DoubleColor color) {

        history.add(color);

        List<DoubleColor> cache = new ArrayList<>();

        for (int i = 0; i < maxHistoryValues; i++) {
            try{
                cache.add(history.get((history.size() - 1) - i));
            }catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        Collections.reverse(cache);

        this.history = cache;

    }

    public boolean isStarting() {
        return starting;
    }

    public void setStarting(boolean starting) {
        this.starting = starting;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean isPlayerParticipating(String user, DoubleColor color){
        return doubleBets.stream().anyMatch(w -> w.getOwner().equalsIgnoreCase(user) && w.getColor() == color);
    }

    public String getRedStatusBet(String user) {
        return isPlayerParticipating(user, DoubleColor.RED) ? redStatusBet : redStatusNoBet;
    }

    public String getWhiteStatusBet(String user) {
        return isPlayerParticipating(user, DoubleColor.WHITE) ? whiteStatusBet : whiteStatusNoBet;
    }

    public String getBlackStatusBet(String user) {
        return isPlayerParticipating(user, DoubleColor.BLACK) ? blackStatusBet : blackStatusNoBet;
    }

    public String getRedRoundStatus(){
        return starting ? redRoundStatusStarting.replace("{time}", String.format("%.2f", startingRemainingTime)) : redRoundStatusStarted;
    }

    public String getWhiteRoundStatus(){
        return starting ? whiteRoundStatusStarting.replace("{time}", String.format("%.2f", startingRemainingTime)) : whiteRoundStatusStarted;
    }

    public String getBlackRoundStatus(){
        return starting ? blackRoundStatusStarting.replace("{time}", String.format("%.2f", startingRemainingTime)) : blackRoundStatusStarted;
    }

    public Map<Integer, DoubleColor> getColorCache() {
        return colorCache;
    }

    public double getStartingRemainingTime() {
        return startingRemainingTime;
    }

    public void setStartingRemainingTime(double startingRemainingTime) {
        if(startingRemainingTime < 0) startingRemainingTime = 0;

        this.startingRemainingTime = startingRemainingTime;
    }

    public List<DoubleAnimationSpot> getSetAnimationSpots() {
        return setAnimationSpots;
    }

    public double getStartingTime() {
        return startingTime;
    }

    public double getAnimationTotalTime() {
        return animationTotalTime;
    }

    public double getAnimationRemainingTime() {
        return animationRemainingTime;
    }

    public void setAnimationRemainingTime(double animationRemainingTime) {
        if(animationRemainingTime < 0) animationRemainingTime = 0;

        this.animationRemainingTime = animationRemainingTime;
    }

    public double getAnimationSpeedTime() {
        return animationSpeedTime;
    }

    public void setAnimationSpeedTime(double animationSpeedTime) {
        if(animationSpeedTime < 0) animationSpeedTime = 0;

        this.animationSpeedTime = animationSpeedTime;
    }

    public double getAnimationSpeedRemainingTime() {
        return animationSpeedRemainingTime;
    }

    public void setAnimationSpeedRemainingTime(double animationSpeedRemainingTime) {
        if(animationSpeedRemainingTime < 0) animationSpeedRemainingTime = 0;

        this.animationSpeedRemainingTime = animationSpeedRemainingTime;
    }

    public double getStoppedTime() {
        return stoppedTime;
    }

    public double getStoppedRemainingTime() {
        return stoppedRemainingTime;
    }

    public void setStoppedRemainingTime(double stoppedRemainingTime) {
        if(stoppedRemainingTime < 0) stoppedRemainingTime = 0;

        this.stoppedRemainingTime = stoppedRemainingTime;
    }

    public double getBreakChangeTime() {
        return breakChangeTime;
    }

    public int getStartBreak() {
        return startBreak;
    }

    public int getFinalBreak() {
        return finalBreak;
    }

    public double getMoneyAlert() {
        return moneyAlert;
    }

    public double getCashAlert() {
        return cashAlert;
    }

    public int getNumOfItem() {
        return numOfItem;
    }

    public void setNumOfItem(int numOfItem) {
        this.numOfItem = numOfItem;
    }

    private ItemStack getColorItem(DoubleColor color){
        switch (color){
            case RED: return getRedItem();
            case WHITE: return getWhiteItem();
            default: return getBlackItem();
        }
    }

    public void updateInventories(boolean threeItems, boolean history, boolean gamblers){
        for (Map.Entry<String, Inventory> entry : doubleInventories.entrySet()) {
            Player target = Bukkit.getPlayer(entry.getKey());

            if(target != null) {
                BlazeUser user = getManager().getBlazeUser(entry.getKey());

                if(threeItems) {
                    entry.getValue().setItem(getBetRedSlot(), getBetRedItem(user));
                    entry.getValue().setItem(getBetWhiteSlot(), getBetWhiteItem(user));
                    entry.getValue().setItem(getBetBlackSlot(), getBetBlackItem(user));
                }

                if(history) entry.getValue().setItem(getHistorySlot(), getHistoryItem());
                if(gamblers) entry.getValue().setItem(getGamblersSlot(), getGamblersItem());

                target.updateInventory();
            }
        }
    }

    public void setAnimationItems(){

        DoubleAnimationSpot animationSpot = null;
        for (int i = 0; i < doubleAnimationSpots.size(); i++) {
            if(i == doubleAnimationSpots.size() - 1){
                animationSpot = doubleAnimationSpots.get(i);
                break;
            }

            DoubleAnimationSpot firstRowOfTime = doubleAnimationSpots.get(i);
            DoubleAnimationSpot secondRowOfTime = doubleAnimationSpots.get(i + 1);

            if(startingRemainingTime <= firstRowOfTime.getTime() && startingRemainingTime > secondRowOfTime.getTime()){
                animationSpot = firstRowOfTime;
                break;
            }
        }

        if(animationSpot == null){
            sendDebugMessage("Animation SPOT is null!");
            return;
        }

        if(!setAnimationSpots.contains(animationSpot)) {

            for (Map.Entry<String, Inventory> entry : doubleInventories.entrySet()) {
                for (Map.Entry<ItemStack, List<Integer>> itemEntry : animationSpot.getItems().entrySet()) {
                    itemEntry.getValue().forEach(w -> entry.getValue().setItem(w, itemEntry.getKey()));
                }

                Player player = Bukkit.getPlayer(entry.getKey());

                if(player != null) player.updateInventory();
            }

            setAnimationSpots.add(animationSpot);
        }
    }

    private DoubleColor getRandomColor() {
        double random = rand.nextDouble() * 100;

        if(random <= startRedChance + startWhiteChance + startBlackChance && random > startRedChance + startWhiteChance){ // 100 // 1 + 49.5 = 50.5

            return DoubleColor.BLACK;
        }else if(random <= startRedChance + startWhiteChance && random > startRedChance) { // 1 + 49.5 = 50.5   // 49.5

            return DoubleColor.WHITE;
        }else{ // less than 49.5
            return DoubleColor.RED;
        }
    }

    public DoubleColor getRandomDefinitiveColor(){
        double random = rand.nextDouble() * 100;

        if(random <= redChance + whiteChance + blackChance && random > redChance + whiteChance){ // 100 // 1 + 49.5 = 50.5

            return DoubleColor.BLACK;
        }else if(random <= redChance + whiteChance && random > redChance) { // 1 + 49.5 = 50.5   // 49.5

            return DoubleColor.WHITE;
        }else{ // less than 49.5

            return DoubleColor.RED;
        }
    }

    public boolean manageAnimation(){

        if (getAnimationSpeedRemainingTime() == 0 || getAnimationRemainingTime() == 0) {
            setAnimationSpeedRemainingTime(getAnimationSpeedTime());

            DoubleColor chosenColor = getRandomColor();

            if(getNumOfItem() == getAmountOfRemainingUpdates() - 5){
                chosenColor = roundChosenColor;
            }

            Map<Integer, DoubleColor> cache = new HashMap<>();
            cache.put(17, chosenColor);

            for (Map.Entry<Integer, DoubleColor> entry : colorCache.entrySet()) {
                if (entry.getKey() == 9) continue;

                cache.put(entry.getKey() - 1, entry.getValue());
            }

            colorCache = cache;

            for (Map.Entry<String, Inventory> entry : doubleInventories.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if(player != null){
                    setColorItems(entry.getValue());

                    player.updateInventory();
                }
            }

            setNumOfItem(getNumOfItem() + 1);

        }else{
            setAnimationSpeedRemainingTime(getAnimationSpeedRemainingTime() - 1);
        }

        if(getAnimationRemainingTime() == 0){
            for (Map.Entry<String, Inventory> entry : doubleInventories.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if(player != null) {
                    for (Map.Entry<ItemStack, List<Integer>> listEntry : finalItems.entrySet()) {
                        for (Integer i : listEntry.getValue()){
                            entry.getValue().setItem(i, listEntry.getKey());
                            player.updateInventory();
                        }
                    }
                }
            }

            if(colorCache.get(13) == DoubleColor.BLACK){

                double valToRemove = startBlackChance / 10;

                blackChance -= valToRemove;

                redChance += (valToRemove / 8) * 7;
                whiteChance += (valToRemove / 8) * 1;

            }else if(colorCache.get(13) == DoubleColor.WHITE){

                redChance = startRedChance;
                whiteChance = startWhiteChance;
                blackChance = startBlackChance;

            }else{

                double valToRemove = startRedChance / 10;

                redChance -= valToRemove;

                blackChance += (valToRemove / 8) * 7;
                whiteChance += (valToRemove / 8) * 1;

            }

            sendDebugMessage("[Double] Red: " + String.format("%.2f", redChance));
            sendDebugMessage("[Double] White: " + String.format("%.2f", whiteChance));
            sendDebugMessage("[Double] Black: " + String.format("%.2f", blackChance));

            setNumOfItem(0);
            return true;
        }

        setAnimationRemainingTime(getAnimationRemainingTime() - 0.05);
        return false;
    }

    public void setColorItems(Inventory inv){
        for (Map.Entry<Integer, DoubleColor> entry : colorCache.entrySet()) {
            inv.setItem(entry.getKey(), getColorItem(entry.getValue()));
        }
    }

    private int getAmountOfRemainingUpdates(){
        double d = animationTotalTime;
        double cache = startBreak;

        double animationSpeedTime = startBreak;

        Map<Integer, Double> map = new HashMap<>();

        for(double i = 0; i < animationTotalTime * 20; i++) {

            if(cache <= 0 || d <= 0){
                cache = animationSpeedTime;

                map.put(map.size(), d);

            }else cache -= 1;

            if(animationTotalTime - breakChangeTime >= d)
                animationSpeedTime = animationSpeedTime + ((getDoubleManager().getFinalBreak() - getDoubleManager().getStartBreak()) / (getDoubleManager().getBreakChangeTime() * 20));

            d = d - 0.05;
        }

        return map.size();
    }

}
