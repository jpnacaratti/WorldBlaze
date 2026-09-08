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

        this.startingTime = getInstance().getConfig().getDouble("Config.Coinflip.Tempo-iniciando");
        this.flippingTime = getInstance().getConfig().getDouble("Config.Coinflip.Tempo-flipping");

        this.breakTime = getInstance().getConfig().getInt("Config.Coinflip.Intervalo-troca");

        this.moneyAlert = getInstance().getConfig().getDouble("Config.Coinflip.Alerta-money");
        this.cashAlert = getInstance().getConfig().getDouble("Config.Coinflip.Alerta-cash");

        this.rand = new Random();

        FileConfiguration getConfig = getCoinFlipGuiYml().getConfig();

        this.coinFlipMenuTitle = c(getConfig.getString("Titulo"));

        this.colorChoice = c(getConfig.getString("Cor-escolha"));
        this.colorNoChoice = c(getConfig.getString("Cor-naoescolha"));

        this.finishName = c(getConfig.getString("finalizado.Nome"));
        this.finishLore = getConfig.getStringList("finalizado.Lore");

        if (getConfig.getBoolean("coroa.Usar-head")) coroaItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("coroa.Textura")));
        else coroaItem = new ItemBuilder(Material.getMaterial(getConfig.getString("coroa.Id")), 1, (byte) getConfig.getInt("coroa.Data"));
        coroaItem.setGlow(getConfig.getBoolean("coroa.Glow"));

        this.coroaLore = getConfig.getStringList("coroa.Lore");
        this.coroaName = c(getConfig.getString("coroa.Nome"));

        if (getConfig.getBoolean("cara.Usar-head")) caraItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("cara.Textura")));
        else caraItem = new ItemBuilder(Material.getMaterial(getConfig.getString("cara.Id")), 1, (byte) getConfig.getInt("cara.Data"));
        caraItem.setGlow(getConfig.getBoolean("cara.Glow"));

        this.caraLore = getConfig.getStringList("cara.Lore");
        this.caraName = c(getConfig.getString("cara.Nome"));

        if (getConfig.getBoolean("iniciando.Usar-head")) startingItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("iniciando.Textura")));
        else startingItem = new ItemBuilder(Material.getMaterial(getConfig.getString("iniciando.Id")), 1, (byte) getConfig.getInt("iniciando.Data"));
        startingItem.setGlow(getConfig.getBoolean("iniciando.Glow"));

        this.startingLore = getConfig.getStringList("iniciando.Lore");
        this.startingName = c(getConfig.getString("iniciando.Nome"));

        this.choiceSlot = getConfig.getInt("Slot-moeda");

        FileConfiguration getPrincipalConfig = getCoinFlipPrincipalGuiYml().getConfig();

        this.principalMenuTitle = c(getPrincipalConfig.getString("Titulo"));

        if (getPrincipalConfig.getBoolean("Itens.jogos.Usar-head")) principalGamesItem = new ItemBuilder(SkullCreator.itemFromUrl(getPrincipalConfig.getString("Itens.jogos.Textura")));
        else principalGamesItem = new ItemBuilder(Material.getMaterial(getPrincipalConfig.getString("Itens.jogos.Id")), 1, (byte) getPrincipalConfig.getInt("Itens.jogos.Data"));
        principalGamesItem.setGlow(getPrincipalConfig.getBoolean("Itens.jogos.Glow"));

        this.principalGamesLore = getPrincipalConfig.getStringList("Itens.jogos.Lore");
        this.principalGamesName = c(getPrincipalConfig.getString("Itens.jogos.Nome"));

        this.principalGamesSlot = getPrincipalConfig.getInt("Itens.jogos.Slot");

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
