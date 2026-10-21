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

    // VARIAVEIS QUE NÃO VÃO MUDAR EM HIPÓTESE ALGUMA
    private final Set<String> gamblersGuiOpen;
    private final List<DoubleBet> doubleBets;
    private List<DoubleAnimationSpot> doubleAnimationSpots;
    private final Map<String, Inventory> doubleInventories;
    private final Map<ItemStack, List<Integer>> finalItens;
    private final String menuTitle;
    private final double dopMoney, dopCash, dopChance, startingTime, stoppedTime, animationTotalTime, breakChangeTime, startRedChance, startWhiteChance, startBlackChance, moneyAlert, cashAlert;
    private final int maxHistoricValues, startBreak, finalBreak;
    private final boolean dop;
    private final Random rand;

    // VARIAVEIS QUE VÃO MUDAR CONFORME O ANDAMENTO DO DOUBLE
    private boolean starting, stopped;
    private double totalBetCash, totalBetMoney, redAverage, whiteAverage, blackAverage, startingRemainingTime, stoppedRemainingTime, animationRemainingTime, animationSpeedRemainingTime, animationSpeedTime, whiteChance, redChance, blackChance;
    private int numOfItem;
    private DoubleColor roundChosenColor;
    private final List<DoubleAnimationSpot> setAnimationSpots;
    private List<DoubleColor> historic;
    private Map<Integer, DoubleColor> colorCache;

    // VARIAVEIS QUE NÃO VÃO MUDAR EM HIPOTESE ALGUMA
    private final ItemBuilder redItem, whiteItem, blackItem, moneyItem, cashItem, betRedItem, betWhiteItem, betBlackItem, historicItem, gamblersItem;
    private final List<String> moneyLore, cashLore, betRedLore, betWhiteLore, betBlackLore, historicLore, gamblersLore;
    private final String redStatusBet, redStatusNoBet, redRoundStatusStarting, redRoundStatusStarted, whiteStatusBet, whiteStatusNoBet, whiteRoundStatusStarting, whiteRoundStatusStarted, blackStatusBet, blackStatusNoBet, blackRoundStatusStarting, blackRoundStatusStarted, moneyName, cashName, betRedName, betWhiteName, betBlackName, historicName, gamblersName, redDisplay, whiteDisplay, blackDisplay;
    private final boolean gamblersUsePlayersHead;
    private final int moneySlot, cashSlot, betRedSlot, betWhiteSlot, betBlackSlot, historicSlot, gamblersSlot;

    public DoubleManager() {

        FileConfiguration getConfig = getDoubleGuiYml().getConfig();

        this.menuTitle = c(getConfig.getString("Titulo"));

        this.gamblersGuiOpen = new HashSet<>();
        this.setAnimationSpots = new ArrayList<>();

        this.doubleInventories = new HashMap<>();
        this.colorCache = new HashMap<>();

        this.doubleAnimationSpots = new ArrayList<>();
        this.doubleBets = new ArrayList<>();
        this.historic = new ArrayList<>();

        this.maxHistoricValues = getInstance().getConfig().getInt("config.double.max-history-values");

        this.rand = new Random();

        this.startingTime = getInstance().getConfig().getDouble("config.double.starting-time");
        this.stoppedTime = getInstance().getConfig().getDouble("config.double.finished-time");

        this.startingRemainingTime = startingTime;
        this.stoppedRemainingTime = stoppedTime;

        this.starting = true;

        this.breakChangeTime = getInstance().getConfig().getDouble("config.double.interval-change-time");
        this.startBreak = getInstance().getConfig().getInt("config.double.initial-interval");
        this.finalBreak = getInstance().getConfig().getInt("config.double.final-interval");

        this.dop = getInstance().getConfig().getBoolean("config.double.rigging.enabled");
        this.dopMoney = getInstance().getConfig().getDouble("config.double.rigging.money");
        this.dopCash = getInstance().getConfig().getDouble("config.double.rigging.cash");
        this.dopChance = getInstance().getConfig().getDouble("config.double.rigging.chance");

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

        for (String s : getConfig.getConfigurationSection("Starting-animation").getKeys(false)) doubleAnimationSpots.add(new DoubleAnimationSpot(s));
        doubleAnimationSpots = doubleAnimationSpots.stream().sorted(Comparator.comparingDouble(DoubleAnimationSpot::getTime).reversed()).collect(Collectors.toList());

        this.finalItens = new HashMap<>();
        for (String s : getConfig.getConfigurationSection("Vidros-final").getKeys(false)) {
            finalItens.put(new ItemStack(Material.getMaterial(getConfig.getString("Vidros-final."+s+".Id")),
                    1, (short) getConfig.getInt("Vidros-final."+s+".Data")), getConfig.getIntegerList("Vidros-final."+s+".Slot"));
        }

        if (getConfig.getBoolean("vermelho.Usar-head")) redItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("vermelho.Textura")));
        else redItem = new ItemBuilder(Material.getMaterial(getConfig.getString("vermelho.Id")), 1, (byte) getConfig.getInt("vermelho.Data"));
        redItem.setGlow(getConfig.getBoolean("vermelho.Glow")).setName(c(getConfig.getString("vermelho.Nome")));

        getConfig.getStringList("vermelho.Lore").forEach(w -> redItem.addLoreLine(c(w)));

        if (getConfig.getBoolean("branco.Usar-head")) whiteItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("branco.Textura")));
        else whiteItem = new ItemBuilder(Material.getMaterial(getConfig.getString("branco.Id")), 1, (byte) getConfig.getInt("branco.Data"));
        whiteItem.setGlow(getConfig.getBoolean("branco.Glow")).setName(c(getConfig.getString("branco.Nome")));

        getConfig.getStringList("branco.Lore").forEach(w -> whiteItem.addLoreLine(c(w)));

        if (getConfig.getBoolean("preto.Usar-head")) blackItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("preto.Textura")));
        else blackItem = new ItemBuilder(Material.getMaterial(getConfig.getString("preto.Id")), 1, (byte) getConfig.getInt("preto.Data"));
        blackItem.setGlow(getConfig.getBoolean("preto.Glow")).setName(c(getConfig.getString("preto.Nome")));

        getConfig.getStringList("preto.Lore").forEach(w -> blackItem.addLoreLine(c(w)));

        if (getConfig.getBoolean("Itens.money.Usar-head")) moneyItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.money.Textura")));
        else moneyItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.money.Id")), 1, (byte) getConfig.getInt("Itens.money.Data"));
        moneyItem.setGlow(getConfig.getBoolean("Itens.money.Glow"));

        this.moneyLore = getConfig.getStringList("Itens.money.Lore");
        this.moneyName = c(getConfig.getString("Itens.money.Nome"));

        this.moneySlot = getConfig.getInt("Itens.money.Slot");

        if (getConfig.getBoolean("Itens.cash.Usar-head")) cashItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.cash.Textura")));
        else cashItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.cash.Id")), 1, (byte) getConfig.getInt("Itens.cash.Data"));
        cashItem.setGlow(getConfig.getBoolean("Itens.cash.Glow"));

        this.cashLore = getConfig.getStringList("Itens.cash.Lore");
        this.cashName = c(getConfig.getString("Itens.cash.Nome"));

        this.cashSlot = getConfig.getInt("Itens.cash.Slot");

        if (getConfig.getBoolean("Itens.apostar-vermelho.Usar-head")) betRedItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.apostar-vermelho.Textura")));
        else betRedItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.apostar-vermelho.Id")), 1, (byte) getConfig.getInt("Itens.apostar-vermelho.Data"));
        betRedItem.setGlow(getConfig.getBoolean("Itens.apostar-vermelho.Glow"));

        this.betRedLore = getConfig.getStringList("Itens.apostar-vermelho.Lore");
        this.betRedName = c(getConfig.getString("Itens.apostar-vermelho.Nome"));

        this.betRedSlot = getConfig.getInt("Itens.apostar-vermelho.Slot");

        this.redStatusBet = c(getConfig.getString("Itens.apostar-vermelho.Status-apostou"));
        this.redStatusNoBet = c(getConfig.getString("Itens.apostar-vermelho.Status-naoapostou"));

        this.redRoundStatusStarted = c(getConfig.getString("Itens.apostar-vermelho.RoundStatus-iniciou"));
        this.redRoundStatusStarting = c(getConfig.getString("Itens.apostar-vermelho.RoundStatus-naoiniciou"));

        if (getConfig.getBoolean("Itens.apostar-branco.Usar-head")) betWhiteItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.apostar-branco.Textura")));
        else betWhiteItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.apostar-branco.Id")), 1, (byte) getConfig.getInt("Itens.apostar-branco.Data"));
        betWhiteItem.setGlow(getConfig.getBoolean("Itens.apostar-branco.Glow"));

        this.betWhiteLore = getConfig.getStringList("Itens.apostar-branco.Lore");
        this.betWhiteName = c(getConfig.getString("Itens.apostar-branco.Nome"));

        this.betWhiteSlot = getConfig.getInt("Itens.apostar-branco.Slot");

        this.whiteStatusBet = c(getConfig.getString("Itens.apostar-branco.Status-apostou"));
        this.whiteStatusNoBet = c(getConfig.getString("Itens.apostar-branco.Status-naoapostou"));

        this.whiteRoundStatusStarted = c(getConfig.getString("Itens.apostar-branco.RoundStatus-iniciou"));
        this.whiteRoundStatusStarting = c(getConfig.getString("Itens.apostar-branco.RoundStatus-naoiniciou"));

        if (getConfig.getBoolean("Itens.apostar-preto.Usar-head")) betBlackItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.apostar-preto.Textura")));
        else betBlackItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.apostar-preto.Id")), 1, (byte) getConfig.getInt("Itens.apostar-preto.Data"));
        betBlackItem.setGlow(getConfig.getBoolean("Itens.apostar-preto.Glow"));

        this.betBlackLore = getConfig.getStringList("Itens.apostar-preto.Lore");
        this.betBlackName = c(getConfig.getString("Itens.apostar-preto.Nome"));

        this.betBlackSlot = getConfig.getInt("Itens.apostar-preto.Slot");

        this.blackStatusBet = c(getConfig.getString("Itens.apostar-preto.Status-apostou"));
        this.blackStatusNoBet = c(getConfig.getString("Itens.apostar-preto.Status-naoapostou"));

        this.blackRoundStatusStarted = c(getConfig.getString("Itens.apostar-preto.RoundStatus-iniciou"));
        this.blackRoundStatusStarting = c(getConfig.getString("Itens.apostar-preto.RoundStatus-naoiniciou"));

        if (getConfig.getBoolean("Itens.historico.Usar-head")) historicItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.historico.Textura")));
        else historicItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.historico.Id")), 1, (byte) getConfig.getInt("Itens.historico.Data"));
        historicItem.setGlow(getConfig.getBoolean("Itens.historico.Glow"));

        this.historicLore = getConfig.getStringList("Itens.historico.Lore");
        this.historicName = c(getConfig.getString("Itens.historico.Nome"));

        this.historicSlot = getConfig.getInt("Itens.historico.Slot");

        this.redDisplay = c(getConfig.getString("Itens.historico.Display-vermelho"));
        this.whiteDisplay = c(getConfig.getString("Itens.historico.Display-branco"));
        this.blackDisplay = c(getConfig.getString("Itens.historico.Display-preto"));

        if (getConfig.getBoolean("Itens.apostadores.Usar-head-ultimo-player")) gamblersItem = new ItemBuilder(SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654"));
        else if (getConfig.getBoolean("Itens.apostadores.Usar-head")) gamblersItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.apostadores.Textura")));
        else gamblersItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.apostadores.Id")), 1, (byte) getConfig.getInt("Itens.apostadores.Data"));
        gamblersItem.setGlow(getConfig.getBoolean("Itens.apostadores.Glow"));

        this.gamblersLore = getConfig.getStringList("Itens.apostadores.Lore");
        this.gamblersName = c(getConfig.getString("Itens.apostadores.Nome"));

        this.gamblersSlot = getConfig.getInt("Itens.apostadores.Slot");

        this.gamblersUsePlayersHead = getConfig.getBoolean("Itens.apostadores.Usar-head-ultimo-player");
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
                replace("{red_average}", String.format("%.2f", redAverage)).
                replace("{money}", letterFormatter(user.getDoubleMoneyValue())).
                replace("{cash}", letterFormatter(user.getDoubleCashValue())).
                replace("{status}", getRedStatusBet(user.getOwner())).
                replace("{round_status}",getRedRoundStatus()))));

        return clone.toItemStack();
    }

    public ItemStack getBetWhiteItem(BlazeUser user) {
        ItemBuilder clone = betWhiteItem.clone();

        clone.setName(betWhiteName);

        betWhiteLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{white_average}", String.format("%.2f", whiteAverage)).
                replace("{money}", letterFormatter(user.getDoubleMoneyValue())).
                replace("{cash}", letterFormatter(user.getDoubleCashValue())).
                replace("{status}", getWhiteStatusBet(user.getOwner())).
                replace("{round_status}",getWhiteRoundStatus()))));

        return clone.toItemStack();
    }

    public ItemStack getBetBlackItem(BlazeUser user) {
        ItemBuilder clone = betBlackItem.clone();

        clone.setName(betBlackName);

        betBlackLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{black_average}", String.format("%.2f", blackAverage)).
                replace("{money}", letterFormatter(user.getDoubleMoneyValue())).
                replace("{cash}", letterFormatter(user.getDoubleCashValue())).
                replace("{status}", getBlackStatusBet(user.getOwner())).
                replace("{round_status}",getBlackRoundStatus()))));

        return clone.toItemStack();
    }

    public ItemStack getHistoricItem() {
        ItemBuilder clone = historicItem.clone();

        clone.setName(historicName);

        List<DoubleColor> copy = new ArrayList<>(historic);
        Collections.reverse(copy);

        for (String s : historicLore) {
            if(s.contains("{historico}")){
                for (DoubleColor doubleColor : copy){
                    if (doubleColor == DoubleColor.RED) clone.addLoreLine(c(s.replace("{historico}", redDisplay)));
                    else if (doubleColor == DoubleColor.WHITE) clone.addLoreLine(c(s.replace("{historico}", whiteDisplay)));
                    else clone.addLoreLine(c(s.replace("{historico}", blackDisplay)));
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

        if(dop && (totalBetCash >= dopCash || totalBetMoney >= dopMoney)){
            if (rand.nextDouble() * 100 <= dopChance) {

                double red;
                double white;
                double black;

                if (totalBetCash > 0) {
                    red = getColorTotalBetCash(DoubleColor.RED);
                    white = getColorTotalBetCash(DoubleColor.WHITE);
                    black = getColorTotalBetCash(DoubleColor.BLACK);
                    sendDebugMessage("[Double] Quebrou (CASH)");
                } else {
                    red = getColorTotalBetMoney(DoubleColor.RED);
                    white = getColorTotalBetMoney(DoubleColor.WHITE);
                    black = getColorTotalBetMoney(DoubleColor.BLACK);
                    sendDebugMessage("[Double] Quebrou (MONEY)");
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

        sendDebugMessage("[Double] A próxima cor vai ser: " + roundChosenColor);

    }

    public ItemStack getGamblersItem() {
        ItemBuilder clone;
        if(!gamblersUsePlayersHead || doubleBets.size() == 0) clone = gamblersItem.clone();
        else clone = new ItemBuilder(SkullCreator.itemFromName(getLastPlayer()));

        clone.setName(gamblersName);

        gamblersLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{jogadores}", doubleBets.size()+"").
                replace("{last_player}", getLastPlayer()).
                replace("{red_average}", String.format("%.2f", redAverage)).
                replace("{white_average}", String.format("%.2f", whiteAverage)).
                replace("{black_average}", String.format("%.2f", blackAverage)).
                replace("{total_money}", letterFormatter(totalBetMoney)).
                replace("{total_cash}", letterFormatter(totalBetCash)))));

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

    public int getHistoricSlot() {
        return historicSlot;
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

    public void addHistoric(DoubleColor color) {

        historic.add(color);

        List<DoubleColor> cache = new ArrayList<>();

        for (int i = 0; i < maxHistoricValues; i++) {
            try{
                cache.add(historic.get((historic.size() - 1) - i));
            }catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        Collections.reverse(cache);

        this.historic = cache;

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
        return starting ? redRoundStatusStarting.replace("{tempo}", String.format("%.2f", startingRemainingTime)) : redRoundStatusStarted;
    }

    public String getWhiteRoundStatus(){
        return starting ? whiteRoundStatusStarting.replace("{tempo}", String.format("%.2f", startingRemainingTime)) : whiteRoundStatusStarted;
    }

    public String getBlackRoundStatus(){
        return starting ? blackRoundStatusStarting.replace("{tempo}", String.format("%.2f", startingRemainingTime)) : blackRoundStatusStarted;
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

    public void updateInventories(boolean threeItens, boolean historic, boolean gamblers){
        for (Map.Entry<String, Inventory> entry : doubleInventories.entrySet()) {
            Player target = Bukkit.getPlayer(entry.getKey());

            if(target != null) {
                BlazeUser user = getManager().getBlazeUser(entry.getKey());

                if(threeItens) {
                    entry.getValue().setItem(getBetRedSlot(), getBetRedItem(user));
                    entry.getValue().setItem(getBetWhiteSlot(), getBetWhiteItem(user));
                    entry.getValue().setItem(getBetBlackSlot(), getBetBlackItem(user));
                }

                if(historic) entry.getValue().setItem(getHistoricSlot(), getHistoricItem());
                if(gamblers) entry.getValue().setItem(getGamblersSlot(), getGamblersItem());

                target.updateInventory();
            }
        }
    }

    public void setAnimationItens(){

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
            sendDebugMessage("Animation SPOT está nulo!");
            return;
        }

        if(!setAnimationSpots.contains(animationSpot)) {

            for (Map.Entry<String, Inventory> entry : doubleInventories.entrySet()) {
                for (Map.Entry<ItemStack, List<Integer>> itemEntry : animationSpot.getItens().entrySet()) {
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
        }else{ // menor que 49.5
            return DoubleColor.RED;
        }
    }

    public DoubleColor getRandomDefinitiveColor(){
        double random = rand.nextDouble() * 100;

        if(random <= redChance + whiteChance + blackChance && random > redChance + whiteChance){ // 100 // 1 + 49.5 = 50.5

            return DoubleColor.BLACK;
        }else if(random <= redChance + whiteChance && random > redChance) { // 1 + 49.5 = 50.5   // 49.5

            return DoubleColor.WHITE;
        }else{ // menor q 49.5

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
                    setColorItens(entry.getValue());

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
                    for (Map.Entry<ItemStack, List<Integer>> listEntry : finalItens.entrySet()) {
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

            sendDebugMessage("[Double] Vermelho: " + redChance);
            sendDebugMessage("[Double] Branco: " + whiteChance);
            sendDebugMessage("[Double] Preto: " + blackChance);

            setNumOfItem(0);
            return true;
        }

        setAnimationRemainingTime(getAnimationRemainingTime() - 0.05);
        return false;
    }

    public void setColorItens(Inventory inv){
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
