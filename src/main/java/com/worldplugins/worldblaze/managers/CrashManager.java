package com.worldplugins.worldblaze.managers;

import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.objects.CrashBet;
import com.worldplugins.worldblaze.objects.CrashChance;
import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.SkullCreator;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class CrashManager {

    // VARIÁVEIS QUE NÃO VÃO MUDAR EM HIPÓTESE ALGUMA
    private final Set<String> gamblersGuiOpen;
    private final LinkedHashMap<String, CrashBet> crashBets;
    private final Map<String, Inventory> crashInventories;
    private final String menuTitle;
    private final double secondsBeforeStart, timeToChangeCrashedState, timeStartCrash, sumStart, sumFinal;
    private final int maxHistoricValues;
    private final Random rand;

    // VARIÁVEIS QUE VAO MUDAR CONFORME O CRASH VAI ROLANDO
    private boolean crashed, starting, high, crashInZero;
    private double totalBetCash, totalBetMoney, crashValue, timeToSubtract, duration, crashZeroChance, whenWillCrash;
    private int gamesInHigh, gamesInLow;
    private String lastPlayer;
    private List<Double> historic;

    // CONFIGURAÇÃO DOS ITENS DO MENU
    private final ItemBuilder moneyItem, cashItem, crashStartingItem, crashStartedItem, crashCrashedItem, historicItem, gamblersItem;
    private final List<String> moneyLore, cashLore, crashStartingLore, crashStartedLore, crashCrashedLore, historicLore, gamblersLore;
    private final String moneyName, cashName, crashStartingName, crashStartedName, crashCrashedName, historicName, gamblersName;
    private final String statusParticipatingStarting, statusParticipateStarting, statusNoParticipatedStarted, statusParticipatingStarted, statusParticipateStarted, statusNoParticipatedCrashed, statusWonCrashed, statusLoseCrashed;
    private final boolean gamblersUsePlayersHead;
    private final int moneySlot, cashSlot, crashStartingSlot, crashStartedSlot, crashCrashedSlot, historicSlot, gamblersSlot;
    private final Map<Integer, ItemStack> startingGlass, startedGlass, crashedGlass;

    private final List<CrashChance> crashChances;

    public CrashManager(){

        FileConfiguration getConfig = getCrashGuiYml().getConfig();

        this.crashChances = new ArrayList<>();

        for (String s : getInstance().getConfig().getStringList("config.crash.values")) {
            String[] split = s.split(";");

            CrashChance crashChance = new CrashChance(Double.parseDouble(split[2]), Double.parseDouble(split[0]), Double.parseDouble(split[1]));

            crashChances.add(crashChance);
        }

        this.menuTitle = c(getConfig.getString("Titulo"));

        this.starting = true;

        this.crashBets = new LinkedHashMap<>();
        this.crashInventories = new HashMap<>();

        this.historic = new ArrayList<>();

        this.startingGlass = new HashMap<>();
        this.startedGlass = new HashMap<>();
        this.crashedGlass = new HashMap<>();

        this.rand = new Random();

        this.secondsBeforeStart = getInstance().getConfig().getDouble("config.crash.starting-time");
        this.timeToChangeCrashedState = getInstance().getConfig().getDouble("config.crash.crashed-time");
        this.timeStartCrash = getInstance().getConfig().getDouble("config.crash.crash-start-time");

        this.sumStart = getInstance().getConfig().getDouble("config.crash.crash-tick-sum-initial");
        this.sumFinal = getInstance().getConfig().getDouble("config.crash.crash-tick-sum-final");

        this.maxHistoricValues = getInstance().getConfig().getInt("config.crash.max-history-values");

        this.crashValue = 1.0;

        this.gamblersGuiOpen = new HashSet<>();

        for (Integer integer : getConfig.getIntegerList("Slots-vidros")) {
            ItemBuilder itemStarting = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) getConfig.getInt("Cor-iniciando"));
            ItemBuilder itemStarted = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) getConfig.getInt("Cor-iniciado"));
            ItemBuilder itemCrashed = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) getConfig.getInt("Cor-crash"));

            startingGlass.put(integer, itemStarting.toItemStack().clone());
            startedGlass.put(integer, itemStarted.toItemStack().clone());
            crashedGlass.put(integer, itemCrashed.toItemStack().clone());
        }

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

        if (getConfig.getBoolean("Itens.crash-iniciando.Usar-head")) crashStartingItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.crash-iniciando.Textura")));
        else crashStartingItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.crash-iniciando.Id")), 1, (byte) getConfig.getInt("Itens.crash-iniciando.Data"));
        crashStartingItem.setGlow(getConfig.getBoolean("Itens.crash-iniciando.Glow"));

        this.crashStartingLore = getConfig.getStringList("Itens.crash-iniciando.Lore");
        this.crashStartingName = c(getConfig.getString("Itens.crash-iniciando.Nome"));

        this.crashStartingSlot = getConfig.getInt("Itens.crash-iniciando.Slot");

        this.statusParticipatingStarting = c(getConfig.getString("Itens.crash-iniciando.Status-participando"));
        this.statusParticipateStarting = c(getConfig.getString("Itens.crash-iniciando.Status-participar"));

        if (getConfig.getBoolean("Itens.crash-iniciado.Usar-head")) crashStartedItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.crash-iniciado.Textura")));
        else crashStartedItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.crash-iniciado.Id")), 1, (byte) getConfig.getInt("Itens.crash-iniciado.Data"));
        crashStartedItem.setGlow(getConfig.getBoolean("Itens.crash-iniciado.Glow"));

        this.crashStartedLore = getConfig.getStringList("Itens.crash-iniciado.Lore");
        this.crashStartedName = c(getConfig.getString("Itens.crash-iniciado.Nome"));

        this.crashStartedSlot = getConfig.getInt("Itens.crash-iniciado.Slot");

        this.statusNoParticipatedStarted = c(getConfig.getString("Itens.crash-iniciado.Status-nao-participou"));
        this.statusParticipatingStarted = c(getConfig.getString("Itens.crash-iniciado.Status-participando"));
        this.statusParticipateStarted = c(getConfig.getString("Itens.crash-iniciado.Status-saiu"));

        if (getConfig.getBoolean("Itens.crash-crashou.Usar-head")) crashCrashedItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.crash-crashou.Textura")));
        else crashCrashedItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.crash-crashou.Id")), 1, (byte) getConfig.getInt("Itens.crash-crashou.Data"));
        crashCrashedItem.setGlow(getConfig.getBoolean("Itens.crash-crashou.Glow"));

        this.crashCrashedLore = getConfig.getStringList("Itens.crash-crashou.Lore");
        this.crashCrashedName = c(getConfig.getString("Itens.crash-crashou.Nome"));

        this.crashCrashedSlot = getConfig.getInt("Itens.crash-crashou.Slot");

        this.statusNoParticipatedCrashed = c(getConfig.getString("Itens.crash-crashou.Status-nao-participou"));
        this.statusWonCrashed = c(getConfig.getString("Itens.crash-crashou.Status-ganhou"));
        this.statusLoseCrashed = c(getConfig.getString("Itens.crash-crashou.Status-perdeu"));

        if (getConfig.getBoolean("Itens.historico.Usar-head")) historicItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.historico.Textura")));
        else historicItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.historico.Id")), 1, (byte) getConfig.getInt("Itens.historico.Data"));
        historicItem.setGlow(getConfig.getBoolean("Itens.historico.Glow"));

        this.historicLore = getConfig.getStringList("Itens.historico.Lore");
        this.historicName = c(getConfig.getString("Itens.historico.Nome"));

        this.historicSlot = getConfig.getInt("Itens.historico.Slot");

        if (getConfig.getBoolean("Itens.apostadores.Usar-head-ultimo-player")) gamblersItem = new ItemBuilder(SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654"));
        else if (getConfig.getBoolean("Itens.apostadores.Usar-head")) gamblersItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.apostadores.Textura")));
        else gamblersItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.apostadores.Id")), 1, (byte) getConfig.getInt("Itens.apostadores.Data"));
        gamblersItem.setGlow(getConfig.getBoolean("Itens.apostadores.Glow"));

        this.gamblersLore = getConfig.getStringList("Itens.apostadores.Lore");
        this.gamblersName = c(getConfig.getString("Itens.apostadores.Nome"));

        this.gamblersSlot = getConfig.getInt("Itens.apostadores.Slot");

        this.gamblersUsePlayersHead = getConfig.getBoolean("Itens.apostadores.Usar-head-ultimo-player");

    }

    public boolean crash(){

        boolean b = false;
        if (crashValue >= whenWillCrash) {
            crashValue = whenWillCrash;
            b = true;
        }

        return b;

        /*if(crashInZero){
            if(crashValue >= whenWillCrash){
                crashInZero = false;
                whenWillCrash = 0;
                return true;
            }else return false;
        }

        if(duration <= 0) {
            boolean zero = rand.nextInt(100) <= crashZeroChance;

            if (zero){
                crashInZero = true;
                crashZeroChance = 5;
                return true;
            }
        }

        if(high) return rand.nextDouble() * 100 <= 0.5;
        else return rand.nextDouble() * 100 <= 1.75;*/
    }

    public void updateVariables(){

        whenWillCrash = 0;

        Collections.shuffle(crashChances);

        Double crashVar = null;
        while (crashVar == null){
            CrashChance crashChance = crashChances.get(0);

            if (rand.nextDouble() * 100 <= crashChance.getChance()) {

                if(crashChance.getCrashStart() == crashChance.getCrashFinal()){
                    crashVar = crashChance.getCrashStart();
                }else {
                    crashVar = ThreadLocalRandom.current().nextDouble(crashChance.getCrashStart(), crashChance.getCrashFinal());
                }
                whenWillCrash = crashVar;
                sendDebugMessage("[Crash] Próximo crash vai ser em: " + String.format("%.2f", whenWillCrash));
            }
        }

        /*if(crashInZero){
            if (rand.nextInt(100) <= 40) {
                whenWillCrash = 6 + (rand.nextDouble() * 2);
                sendDebugMessage("[Crash] Crashou em zero e agora vai subir ate: " + String.format("%.2f", whenWillCrash));
                return;
            }else{
                crashInZero = false;
                whenWillCrash = 0;
            }
        }

        if(high){
            if(rand.nextInt(100) <= gamesInHigh * 2) {
                sendDebugMessage("[Crash] Crash agr ta em baixa");
                high = false;
                gamesInHigh = 0;
            }else gamesInHigh++;
        }else{
            if(rand.nextInt(100) <= gamesInLow * 1.5) {
                sendDebugMessage("[Crash] Crash agr ta em alta");
                high = true;
                gamesInLow = 0;
            }else gamesInLow++;
        }

        crashZeroChance += 5;*/
    }

    public ItemStack getMoneyItem(BlazeUser user) {
        ItemBuilder clone = moneyItem.clone();

        clone.setName(moneyName);

        moneyLore.forEach(w -> clone.addLoreLine(c(w.replace("{money}", letterFormatter(user.getCrashMoneyValue())))));

        return clone.toItemStack();
    }

    public ItemStack getCashItem(BlazeUser user) {
        ItemBuilder clone = cashItem.clone();

        clone.setName(cashName);

        cashLore.forEach(w -> clone.addLoreLine(c(w.replace("{cash}", letterFormatter(user.getCrashCashValue())))));

        return clone.toItemStack();
    }

    public ItemStack getCrashStartingItem(BlazeUser user) {
        ItemBuilder clone = crashStartingItem.clone();

        clone.setName(crashStartingName);

        for (String s : crashStartingLore) {
            if(s.contains("{status-iniciando}")){
                if(crashBets.containsKey(user.getOwner())) clone.addLoreLine(c(statusParticipatingStarting));
                else clone.addLoreLine(c(statusParticipateStarting));
            }else clone.addLoreLine(c(s.replace("{tempo}", String.format("%.2f",getCrashManager().getSecondsBeforeStart() - getCrashManager().getTimeToSubtract())).
                    replace("{money}", letterFormatter(user.getCrashMoneyValue())).
                    replace("{cash}", letterFormatter(user.getCrashCashValue()))));
        }

        return clone.toItemStack();
    }

    public ItemStack getCrashStartedItem(BlazeUser user) {
        ItemBuilder clone = crashStartedItem.clone();

        clone.setName(crashStartedName.replace("{valor}", String.format("%.2f", crashValue)));

        double betMoney = 0;
        double betCash = 0;
        if(crashBets.containsKey(user.getOwner())){
            betMoney = crashBets.get(user.getOwner()).getMoneyValue();
            betCash = crashBets.get(user.getOwner()).getCashValue();
        }

        for (String s : crashStartedLore) {
            if(s.contains("{status-iniciado}")){
                if(!crashBets.containsKey(user.getOwner())) clone.addLoreLine(statusNoParticipatedStarted);
                else if (isPlayerLeftCrash(user.getOwner())) clone.addLoreLine(c(statusParticipateStarted.
                        replace("{valor-saida}", String.format("%.2f", getPlayerLeftValue(user.getOwner())))));
                else clone.addLoreLine(statusParticipatingStarted);
            }else clone.addLoreLine(c(s.replace("{valor}", String.format("%.2f", crashValue)).
                    replace("{money-apostado}", letterFormatter(betMoney)).
                    replace("{cash-apostado}", letterFormatter(betCash))));
        }

        return clone.toItemStack();
    }

    public ItemStack getCrashCrashedItem(BlazeUser user) {
        ItemBuilder clone = crashCrashedItem.clone();

        clone.setName(crashCrashedName.replace("{valor}", String.format("%.2f", crashValue)));

        double betMoney = 0;
        double betCash = 0;
        if(crashBets.containsKey(user.getOwner())){
            betMoney = crashBets.get(user.getOwner()).getMoneyValue();
            betCash = crashBets.get(user.getOwner()).getCashValue();
        }

        for (String s : crashCrashedLore) {
            if(s.contains("{status-crashou}")){
                if(!crashBets.containsKey(user.getOwner())) clone.addLoreLine(statusNoParticipatedCrashed);
                else if (isPlayerLeftCrash(user.getOwner())) clone.addLoreLine(c(statusWonCrashed.
                        replace("{valor-saida}", String.format("%.2f", getPlayerLeftValue(user.getOwner())))));
                else clone.addLoreLine(statusLoseCrashed);
            }else clone.addLoreLine(c(s.replace("{valor}", String.format("%.2f", crashValue)).
                    replace("{ganho-money}", letterFormatter(getMoneyWon(user.getOwner()))).
                    replace("{ganho-cash}", letterFormatter(getCashWon(user.getOwner()))).
                    replace("{money-apostado}", letterFormatter(betMoney)).
                    replace("{cash-apostado}", letterFormatter(betCash))));
        }

        return clone.toItemStack();
    }

    public ItemStack getHistoricItem() {
        ItemBuilder clone = historicItem.clone();

        clone.setName(historicName);

        List<Double> copy = new ArrayList<>(historic);
        Collections.reverse(copy);

        for (String s : historicLore) {
            if(s.contains("{historico}")){
                for (Double d : copy) clone.addLoreLine(c(s.replace("{historico}", String.format("%.2f",d))));
            }else clone.addLoreLine(c(s));
        }

        return clone.toItemStack();
    }

    public ItemStack getGamblersItem() {
        ItemBuilder clone;
        if(!gamblersUsePlayersHead || crashBets.size() == 0) clone = gamblersItem.clone();
        else clone = new ItemBuilder(SkullCreator.itemFromName(getLastPlayer()));

        clone.setName(gamblersName);

        gamblersLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{jogadores}", crashBets.size()+"").
                replace("{last_player}", getLastPlayer()).
                replace("{total_money}", letterFormatter(totalBetMoney)).
                replace("{total_cash}", letterFormatter(totalBetCash)))));

        return clone.toItemStack();
    }

    public String getStatusParticipatingStarting() {
        return statusParticipatingStarting;
    }

    public String getStatusParticipateStarting() {
        return statusParticipateStarting;
    }

    public String getStatusNoParticipatedStarted() {
        return statusNoParticipatedStarted;
    }

    public String getStatusParticipatingStarted() {
        return statusParticipatingStarted;
    }

    public String getStatusParticipateStarted() {
        return statusParticipateStarted;
    }

    public String getStatusNoParticipatedCrashed() {
        return statusNoParticipatedCrashed;
    }

    public String getStatusWonCrashed() {
        return statusWonCrashed;
    }

    public String getStatusLoseCrashed() {
        return statusLoseCrashed;
    }

    public boolean isGamblersUsePlayersHead() {
        return gamblersUsePlayersHead;
    }

    public int getMoneySlot() {
        return moneySlot;
    }

    public int getCashSlot() {
        return cashSlot;
    }

    public int getCrashStartingSlot() {
        return crashStartingSlot;
    }

    public int getCrashStartedSlot() {
        return crashStartedSlot;
    }

    public int getCrashCrashedSlot() {
        return crashCrashedSlot;
    }

    public int getHistoricSlot() {
        return historicSlot;
    }

    public int getGamblersSlot() {
        return gamblersSlot;
    }

    public Map<Integer, ItemStack> getStartingGlass() {
        return startingGlass;
    }

    public Map<Integer, ItemStack> getStartedGlass() {
        return startedGlass;
    }

    public Map<Integer, ItemStack> getCrashedGlass() {
        return crashedGlass;
    }

    public List<Double> getHistoric() {
        return historic;
    }

    public double getCrashValue() {
        return crashValue;
    }

    public void setCrashValue(double crashValue) {
        if(crashValue > whenWillCrash) crashValue = whenWillCrash;
        this.crashValue = crashValue;
    }

    public boolean isCrashed() {
        return crashed;
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    public LinkedHashMap<String, CrashBet> getCrashBets() {
        return crashBets;
    }

    public Map<String, Inventory> getCrashInventories() {
        return crashInventories;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public void setTotalBetMoney(){
        final double[] var = {0};

        crashBets.forEach((k, w) -> var[0] += w.getMoneyValue());

        totalBetMoney = var[0];
    }

    public void setTotalBetCash(){
        final double[] var = {0};

        crashBets.forEach((k, w) -> var[0] += w.getCashValue());

        totalBetCash = var[0];
    }

    public double getTotalBetCash() {
        return totalBetCash;
    }

    public double getTotalBetMoney() {
        return totalBetMoney;
    }

    public String getLastPlayer(){
        if(crashBets.size() == 0) return "-/-";

        return lastPlayer;
    }

    public void setLastPlayer(String lastPlayer) {
        this.lastPlayer = lastPlayer;
    }

    public boolean isStarting() {
        return starting;
    }

    public void setStarting(boolean starting) {
        this.starting = starting;
    }

    public boolean isPlayerParticipating(String player){
        return crashBets.containsKey(player);
    }

    public boolean isPlayerLeftCrash(String player){
        if(!crashBets.containsKey(player)) return false;

        return crashBets.get(player).isLeft();
    }

    public double getPlayerLeftValue(String player){
        if(!crashBets.containsKey(player)) return 0;

        return crashBets.get(player).getLeftValue();
    }

    public double getPlayerMoneyBet(String player){

        if(crashBets.size() == 0) return 0;

        if(crashBets.containsKey(player)) return 0;

        return crashBets.get(player).getMoneyValue();
    }

    public double getPlayerCashBet(String player){

        if(crashBets.size() == 0) return 0;

        if(crashBets.containsKey(player)) return 0;

        return crashBets.get(player).getCashValue();
    }

    public double getSecondsBeforeStart() {
        return secondsBeforeStart;
    }

    public double getTimeToSubtract() {
        return timeToSubtract;
    }

    public void setTimeToSubtract(double timeToSubtract) {
        this.timeToSubtract = timeToSubtract;
    }

    public double getTimeToChangeCrashedState() {
        return timeToChangeCrashedState;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public Set<String> getGamblersGuiOpen() {
        return gamblersGuiOpen;
    }

    public double getSumRoundValue(){
        if(duration > timeStartCrash) return sumFinal;

        return sumStart;
    }

    public void addHistoric(double value){
        historic.add(value);

        List<Double> cache = new ArrayList<>();

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

    public double getMoneyWon(String player){
        if(!crashBets.containsKey(player)) return 0;

        CrashBet crashBet = crashBets.get(player);

        if(!crashBet.isLeft()) return 0;

        return crashBet.getMoneyValue() * crashBet.getLeftValue();
    }

    public double getCashWon(String player){
        if(!crashBets.containsKey(player)) return 0;

        CrashBet crashBet = crashBets.get(player);

        if(!crashBet.isLeft()) return 0;

        return crashBet.getCashValue() * crashBet.getLeftValue();
    }
}

