package com.worldplugins.worldblaze;

import com.worldplugins.worldblaze.cash.CashProvider;
import com.worldplugins.worldblaze.cash.CashProviders;
import com.worldplugins.worldblaze.commands.*;
import com.worldplugins.worldblaze.listeners.*;
import com.worldplugins.worldblaze.managers.*;
import com.worldplugins.worldblaze.objects.*;
import com.worldplugins.worldblaze.schedulers.MainTask;
import com.worldplugins.worldblaze.utils.FileCreator;
import com.worldplugins.worldblaze.utils.Messages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.worldplugins.worldblaze.utils.Utils.*;

public class WorldBlaze extends JavaPlugin {

    private static WorldBlaze instance;

    private static FileCreator mainGuiYml;
    private static FileCreator graphGuiYml;
    private static FileCreator crashGuiYml;
    private static FileCreator doubleGuiYml;
    private static FileCreator mineGuiYml;
    private static FileCreator animalGameGuiYml;
    private static FileCreator coinFlipGuiYml;
    private static FileCreator mainMineGuiYml;
    private static FileCreator graphGamblersGuiYml;
    private static FileCreator crashGamblersGuiYml;
    private static FileCreator doubleGamblersGuiYml;
    private static FileCreator minePlayersGuiYml;
    private static FileCreator animalGameGamblersGuiYml;
    private static FileCreator coinFlipGamesGuiYml;
    private static FileCreator createMineGuiYml;
    private static FileCreator animalsGuiYml;
    private static FileCreator animalsBetGuiYml;
    private static FileCreator coinFlipMainGuiYml;
    private static FileCreator coinFlipCreateGameGuiYml;
    private static FileCreator dataYml;

    private static UserManager manager;
    private static GraphManager graphManager;
    private static CrashManager crashManager;
    private static DoubleManager doubleManager;
    private static MineManager mineManager;
    private static AnimalGameManager animalGameManager;
    private static CoinFlipManager coinFlipManager;

    private static Messages messages;

    public static Economy econ = null;

    private static CashProvider cashProvider;

    private boolean reloading, locked;
    public static boolean send;
    private Set<String> reloadingMessages;

    @Override
    public void onEnable() {

        instance = this;

        Bukkit.getConsoleSender().sendMessage("[WorldBlaze] Plugin started!");
        setupEconomy();
        registerCommands();
        registerEvents();
        loadConfiguration();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new MainTask(), 200, 1);
        getAnimalGameManager().loadPlayers();
    }

    @Override
    public void onDisable() {

        Bukkit.getScheduler().cancelAllTasks();

        for (GraphBet graphBet : getGraphManager().getGraphBets()) {
            depositValues(Bukkit.getOfflinePlayer(graphBet.getOwner()), graphBet.getCashValue(), graphBet.getMoneyValue());
        }

        for (Map.Entry<String, CrashBet> entry : getCrashManager().getCrashBets().entrySet()) {
            depositValues(Bukkit.getOfflinePlayer(entry.getKey()), entry.getValue().getCashValue(), entry.getValue().getMoneyValue());
        }

        for (DoubleBet doubleBet : getDoubleManager ().getDoubleBets()) {
            depositValues(Bukkit.getOfflinePlayer(doubleBet.getOwner()), doubleBet.getCashValue(), doubleBet.getMoneyValue());
        }

        for (MineBet mineBet : getMineManager().getMineBets()) {
            depositValues(Bukkit.getOfflinePlayer(mineBet.getOwner()), mineBet.getCashValue(), mineBet.getMoneyValue());
        }

        for (CoinFlipBet coinFlipBet : getCoinFlipManager().getCoinFlipBets()) {
            depositValues(Bukkit.getOfflinePlayer(coinFlipBet.getOwner()), coinFlipBet.getCashValue(), coinFlipBet.getMoneyValue());
            if(coinFlipBet.getOpponent() != null){
                depositValues(Bukkit.getOfflinePlayer(coinFlipBet.getOpponent()), coinFlipBet.getCashValue(), coinFlipBet.getMoneyValue());
            }
        }

        getAnimalGameManager().savePlayers();
    }

    private void loadConfiguration(){
        getConfig().options().copyDefaults(false);
        saveDefaultConfig();
        reloadConfig();
        reloadingMessages = new HashSet<>();
        manager = new UserManager();
        dataYml = new FileCreator("data.yml", getDataFolder()+"/");
        mainGuiYml = new FileCreator("main.yml", getDataFolder()+"/menus/");
        graphGuiYml = new FileCreator("graph/graph.yml", getDataFolder()+"/");
        graphGamblersGuiYml = new FileCreator("graph/bettors.yml", getDataFolder()+"/");
        crashGuiYml = new FileCreator("crash/crash.yml", getDataFolder()+"/");
        crashGamblersGuiYml = new FileCreator("crash/bettors.yml", getDataFolder()+"/");
        doubleGuiYml = new FileCreator("double/double.yml", getDataFolder()+"/");
        doubleGamblersGuiYml = new FileCreator("double/bettors.yml", getDataFolder()+"/");
        mainMineGuiYml = new FileCreator("mine/main.yml", getDataFolder()+"/");
        createMineGuiYml = new FileCreator("mine/create-game.yml", getDataFolder()+"/");
        mineGuiYml = new FileCreator("mine/mine.yml", getDataFolder()+"/");
        minePlayersGuiYml = new FileCreator("mine/players.yml", getDataFolder()+"/");
        animalGameGuiYml = new FileCreator("animalgame/main.yml", getDataFolder()+"/");
        animalsGuiYml = new FileCreator("animalgame/animals.yml", getDataFolder()+"/");
        animalGameGamblersGuiYml = new FileCreator("animalgame/bettors.yml", getDataFolder()+"/");
        animalsBetGuiYml = new FileCreator("animalgame/bet.yml", getDataFolder()+"/");
        coinFlipMainGuiYml = new FileCreator("coinflip/main.yml", getDataFolder()+"/");
        coinFlipCreateGameGuiYml = new FileCreator("coinflip/create-game.yml", getDataFolder()+"/");
        coinFlipGamesGuiYml = new FileCreator("coinflip/games.yml", getDataFolder()+"/");
        coinFlipGuiYml = new FileCreator("coinflip/coinflip.yml", getDataFolder()+"/");

        graphManager = new GraphManager();
        crashManager = new CrashManager();
        doubleManager = new DoubleManager();
        mineManager = new MineManager();
        animalGameManager = new AnimalGameManager();
        coinFlipManager = new CoinFlipManager();

        messages = new Messages();

        send = getConfig().getBoolean("config.debug");

        setupCashProvider();
    }

    private void registerEvents(){
        Bukkit.getPluginManager().registerEvents(new InventoryClickMain(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickGraph(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickGraphGamblers(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickCrash(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickCrashGamblers(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickDouble(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickDoubleGamblers(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickMineMain(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickMineCreate(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickMine(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickMinePlayers(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickAnimalGameMain(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickAnimalGameAnimals(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickAnimalGameGamblers(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickAnimalGameBet(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickCoinFlipMain(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickCoinFlipCreateGame(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickCoinFlipGames(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickCoinFlip(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClose(), this);
        Bukkit.getPluginManager().registerEvents(new AsyncPlayerChat(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDisconnect(), this);
    }

    private void registerCommands(){
        getCommand("blaze").setExecutor(new MainCMD());
        getCommand("graph").setExecutor(new GraphCMD());
        getCommand("crash").setExecutor(new CrashCMD());
        getCommand("double").setExecutor(new DoubleCMD());
        getCommand("mine").setExecutor(new MineCMD());
        getCommand("animalgame").setExecutor(new AnimalGameCMD());
        getCommand("coinflip").setExecutor(new CoinFlipCMD());
        getCommand("viewcoinflip").setExecutor(new ViewCoinFlipCMD());
    }

    public void reloadManagers(){
        graphManager = new GraphManager();
        crashManager = new CrashManager();
        doubleManager = new DoubleManager();
        mineManager = new MineManager();
        animalGameManager = new AnimalGameManager();
        coinFlipManager = new CoinFlipManager();

        messages = new Messages();
        formats = getConfig().getStringList("formats").toArray(new String[0]);

        setupCashProvider();
    }

    private void setupCashProvider(){
        cashProvider = CashProviders.detect(getConfig().getString("config.cash-provider"));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static CashProvider getCashProvider() {
        return cashProvider;
    }

    public static FileCreator getDataYml() {
        return dataYml;
    }

    public static WorldBlaze getInstance() {
        return instance;
    }

    public static FileCreator getMainGuiYml() {
        return mainGuiYml;
    }

    public static FileCreator getGraphGuiYml() {
        return graphGuiYml;
    }

    public static UserManager getManager() {
        return manager;
    }

    public static GraphManager getGraphManager() {
        return graphManager;
    }

    public static FileCreator getGraphGamblersGuiYml() {
        return graphGamblersGuiYml;
    }

    public static FileCreator getCrashGuiYml() {
        return crashGuiYml;
    }

    public static CrashManager getCrashManager() {
        return crashManager;
    }

    public static FileCreator getCrashGamblersGuiYml() {
        return crashGamblersGuiYml;
    }

    public static FileCreator getDoubleGuiYml() {
        return doubleGuiYml;
    }

    public static DoubleManager getDoubleManager() {
        return doubleManager;
    }

    public boolean isReloading() {
        return reloading;
    }

    public void setReloading(boolean reloading) {
        this.reloading = reloading;
    }

    public Set<String> getReloadingMessages() {
        return reloadingMessages;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public static FileCreator getDoubleGamblersGuiYml() {
        return doubleGamblersGuiYml;
    }

    public static FileCreator getMainMineGuiYml() {
        return mainMineGuiYml;
    }

    public static MineManager getMineManager() {
        return mineManager;
    }

    public static FileCreator getCreateMineGuiYml() {
        return createMineGuiYml;
    }

    public static FileCreator getMineGuiYml() {
        return mineGuiYml;
    }

    public static FileCreator getMinePlayersGuiYml() {
        return minePlayersGuiYml;
    }

    public static FileCreator getAnimalGameGuiYml() {
        return animalGameGuiYml;
    }

    public static AnimalGameManager getAnimalGameManager() {
        return animalGameManager;
    }

    public static FileCreator getAnimalsGuiYml() {
        return animalsGuiYml;
    }

    public static FileCreator getAnimalGameGamblersGuiYml() {
        return animalGameGamblersGuiYml;
    }

    public static FileCreator getAnimalsBetGuiYml() {
        return animalsBetGuiYml;
    }

    public static CoinFlipManager getCoinFlipManager() {
        return coinFlipManager;
    }

    public static FileCreator getCoinFlipMainGuiYml() {
        return coinFlipMainGuiYml;
    }

    public static FileCreator getCoinFlipCreateGameGuiYml() {
        return coinFlipCreateGameGuiYml;
    }

    public static FileCreator getCoinFlipGamesGuiYml() {
        return coinFlipGamesGuiYml;
    }

    public static FileCreator getCoinFlipGuiYml() {
        return coinFlipGuiYml;
    }

    public static Messages getMessages() {
        return messages;
    }
}
