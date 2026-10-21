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

    // VARIAVEIS QUE NAO VAO MUDAR EM HIPOTESE ALGUMA
    private final List<CoinFlipBet> coinFlipBets;
    private final Set<String> coinFlipGamblersGuiOpen;
    private final Map<String, Inventory> principalInventories;
    private final double startingTime, flippingTime, moneyAlert, cashAlert;
    private final int breakTime;
    private final String principalMenuTitle, coinFlipMenuTitle;
    private final Random rand;

    // VARIAVEIS QUE NAO VAO MUDAR EM HIPOTESE ALGUMA, ITENS
    private final ItemBuilder principalGamesItem, startingItem, caraItem, coroaItem;
    private final String principalGamesName, startingName, caraName, coroaName, finishName, colorChoice, colorNoChoice;
    private final List<String> principalGamesLore, startingLore, caraLore, coroaLore, finishLore;
    private final int principalGamesSlot, choiceSlot;

    public CoinFlipManager(){

        this.coinFlipBets = new ArrayList<>();
        this.principalInventories = new HashMap<>();
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

        if (getConfig.getBoolean("tails.use-head")) coroaItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("tails.texture")));
        else coroaItem = new ItemBuilder(Material.getMaterial(getConfig.getString("tails.id")), 1, (byte) getConfig.getInt("tails.data"));
        coroaItem.setGlow(getConfig.getBoolean("tails.glow"));

        this.coroaLore = getConfig.getStringList("tails.lore");
        this.coroaName = c(getConfig.getString("tails.name"));

        if (getConfig.getBoolean("heads.use-head")) caraItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("heads.texture")));
        else caraItem = new ItemBuilder(Material.getMaterial(getConfig.getString("heads.id")), 1, (byte) getConfig.getInt("heads.data"));
        caraItem.setGlow(getConfig.getBoolean("heads.glow"));

        this.caraLore = getConfig.getStringList("heads.lore");
        this.caraName = c(getConfig.getString("heads.name"));

        if (getConfig.getBoolean("starting.use-head")) startingItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("starting.texture")));
        else startingItem = new ItemBuilder(Material.getMaterial(getConfig.getString("starting.id")), 1, (byte) getConfig.getInt("starting.data"));
        startingItem.setGlow(getConfig.getBoolean("starting.glow"));

        this.startingLore = getConfig.getStringList("starting.lore");
        this.startingName = c(getConfig.getString("starting.name"));

        this.choiceSlot = getConfig.getInt("coin-slot");

        FileConfiguration getPrincipalConfig = getCoinFlipPrincipalGuiYml().getConfig();

        this.principalMenuTitle = c(getPrincipalConfig.getString("title"));

        if (getPrincipalConfig.getBoolean("items.games.use-head")) principalGamesItem = new ItemBuilder(SkullCreator.itemFromUrl(getPrincipalConfig.getString("items.games.texture")));
        else principalGamesItem = new ItemBuilder(Material.getMaterial(getPrincipalConfig.getString("items.games.id")), 1, (byte) getPrincipalConfig.getInt("items.games.data"));
        principalGamesItem.setGlow(getPrincipalConfig.getBoolean("items.games.glow"));

        this.principalGamesLore = getPrincipalConfig.getStringList("items.games.lore");
        this.principalGamesName = c(getPrincipalConfig.getString("items.games.name"));

        this.principalGamesSlot = getPrincipalConfig.getInt("items.games.slot");

    }

    public List<CoinFlipBet> getCoinFlipBets() {
        return coinFlipBets;
    }

    public Map<String, Inventory> getPrincipalInventories() {
        return principalInventories;
    }

    public Set<String> getCoinFlipGamblersGuiOpen() {
        return coinFlipGamblersGuiOpen;
    }

    public String getPrincipalMenuTitle() {
        return principalMenuTitle;
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

    public ItemStack getPrincipalGamesItem(String player){
        ItemBuilder clone = principalGamesItem.clone();

        clone.setName(principalGamesName);

        principalGamesLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{ocorrendo}", getNumClosedGames() + "").
                replace("{espera}", getNumOpenGames() + "").
                replace("{especifico}", getNumSpecificGames(player) + ""))));

        return clone.toItemStack();
    }

    public ItemStack getStartingItem(CoinFlipBet bet) {
        ItemBuilder clone = startingItem.clone();

        clone.setName(startingName);

        startingLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{tempo}", String.format("%.2f", (startingTime - bet.getSubtract()))).
                replace("{money}", letterFormatter(bet.getMoneyValue())).
                replace("{cash}", letterFormatter(bet.getCashValue())).
                replace("{jogadorDono}", bet.getOwner()).
                replace("{jogadorOponente}", bet.getOpponent()).
                replace("{escolhaDono}",bet.isCara() ? "Cara" : "Coroa").
                replace("{escolhaOponente}",bet.isCara() ? "Coroa" : "Cara"))));

        return clone.toItemStack();
    }

    public ItemStack getCaraItem(CoinFlipBet bet) {
        ItemBuilder clone = caraItem.clone();

        clone.setName(caraName);

        for (String s : caraLore) {
            if(s.contains("{cor}")){

                boolean change = false;
                if(s.contains("{jogadorDono}")){

                    if(bet.isCara() && bet.isChoiceCara()) change = true;
                    else if (!bet.isCara() && !bet.isChoiceCara()) change = true;

                }else if(s.contains("{jogadorOponente}")){

                    if(bet.isCara() && !bet.isChoiceCara()) change = true;
                    else if (!bet.isCara() && bet.isChoiceCara()) change = true;

                }

                clone.addLoreLine(c(s.
                        replace("{cor}", change ? colorChoice : colorNoChoice).
                        replace("{money}", letterFormatter(bet.getMoneyValue())).
                        replace("{cash}", letterFormatter(bet.getCashValue())).
                        replace("{jogadorDono}", bet.getOwner()).
                        replace("{jogadorOponente}", bet.getOpponent()).
                        replace("{escolhaDono}",bet.isCara() ? "Cara" : "Coroa").
                        replace("{escolhaOponente}",bet.isCara() ? "Coroa" : "Cara")));

            }else{
                clone.addLoreLine(c(s.
                        replace("{money}", letterFormatter(bet.getMoneyValue())).
                        replace("{cash}", letterFormatter(bet.getCashValue())).
                        replace("{jogadorDono}", bet.getOwner()).
                        replace("{jogadorOponente}", bet.getOpponent()).
                        replace("{escolhaDono}",bet.isCara() ? "Cara" : "Coroa").
                        replace("{escolhaOponente}",bet.isCara() ? "Coroa" : "Cara")));
            }
        }

        return clone.toItemStack();
    }

    public ItemStack getCoroaItem(CoinFlipBet bet) {
        ItemBuilder clone = coroaItem.clone();

        clone.setName(coroaName);

        for (String s : coroaLore) {
            if(s.contains("{cor}")){

                boolean change = false;
                if(s.contains("{jogadorDono}")){

                    if(bet.isCara() && bet.isChoiceCara()) change = true;
                    else if (!bet.isCara() && !bet.isChoiceCara()) change = true;

                }else if(s.contains("{jogadorOponente}")){

                    if(bet.isCara() && !bet.isChoiceCara()) change = true;
                    else if (!bet.isCara() && bet.isChoiceCara()) change = true;

                }

                clone.addLoreLine(c(s.
                        replace("{cor}", change ? colorChoice : colorNoChoice).
                        replace("{money}", letterFormatter(bet.getMoneyValue())).
                        replace("{cash}", letterFormatter(bet.getCashValue())).
                        replace("{jogadorDono}", bet.getOwner()).
                        replace("{jogadorOponente}", bet.getOpponent()).
                        replace("{escolhaDono}",bet.isCara() ? "Cara" : "Coroa").
                        replace("{escolhaOponente}",bet.isCara() ? "Coroa" : "Cara")));

            }else{
                clone.addLoreLine(c(s.
                        replace("{money}", letterFormatter(bet.getMoneyValue())).
                        replace("{cash}", letterFormatter(bet.getCashValue())).
                        replace("{jogadorDono}", bet.getOwner()).
                        replace("{jogadorOponente}", bet.getOpponent()).
                        replace("{escolhaDono}",bet.isCara() ? "Cara" : "Coroa").
                        replace("{escolhaOponente}",bet.isCara() ? "Coroa" : "Cara")));
            }
        }

        return clone.toItemStack();
    }

    public ItemStack getFinishItem(CoinFlipBet bet){

        ItemBuilder item;
        if (bet.isChoiceCara()) item = caraItem.clone();
        else item = coroaItem.clone();

        item.setName(finishName);

        String winner = "-/-";
        if(bet.isCara() && bet.isChoiceCara()) winner = bet.getOwner();
        else if(!bet.isCara() && !bet.isChoiceCara()) winner = bet.getOwner();
        else if(bet.isCara() && !bet.isChoiceCara()) winner = bet.getOpponent();
        else if(!bet.isCara() && bet.isChoiceCara()) winner = bet.getOpponent();

        String finalWinner = winner;
        finishLore.forEach(w -> item.addLoreLine(c(w.replace("{escolhaFinal}", bet.isChoiceCara() ? "Cara" : "Coroa").
                replace("{winner}", finalWinner).
                replace("{money}", letterFormatter(bet.getMoneyValue() * 2)).
                replace("{cash}", letterFormatter(bet.getCashValue() * 2))
        )));

        return item.toItemStack();
    }

    public int getPrincipalGamesSlot() {
        return principalGamesSlot;
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

    public void updatePrincipalInventories(){
        Bukkit.getScheduler().runTask(getInstance(), ()->{
            for (Map.Entry<String, Inventory> entry : getCoinFlipManager().getPrincipalInventories().entrySet()) {
                entry.getValue().setItem(getCoinFlipManager().getPrincipalGamesSlot(), getCoinFlipManager().getPrincipalGamesItem(entry.getKey()));
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

        if(bet.isCara() && bet.isChoiceCara()) return true;
        else if(!bet.isCara() && !bet.isChoiceCara()) return true;
        else if(bet.isCara() && !bet.isChoiceCara()) return false;
        else if(!bet.isCara() && bet.isChoiceCara()) return false;

        return false;
    }
}
