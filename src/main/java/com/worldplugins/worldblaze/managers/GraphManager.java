package com.worldplugins.worldblaze.managers;

import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.objects.GraphBet;
import com.worldplugins.worldblaze.objects.GraphPoint;
import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.NBTTag;
import com.worldplugins.worldblaze.utils.SkullCreator;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class GraphManager {

    // VARIAVEIS FINAIS DO GRÁFICO QUE EM HIPOTESE ALGUMA SERÃO ALTERADAS
    private final Set<String> gamblersGuiOpen;
    private final List<GraphPoint> graphPoints;
    private final List<GraphBet> graphBets;
    private final Map<String, Inventory> graphInventories;
    private final String menuTitle;
    private final double receiveValue, maxRandomValue, minRandomValue;
    private final Random rand;
    private final int delay;

    // VARIAVEIS QUE VÃO DEFINIR O ANDAMENTO DO GRÁFICO
    private boolean graphDirection;
    private int waveCounter;

    // VARIAVEIS QUE VAO MUDAR CONFORME O GRÁFICO ANDA
    private int pointsNum;
    private double lastPointValue, positiveBetAverage, negativeBetAverage, totalBetCash, totalBetMoney;
    private final List<Boolean> historic;

    // CONFIGURAÇÃO DOS ITENS DO MENU
    private final ItemBuilder positiveItem, negativeItem, betDecreaseItem, betUpItem, historicItem, gamblersItem, timeItem, moneyItem, cashItem;
    private final List<String> positiveLore, negativeLore, betDecreaseLore, betUpLore, historicLore, gamblersLore, timeLore, moneyLore, cashLore;
    private final String positiveName, negativeName, betDecreaseName, betUpName, historicName, historicPositiveDisplay, historicNegativeDisplay, gamblersName, timeName, moneyName, cashName;
    private final boolean gamblersUsePlayersHead;
    private final int betDecreaseSlot, betUpSlot, historicSlot, gamblersSlot, timeSlot, moneySlot, cashSlot;

    public GraphManager(){

        FileConfiguration getConfig = getGraphGuiYml().getConfig();

        rand = new Random();

        this.gamblersGuiOpen = new HashSet<>();
        this.graphInventories = new HashMap<>();
        this.graphBets = new ArrayList<>();
        this.historic = new ArrayList<>();
        this.graphPoints = new ArrayList<>();

        if (getConfig.getBoolean("positivo.Usar-head")) positiveItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("positivo.Textura")));
        else positiveItem = new ItemBuilder(Material.getMaterial(getConfig.getString("positivo.Id")), 1, (byte) getConfig.getInt("positivo.Data"));
        positiveItem.setGlow(getConfig.getBoolean("positivo.Glow"));

        this.positiveLore = getConfig.getStringList("positivo.Lore");
        this.positiveName = c(getConfig.getString("positivo.Nome"));

        if (getConfig.getBoolean("negativo.Usar-head")) negativeItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("negativo.Textura")));
        else negativeItem = new ItemBuilder(Material.getMaterial(getConfig.getString("negativo.Id")), 1, (byte) getConfig.getInt("negativo.Data"));
        negativeItem.setGlow(getConfig.getBoolean("negativo.Glow"));

        this.negativeLore = getConfig.getStringList("negativo.Lore");
        this.negativeName = c(getConfig.getString("negativo.Nome"));

        if (getConfig.getBoolean("Itens.apostar-diminuir.Usar-head")) betDecreaseItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.apostar-diminuir.Textura")));
        else betDecreaseItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.apostar-diminuir.Id")), 1, (byte) getConfig.getInt("Itens.apostar-diminuir.Data"));
        betDecreaseItem.setGlow(getConfig.getBoolean("Itens.apostar-diminuir.Glow"));

        this.betDecreaseLore = getConfig.getStringList("Itens.apostar-diminuir.Lore");
        this.betDecreaseName = c(getConfig.getString("Itens.apostar-diminuir.Nome"));

        if (getConfig.getBoolean("Itens.apostar-aumentar.Usar-head")) betUpItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.apostar-aumentar.Textura")));
        else betUpItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.apostar-aumentar.Id")), 1, (byte) getConfig.getInt("Itens.apostar-aumentar.Data"));
        betUpItem.setGlow(getConfig.getBoolean("Itens.apostar-aumentar.Glow"));

        this.betUpLore = getConfig.getStringList("Itens.apostar-aumentar.Lore");
        this.betUpName = c(getConfig.getString("Itens.apostar-aumentar.Nome"));

        if (getConfig.getBoolean("Itens.historico.Usar-head")) historicItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.historico.Textura")));
        else historicItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.historico.Id")), 1, (byte) getConfig.getInt("Itens.historico.Data"));
        historicItem.setGlow(getConfig.getBoolean("Itens.historico.Glow"));

        this.historicLore = getConfig.getStringList("Itens.historico.Lore");
        this.historicName = c(getConfig.getString("Itens.historico.Nome"));

        this.historicPositiveDisplay = c(getConfig.getString("Itens.historico.Display-positivo"));
        this.historicNegativeDisplay = c(getConfig.getString("Itens.historico.Display-negativo"));

        if (getConfig.getBoolean("Itens.apostadores.Usar-head-ultimo-player")) gamblersItem = new ItemBuilder(SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654"));
        else if (getConfig.getBoolean("Itens.apostadores.Usar-head")) gamblersItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.apostadores.Textura")));
        else gamblersItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.apostadores.Id")), 1, (byte) getConfig.getInt("Itens.apostadores.Data"));
        gamblersItem.setGlow(getConfig.getBoolean("Itens.apostadores.Glow"));

        this.gamblersLore = getConfig.getStringList("Itens.apostadores.Lore");
        this.gamblersName = c(getConfig.getString("Itens.apostadores.Nome"));

        this.gamblersUsePlayersHead = getConfig.getBoolean("Itens.apostadores.Usar-head-ultimo-player");

        if (getConfig.getBoolean("Itens.tempo.Usar-head")) timeItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.tempo.Textura")));
        else timeItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.tempo.Id")), 1, (byte) getConfig.getInt("Itens.tempo.Data"));
        timeItem.setGlow(getConfig.getBoolean("Itens.tempo.Glow"));

        this.timeLore = getConfig.getStringList("Itens.tempo.Lore");
        this.timeName = c(getConfig.getString("Itens.tempo.Nome"));

        if (getConfig.getBoolean("Itens.money.Usar-head")) moneyItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.money.Textura")));
        else moneyItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.money.Id")), 1, (byte) getConfig.getInt("Itens.money.Data"));
        moneyItem.setGlow(getConfig.getBoolean("Itens.money.Glow"));

        this.moneyLore = getConfig.getStringList("Itens.money.Lore");
        this.moneyName = c(getConfig.getString("Itens.money.Nome"));

        if (getConfig.getBoolean("Itens.cash.Usar-head")) cashItem = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.cash.Textura")));
        else cashItem = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.cash.Id")), 1, (byte) getConfig.getInt("Itens.cash.Data"));
        cashItem.setGlow(getConfig.getBoolean("Itens.cash.Glow"));

        this.cashLore = getConfig.getStringList("Itens.cash.Lore");
        this.cashName = c(getConfig.getString("Itens.cash.Nome"));

        this.betDecreaseSlot = getConfig.getInt("Itens.apostar-diminuir.Slot");
        this.betUpSlot = getConfig.getInt("Itens.apostar-aumentar.Slot");
        this.timeSlot = getConfig.getInt("Itens.tempo.Slot");
        this.moneySlot = getConfig.getInt("Itens.money.Slot");
        this.cashSlot = getConfig.getInt("Itens.cash.Slot");
        this.historicSlot = getConfig.getInt("Itens.historico.Slot");
        this.gamblersSlot = getConfig.getInt("Itens.apostadores.Slot");

        lastPointValue = getInstance().getConfig().getDouble("Config.Graph.Valor-inicial");

        this.menuTitle = c(getConfig.getString("Titulo"));

        this.receiveValue = getInstance().getConfig().getDouble("Config.Graph.Retorno-ganho");
        this.maxRandomValue = getInstance().getConfig().getDouble("Config.Graph.Max-random");
        this.minRandomValue = getInstance().getConfig().getDouble("Config.Graph.Min-random");

        this.graphDirection = rand.nextBoolean();

        this.pointsNum = 1;

        this.delay = getInstance().getConfig().getInt("Config.Graph.Delay-entre-aposta");
    }

    public Set<String> getGamblersGuiOpen() {
        return gamblersGuiOpen;
    }

    public int getDelay() {
        return delay;
    }

    public List<GraphPoint> getGraphPoints() {
        return graphPoints;
    }

    public List<GraphBet> getGraphBets() {
        return graphBets;
    }

    public int getBetDecreaseSlot() {
        return betDecreaseSlot;
    }

    public int getBetUpSlot() {
        return betUpSlot;
    }

    public int getHistoricSlot() {
        return historicSlot;
    }

    public int getGamblersSlot() {
        return gamblersSlot;
    }

    public int getTimeSlot() {
        return timeSlot;
    }

    public int getMoneySlot() {
        return moneySlot;
    }

    public int getCashSlot() {
        return cashSlot;
    }

    public double getTotalBetMoney(){
        return totalBetMoney;
    }

    public void updateTotalBetMoney(){

        if(graphBets.size() == 0){
            this.totalBetMoney = 0;
            return;
        }

        final double[] toR = {0};

        graphBets.forEach(w -> toR[0] += w.getMoneyValue());

        this.totalBetMoney = toR[0];
    }

    public double getTotalBetCash(){
        return totalBetCash;
    }

    public void updateTotalBetCash(){

        if(graphBets.size() == 0){
            this.totalBetCash = 0;
            return;
        }

        final double[] toR = {0};

        graphBets.forEach(w -> toR[0] += w.getCashValue());

        this.totalBetCash = toR[0];
    }

    public String getLastPlayer(){

        if(graphBets.size() == 0) return "-/-";

        return graphBets.get(graphBets.size() - 1).getOwner();
    }

    public double getPositiveBetAverage(){
        return positiveBetAverage;
    }

    public void updatePositiveBetAverage(){

        if(graphBets.size() == 0){
            this.positiveBetAverage = 0;
            return;
        }

        double cache = 0;
        double total = graphBets.size();

        for (GraphBet graphBet : graphBets) {
            if(graphBet.isPositive()) cache++;
        }

        this.positiveBetAverage = (cache / total) * 100.0;
    }

    public double getNegativeBetAverage(){
        return negativeBetAverage;
    }

    public void updateNegativeBetAverage(){

        if(graphBets.size() == 0){
            this.negativeBetAverage = 0;
            return;
        }

        double cache = 0;
        double total = graphBets.size();

        for (GraphBet graphBet : graphBets) {
            if(!graphBet.isPositive()) cache++;
        }

        this.negativeBetAverage = (cache / total) * 100.0;
    }

    public int getPointsNum() {
        return pointsNum;
    }

    public void setPointsNum(int pointsNum) {
        this.pointsNum = pointsNum;
    }

    public double getLastPointValue() {
        return lastPointValue;
    }

    public void setLastPointValue(double lastPointValue) {
        this.lastPointValue = lastPointValue;
    }

    public ItemStack getPositiveItem(){

        ItemBuilder clone = positiveItem.clone();

        clone.setName(positiveName.replace("{update}", pointsNum+""));

        positiveLore.forEach(w -> clone.addLoreLine(c(w.replace("{point-value}", String.format("%.2f", lastPointValue)))));

        NBTTag.setNBTString(clone.toItemStack().getItemMeta(), "WBlaze_graphPoint", pointsNum + "");

        return clone.toItemStack();
    }

    public ItemStack getNegativeItem(){

        ItemBuilder clone = negativeItem.clone();

        clone.setName(negativeName.replace("{update}", pointsNum+""));

        negativeLore.forEach(w -> clone.addLoreLine(c(w.replace("{point-value}", String.format("%.2f", lastPointValue)))));

        NBTTag.setNBTString(clone.toItemStack().getItemMeta(), "WBlaze_graphPoint", pointsNum + "");

        return clone.toItemStack();
    }

    public Map<String, Inventory> getGraphInventories() {
        return graphInventories;
    }

    public Integer getEmptyColumn(Inventory inv) {

        for (int a = 7; a >= 0; a--) {
            boolean empty = true;
            for (int i = 0; i < 5; i++) {
                if (inv.getItem(a + (9 * i)) != null) {
                    empty = false;
                    break;
                }
            }
            if (empty) return a;
        }

        return null;
    }

    public List<Integer> getDuplicatedSlots(Inventory inv){
        List<Integer> toR = new ArrayList<>();

        for (int a = 8; a >= 0; a--) {
            Map<Integer, Integer> map = new HashMap<>();
            for (int i = 0; i < 5; i++) {
                ItemStack item = inv.getItem(a + (9 * i));
                if (item != null) {
                    String nbt = NBTTag.getNBTString(item.getItemMeta(), "WBlaze_graphPoint");
                    if(nbt != null) map.put(a + (9 * i), Integer.parseInt(nbt));
                }
            }
            if(!map.isEmpty()){
                List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(map.entrySet());
                sorted.sort(Map.Entry.comparingByValue());
                for(int i = 0; i < sorted.size() - 1; i++){
                    toR.add(sorted.get(i).getKey());
                }
            }
        }

        return toR;
    }

    public boolean hasAnotherItemInLine(Inventory inv, int slot){

        int lastValue = slot;
        for (int i = 0; i < 5; i++) {
            int cache = lastValue - 9;
            if(cache < 0) break;
            lastValue = cache;
        }

        boolean empty = false;
        for (int i = 0; i < 5; i++) {
            if (inv.getItem(lastValue + (9 * i)) != null) {
                empty = true;
                break;
            }
        }

        return empty;
    }

    public boolean isFirstLine(int slot){
        int lastValue = slot;
        for (int i = 0; i < 5; i++) {
            int cache = lastValue - 9;
            if(cache < 0) break;
            lastValue = cache;
        }

        return lastValue == 0;
    }

    public int getPrimaryColumn(int slot){
        int lastValue = slot;
        for (int i = 0; i < 5; i++) {
            int cache = lastValue - 9;
            if(cache < 0) break;
            lastValue = cache;
        }

        return lastValue;
    }

    public void clearGraphic(Inventory inv){
        for (int a = 8; a >= 0; a--) {
            for (int i = 0; i < 5; i++) {
                inv.setItem(a + (9 * i), null);
            }
        }
    }

    public List<Boolean> getHistoric(){
        return historic;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public void updateHistoric(){
        List<Boolean> toR = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            try{
                toR.add(graphPoints.get(graphPoints.size() - 1 - i).isPositive());
            }catch (IndexOutOfBoundsException ex){
                break;
            }
        }

        historic.clear();
        historic.addAll(toR);
    }

    public ItemStack getBetUpItem(BlazeUser user){
        ItemBuilder clone = betUpItem.clone();

        clone.setName(betUpName);

        betUpLore.forEach(w -> clone.addLoreLine(c(w.replace("{positive_average}", String.format("%.2f", positiveBetAverage)).
                replace("{tempo}", user.getGraphTime()+"").
                replace("{money}", letterFormatter(user.getGraphMoneyValue())).
                replace("{cash}", letterFormatter(user.getGraphCashValue())))));

        return clone.toItemStack();
    }

    public ItemStack getBetDecreaseItem(BlazeUser user){
        ItemBuilder clone = betDecreaseItem.clone();

        clone.setName(betDecreaseName);

        betDecreaseLore.forEach(w -> clone.addLoreLine(c(w.replace("{negative_average}", String.format("%.2f", negativeBetAverage)).
                replace("{tempo}", user.getGraphTime()+"").
                replace("{money}", letterFormatter(user.getGraphMoneyValue())).
                replace("{cash}", letterFormatter(user.getGraphCashValue())))));

        return clone.toItemStack();
    }

    public ItemStack getHistoricItem(){
        ItemBuilder clone = historicItem.clone();

        clone.setName(historicName);

        for (String s : historicLore) {
            if(s.contains("{historico}")){
                for (Boolean b : getGraphManager().getHistoric()) if(b) clone.addLoreLine(c(s.replace("{historico}", historicPositiveDisplay)));
                else clone.addLoreLine(c(s.replace("{historico}", historicNegativeDisplay)));
            }else clone.addLoreLine(c(s));
        }

        return clone.toItemStack();
    }

    public ItemStack getGamblersItem(){
        ItemBuilder clone;
        if(!gamblersUsePlayersHead || graphBets.size() == 0) clone = gamblersItem.clone();
        else clone = new ItemBuilder(SkullCreator.itemFromName(getLastPlayer()));

        clone.setName(gamblersName);

        gamblersLore.forEach(w -> clone.addLoreLine(c(w.replace("{jogadores}", graphBets.size()+"").
                replace("{negative_average}", String.format("%.2f", negativeBetAverage)).
                replace("{positive_average}", String.format("%.2f", positiveBetAverage)).
                replace("{last_player}", getLastPlayer()).
                replace("{total_money}", letterFormatter(totalBetMoney)).
                replace("{total_cash}", letterFormatter(totalBetCash)))));

        return clone.toItemStack();
    }

    public ItemStack getTimeItem(BlazeUser user){
        ItemBuilder clone = timeItem.clone();

        clone.setName(timeName);

        timeLore.forEach(w -> clone.addLoreLine(c(w.replace("{tempo}", user.getGraphTime()+""))));

        return clone.toItemStack();
    }

    public ItemStack getMoneyItem(BlazeUser user){
        ItemBuilder clone = moneyItem.clone();

        clone.setName(moneyName);

        moneyLore.forEach(w -> clone.addLoreLine(c(w.replace("{money}", letterFormatter(user.getGraphMoneyValue())))));

        return clone.toItemStack();
    }

    public ItemStack getCashItem(BlazeUser user){
        ItemBuilder clone = cashItem.clone();

        clone.setName(cashName);

        cashLore.forEach(w -> clone.addLoreLine(c(w.replace("{cash}", letterFormatter(user.getGraphCashValue())))));

        return clone.toItemStack();
    }

    public double getReceiveValue() {
        return receiveValue;
    }

    public double getMaxRandomValue() {
        return maxRandomValue;
    }

    public double getMinRandomValue() {
        return minRandomValue;
    }

    public boolean getNewGraphPoint(){

        waveCounter++;

        if(waveCounter == 33){
            waveCounter = 0;

            if(negativeBetAverage == positiveBetAverage){
                graphDirection = rand.nextBoolean();
            }else{
                graphDirection = !(positiveBetAverage > negativeBetAverage);
            }

            sendDebugMessage("[Graph] Próxima tendencia é: "+graphDirection);

            return !graphDirection;
        }else{

            int minus = 33 - waveCounter;
            double positivePercent;
            if(minus <= 5){
                sendDebugMessage("[Graph] Voltou a subir " + waveCounter);

                if(minus == 2 || minus == 1) return graphDirection;

                if(graphDirection) positivePercent = 50.0 + (10 * (6.0 - minus));
                else positivePercent = 50.0 - (10 * (6.0 - minus));

                return rand.nextInt(100) <= positivePercent;
            }else if(minus <= 8){

                sendDebugMessage("[Graph] Começou a cair " + waveCounter);

                if(graphDirection) positivePercent = 50.0 - (5 * (9.0 - minus));
                else positivePercent = 50.0 + (5 * (9.0 - minus));

                return rand.nextInt(100) <= positivePercent;
            }

            if(rand.nextInt(100) < 15){
                sendDebugMessage("[Graph] Prejudicou o povo");
                if(positiveBetAverage > negativeBetAverage) return false;
                if(positiveBetAverage < negativeBetAverage) return true;
            }

            if(rand.nextInt(100) < 10){
                sendDebugMessage("[Graph] Ajudou o povo");
                if(positiveBetAverage > negativeBetAverage) return true;
                if(positiveBetAverage < negativeBetAverage) return false;
            }

        }

        return rand.nextBoolean();
    }
}
