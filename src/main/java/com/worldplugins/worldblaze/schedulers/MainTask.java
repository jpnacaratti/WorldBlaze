package com.worldplugins.worldblaze.schedulers;

import com.worldplugins.worldblaze.enums.DoubleColor;
import com.worldplugins.worldblaze.guis.CrashGamblersGui;
import com.worldplugins.worldblaze.guis.GraphGamblersGui;
import com.worldplugins.worldblaze.objects.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.worldplugins.worldblaze.utils.Utils.*;
import static com.worldplugins.worldblaze.WorldBlaze.*;

public class MainTask implements Runnable {

    public static Map<String, Integer> graphDelays = new HashMap<>();

    int ticks = 0;
    int graphInterval = 0;
    public static boolean locked;

    @Override
    public void run() {

        if(locked) return;

        ticks++;

        if(ticks > 20){
            ticks = 0;
            graphInterval++;

            Map<String, Integer> graphDelaysAtt = new HashMap<>();
            for (Map.Entry<String, Integer> entry : graphDelays.entrySet()) {
                if(entry.getValue() >= getGraphManager().getDelay()) continue;

                graphDelaysAtt.put(entry.getKey(), entry.getValue() + 1);
            }
            graphDelays = graphDelaysAtt;
        }

        if(getInstance().isReloading() && isAvailableForLockOrReload()){
            closeAllInventories();

            getInstance().reloadConfig();

            getPrincipalGuiYml().reloadFile();

            getGraphGuiYml().reloadFile();
            getGraphGamblersGuiYml().reloadFile();

            getCrashGuiYml().reloadFile();
            getCrashGamblersGuiYml().reloadFile();

            getDoubleGuiYml().reloadFile();
            getDoubleGamblersGuiYml().reloadFile();

            getMineGuiYml().reloadFile();
            getMinePlayersGuiYml().reloadFile();
            getCreateMineGuiYml().reloadFile();
            getPrincipalMineGuiYml().reloadFile();

            getJogoDoBichoGuiYml().reloadFile();
            getJogoDoBichoGamblersGuiYml().reloadFile();
            getBichosGuiYml().reloadFile();
            getBichosBetGuiYml().reloadFile();

            getCoinFlipPrincipalGuiYml().reloadFile();
            getCoinFlipCreateGameGuiYml().reloadFile();
            getCoinFlipGamesGuiYml().reloadFile();
            getCoinFlipGuiYml().reloadFile();

            giveCoinFlipCoins();

            getJogoDoBichoManager().savePlayers();

            getInstance().reloadManagers();

            getJogoDoBichoManager().loadPlayers();

            send = getInstance().getConfig().getBoolean("config.debug");

            getInstance().setReloading(false);

            graphDelays.clear();
            ticks = 0;
            graphInterval = 0;
            locked = false;

            for (String user : getInstance().getReloadingMessages()) {
                if(user.equalsIgnoreCase("console")) Bukkit.getConsoleSender().sendMessage(getMessages().CONFIG_RELOADED);
                else{
                    Player player = Bukkit.getPlayer(user);

                    if(player != null) player.sendMessage(getMessages().CONFIG_RELOADED);
                }
            }
            getInstance().getReloadingMessages().clear();
            return;
        }

        if(getInstance().isLocked() && isAvailableForLockOrReload()){
            getDoubleManager().setStartingRemainingTime(getDoubleManager().getStartingTime());
            getCrashManager().setTimeToSubtract(0);
            ticks = 0;
            graphInterval = 0;
            graphDelays.clear();
            locked = true;
            return;
        }

        //
        // GERENCIANDO COINFLIP
        //

        List<CoinFlipBet> toRemove = new ArrayList<>();
        for (CoinFlipBet coinFlipbet : getCoinFlipManager().getCoinFlipStartedGames()) {

            if(coinFlipbet.isStarting()){
                if(coinFlipbet.getSubtract() >= getCoinFlipManager().getStartingTime()){
                    coinFlipbet.setStarting(false);

                    coinFlipbet.setSubtract(0);

                    coinFlipbet.getGameInventory().setItem(getCoinFlipManager().getChoiceSlot(), getCoinFlipManager().getRandomChoice() ? getCoinFlipManager().getCaraItem(coinFlipbet) : getCoinFlipManager().getCoroaItem(coinFlipbet));
                } else {
                    coinFlipbet.getGameInventory().setItem(getCoinFlipManager().getChoiceSlot(), getCoinFlipManager().getStartingItem(coinFlipbet));

                    coinFlipbet.setSubtract(coinFlipbet.getSubtract() + 0.05);
                }

                coinFlipbet.updateInventories();
            }

            if(!coinFlipbet.isStarting() && !coinFlipbet.isFinished()){

                if(coinFlipbet.getSubtract() >= getCoinFlipManager().getFlippingTime()){
                    coinFlipbet.setFinished(true);

                    coinFlipbet.setChoiceCara(coinFlipbet.getFinalCoinFlip());

                    coinFlipbet.getGameInventory().setItem(getCoinFlipManager().getChoiceSlot(), getCoinFlipManager().getFinishItem(coinFlipbet));

                    coinFlipbet.updateInventories();

                    double wonMoney = (getInstance().getConfig().getDouble("config.coinflip.payout-rate")/100.0) * coinFlipbet.getMoneyValue() * 2;
                    double wonCash = (getInstance().getConfig().getDouble("config.coinflip.payout-rate")/100.0) * coinFlipbet.getCashValue() * 2;

                    Player owner = Bukkit.getPlayer(coinFlipbet.getOwner());
                    Player opponent = Bukkit.getPlayer(coinFlipbet.getOpponent());
                    if (getCoinFlipManager().isOwnerWin(coinFlipbet)) {

                        if (owner != null) {
                            getInstance().getConfig().getStringList("messages.won-coinflip").forEach(w -> owner.sendMessage(c(w.
                                    replace("{money}", letterFormatter(wonMoney)).
                                    replace("{cash}", letterFormatter(wonCash)))));

                            if(wonCash >= getCoinFlipManager().getCashAlert() || wonMoney >= getCoinFlipManager().getMoneyAlert()){
                                getInstance().getConfig().getStringList("messages.broadcast-won-coinflip").forEach(w -> Bukkit.broadcastMessage(c(w.
                                        replace("{player}", coinFlipbet.getOwner()).
                                        replace("{opponent}", coinFlipbet.getOpponent()).
                                        replace("{money}", letterFormatter(wonMoney)).
                                        replace("{cash}", letterFormatter(wonCash)))));
                            }

                            depositValues(owner, wonCash, wonMoney);
                        }

                        if (opponent != null) {
                            getInstance().getConfig().getStringList("messages.lost-coinflip").forEach(w -> opponent.sendMessage(c(w)));
                        }

                    }else{
                        if (opponent != null) {
                            getInstance().getConfig().getStringList("messages.won-coinflip").forEach(w -> opponent.sendMessage(c(w.
                                    replace("{money}", letterFormatter(wonMoney)).
                                    replace("{cash}", letterFormatter(wonCash)))));

                            if(wonCash >= getCoinFlipManager().getCashAlert() || wonMoney >= getCoinFlipManager().getMoneyAlert()){
                                getInstance().getConfig().getStringList("messages.broadcast-won-coinflip").forEach(w -> Bukkit.broadcastMessage(c(w.
                                        replace("{player}", coinFlipbet.getOpponent()).
                                        replace("{opponent}", coinFlipbet.getOwner()).
                                        replace("{money}", letterFormatter(wonMoney)).
                                        replace("{cash}", letterFormatter(wonCash)))));
                            }

                            depositValues(opponent, wonCash, wonMoney);
                        }

                        if (owner != null) {
                            getInstance().getConfig().getStringList("messages.lost-coinflip").forEach(w -> owner.sendMessage(c(w)));
                        }
                    }

                    toRemove.add(coinFlipbet);

                }else{

                    if(getCoinFlipManager().getBreakTime() > 0){
                        if(getCoinFlipManager().getBreakTime() <= coinFlipbet.getTicks()){

                            coinFlipbet.setChoiceCara(getCoinFlipManager().getRandomChoice());

                            coinFlipbet.getGameInventory().setItem(getCoinFlipManager().getChoiceSlot(), coinFlipbet.isChoiceCara() ? getCoinFlipManager().getCaraItem(coinFlipbet) : getCoinFlipManager().getCoroaItem(coinFlipbet));

                            coinFlipbet.updateInventories();

                            coinFlipbet.setTicks(0);
                        }
                        coinFlipbet.setTicks(coinFlipbet.getTicks() + 1);
                    }

                    coinFlipbet.setSubtract(coinFlipbet.getSubtract() + 0.05);
                }
            }

        }

        if(!toRemove.isEmpty()){
            getCoinFlipManager().getCoinFlipBets().removeAll(toRemove);

            getCoinFlipManager().updatePrincipalInventories();
            getCoinFlipManager().updateGamblersInventories();
        }

        //
        // GERENCIANDO DOUBLE
        //

        if(getDoubleManager().isStarting()){

            getDoubleManager().setAnimationItens();

            if(getDoubleManager().getStartingRemainingTime() == 0){

                getDoubleManager().setRoundChosenColor();

                getDoubleManager().setStarting(false);

            }else getDoubleManager().setStartingRemainingTime(getDoubleManager().getStartingRemainingTime() - 0.05);


            getDoubleManager().updateInventories(true, false, false);
        }

        if(!getDoubleManager().isStarting() && !getDoubleManager().isStopped()){
            if (getDoubleManager().manageAnimation()) {
                getDoubleManager().setStopped(true);

                DoubleColor chosenColor = getDoubleManager().getColorCache().get(13);

                for (DoubleBet doubleBet : getDoubleManager().getDoubleBets()) {
                    Player p = Bukkit.getPlayer(doubleBet.getOwner());

                    int multiplier = 2;
                    if(chosenColor == DoubleColor.WHITE && doubleBet.getColor() == DoubleColor.WHITE) multiplier = 14;

                    double wonMoney = doubleBet.getMoneyValue() * multiplier;
                    double wonCash = doubleBet.getCashValue() * multiplier;

                    boolean win = false;
                    if (doubleBet.getColor() == chosenColor){
                        depositValues(Bukkit.getOfflinePlayer(doubleBet.getOwner()), wonCash, wonMoney);
                        if(wonMoney >= getDoubleManager().getMoneyAlert() || wonCash >= getDoubleManager().getCashAlert()){

                            getInstance().getConfig().getStringList("messages.broadcast-won-double").forEach(w -> Bukkit.broadcastMessage(c(w.
                                    replace("{player}", doubleBet.getOwner()).
                                    replace("{money}", letterFormatter(wonMoney)).
                                    replace("{cash}", letterFormatter(wonCash)))));

                        }
                        win = true;
                    }

                    if(p != null) {
                        if(win){
                            getInstance().getConfig().getStringList("messages.won-double").forEach(w -> p.sendMessage(c(w.
                                    replace("{money}", letterFormatter(wonMoney)).
                                    replace("{cash}", letterFormatter(wonCash)))));
                        }else{
                            getInstance().getConfig().getStringList("messages.lost-double").forEach(w -> p.sendMessage(c(w.
                                    replace("{money}", letterFormatter(doubleBet.getMoneyValue())).
                                    replace("{cash}", letterFormatter(doubleBet.getCashValue())))));
                        }
                    }
                }

                createDoubleGamblersGui();

            }else{

                if(getDoubleManager().getAnimationTotalTime() - getDoubleManager().getBreakChangeTime() >= getDoubleManager().getAnimationRemainingTime())
                    getDoubleManager().setAnimationSpeedTime(getDoubleManager().getAnimationSpeedTime() + ((getDoubleManager().getFinalBreak() - getDoubleManager().getStartBreak()) / (getDoubleManager().getBreakChangeTime() * 20)));

            }
        }

        if(getDoubleManager().isStopped()){

            if(getDoubleManager().getStoppedRemainingTime() == 0){
                getDoubleManager().setStarting(true);
                getDoubleManager().setStopped(false);

                getDoubleManager().setStoppedRemainingTime(getDoubleManager().getStoppedTime());
                getDoubleManager().setStartingRemainingTime(getDoubleManager().getStartingTime());

                getDoubleManager().setAnimationRemainingTime(getDoubleManager().getAnimationTotalTime());

                getDoubleManager().setAnimationSpeedTime(getDoubleManager().getStartBreak());
                getDoubleManager().setAnimationSpeedRemainingTime(getDoubleManager().getStartBreak());

                getDoubleManager().getDoubleBets().clear();
                getDoubleManager().getSetAnimationSpots().clear();

                getDoubleManager().setTotalBetMoney();
                getDoubleManager().setTotalBetCash();

                getDoubleManager().setRedAverage();
                getDoubleManager().setWhiteAverage();
                getDoubleManager().setBlackAverage();

                getDoubleManager().addHistoric(getDoubleManager().getColorCache().get(13));

                getDoubleManager().updateInventories(true, true, true);

                createDoubleGamblersGui();

            }else getDoubleManager().setStoppedRemainingTime(getDoubleManager().getStoppedRemainingTime() - 0.05);
        }

        //
        // GERENCIANDO CRASH
        //

        // EXECUTA TUDO QUANDO O CRASH ESTIVER INICIANDO
        if(getCrashManager().isStarting()){
            getCrashManager().setTimeToSubtract(getCrashManager().getTimeToSubtract() + 0.05);

            // VERIFICA SE O TEMPO INICIANDO JA BATEU COM O TEMPO DA CONFIG, SENDO ASSIM ELE SETA O CRASH COMO INICIADO
            if(getCrashManager().getTimeToSubtract() >= getCrashManager().getSecondsBeforeStart()){
                getCrashManager().setStarting(false);

                getCrashManager().setTimeToSubtract(0);

                // MUDA TODOS OS VIDROS PARA QUEM ESTÁ COM O CRASH ABERTO
                for (Map.Entry<String, Inventory> entry : getCrashManager().getCrashInventories().entrySet()) {
                    for (Map.Entry<Integer, ItemStack> glass : getCrashManager().getStartedGlass().entrySet()) {
                        entry.getValue().setItem(glass.getKey(), glass.getValue());
                    }
                    Player target = Bukkit.getPlayer(entry.getKey());

                    if(target != null) {
                        BlazeUser user = getManager().getBlazeUser(entry.getKey());

                        entry.getValue().setItem(getCrashManager().getCrashStartedSlot(), getCrashManager().getCrashStartedItem(user));

                        target.updateInventory();
                    }
                }
            }else for (Map.Entry<String, Inventory> entry : getCrashManager().getCrashInventories().entrySet()) {
                Player target = Bukkit.getPlayer(entry.getKey());

                if(target != null) {
                    BlazeUser user = getManager().getBlazeUser(entry.getKey());

                    entry.getValue().setItem(getCrashManager().getCrashStartingSlot(), getCrashManager().getCrashStartingItem(user));

                    target.updateInventory();
                }
            }
        }

        // EXECUTA TUDO CASO O CRASH CRASHOU
        if(getCrashManager().isCrashed()){
            getCrashManager().setTimeToSubtract(getCrashManager().getTimeToSubtract() + 0.05);

            // VERIFICA SE O TEMPO CRASHADO JA BATEU COM O TEMPO DA CONFIG, SENDO ASSIM ELE ALTERA TODOS OS VALORES E RESETA O MANAGER
            if(getCrashManager().getTimeToSubtract() >= getCrashManager().getTimeToChangeCrashedState()){
                getCrashManager().setCrashed(false);
                getCrashManager().setStarting(true);

                getCrashManager().updateVariables();

                getCrashManager().addHistoric(getCrashManager().getCrashValue());
                getCrashManager().getCrashBets().clear();

                getCrashManager().setTimeToSubtract(0);
                getCrashManager().setCrashValue(1);
                getCrashManager().setDuration(0);

                getCrashManager().setTotalBetCash();
                getCrashManager().setTotalBetMoney();

                getCrashManager().setLastPlayer(null);

                // SETA O VIDRO DO INVENTARIO PARA A COR DO STARTING
                for (Map.Entry<String, Inventory> entry : getCrashManager().getCrashInventories().entrySet()) {
                    for (Map.Entry<Integer, ItemStack> glass : getCrashManager().getStartingGlass().entrySet()) {
                        entry.getValue().setItem(glass.getKey(), glass.getValue());
                    }

                    Player target = Bukkit.getPlayer(entry.getKey());

                    if(target != null) {
                        BlazeUser user = getManager().getBlazeUser(entry.getKey());

                        entry.getValue().setItem(getCrashManager().getHistoricSlot(), getCrashManager().getHistoricItem());
                        entry.getValue().setItem(getCrashManager().getGamblersSlot(), getCrashManager().getGamblersItem());
                        entry.getValue().setItem(getCrashManager().getCrashStartingSlot(), getCrashManager().getCrashStartingItem(user));

                        target.updateInventory();
                    }
                }

                // ATUALIZA O GUI DOS APOSTADORES PARA QUEM TEM ELE ABERTO
                createCrashGamblersGui();
            }
        }

        // EXECUTA TUDO QUANDO O CRASH ESTIVER INICIADO
        if(!getCrashManager().isStarting() && !getCrashManager().isCrashed()){

            // VERIFICA SE CRASHOU OU NÃO
            if(getCrashManager().crash()){
                getCrashManager().setCrashed(true);

                // PASSA UM FOR POR TODOS OS INVENTÁRIOS DE PESSOAS QUE AINDA ESTÃO JOGANDO
                for (Map.Entry<String, Inventory> entry : getCrashManager().getCrashInventories().entrySet()) {
                    for (Map.Entry<Integer, ItemStack> glass : getCrashManager().getCrashedGlass().entrySet()) {
                        entry.getValue().setItem(glass.getKey(), glass.getValue());
                    }

                    Player target = Bukkit.getPlayer(entry.getKey());
                    if(target != null) {
                        BlazeUser user = getManager().getBlazeUser(entry.getKey());

                        entry.getValue().setItem(getCrashManager().getCrashCrashedSlot(), getCrashManager().getCrashCrashedItem(user));

                        target.updateInventory();

                        // VERIFICA SE O JOGADOR APOSTOU
                        if (getCrashManager().getCrashBets().containsKey(entry.getKey())) {
                            CrashBet crashBet = getCrashManager().getCrashBets().get(entry.getKey());

                            // VERIFICA SE O JOGADOR JA SAIU DO CRASH
                            if (!crashBet.isLeft()) getInstance().getConfig().getStringList("messages.lost-crash").forEach(w -> target.sendMessage(c(w.
                                    replace("{money}", letterFormatter(crashBet.getMoneyValue())).
                                    replace("{cash}", letterFormatter(crashBet.getCashValue())))));
                        }
                    }
                }

            }else {
                for (Map.Entry<String, Inventory> entry : getCrashManager().getCrashInventories().entrySet()) {
                    Player target = Bukkit.getPlayer(entry.getKey());

                    if(target != null) {
                        BlazeUser user = getManager().getBlazeUser(entry.getKey());

                        entry.getValue().setItem(getCrashManager().getCrashStartedSlot(), getCrashManager().getCrashStartedItem(user));

                        target.updateInventory();
                    }
                }

                // SOMA OS VALORES DE TEMPO
                getCrashManager().setDuration(getCrashManager().getDuration() + 0.05);

                getCrashManager().setCrashValue(getCrashManager().getCrashValue() + getCrashManager().getSumRoundValue());
            }
        }

        //
        // GERENCIANDO O GRAPH
        //

        if(graphInterval >= 5) {
            graphInterval = 0;

            // DEFININDO SE O GRÁFICO VAI SUBIR OU DESCER
            if (getGraphManager().getNewGraphPoint()) {
                getGraphManager().setLastPointValue(getGraphManager().getLastPointValue() + ThreadLocalRandom.current().
                        nextDouble(getGraphManager().getMinRandomValue(), getGraphManager().getMaxRandomValue()));

                getGraphManager().getGraphPoints().add(new GraphPoint(getGraphManager().getPointsNum(), getGraphManager().getLastPointValue(),
                        getGraphManager().getPositiveItem(), true));
            } else {
                getGraphManager().setLastPointValue(getGraphManager().getLastPointValue() - ThreadLocalRandom.current().
                        nextDouble(getGraphManager().getMinRandomValue(), getGraphManager().getMaxRandomValue()));

                getGraphManager().getGraphPoints().add(new GraphPoint(getGraphManager().getPointsNum(), getGraphManager().getLastPointValue(),
                        getGraphManager().getNegativeItem(), false));
            }

            // VENDO AS APOSTAS ATUAIS E QUAIS JOGADORES GANHARAM, EM SEGUIDA REMOVENDO AS APOSTAS JA GANHAS DA LISTA
            boolean change = false;
            List<GraphBet> remove = new ArrayList<>();
            for (GraphBet graphBet : getGraphManager().getGraphBets()) {
                if (graphBet.getWhenReceive() != getGraphManager().getPointsNum()) continue;

                remove.add(graphBet);

                change = true;

                OfflinePlayer player = Bukkit.getOfflinePlayer(graphBet.getOwner());

                if (graphBet.getGraphValue() == getGraphManager().getLastPointValue()) {
                    if (player.isOnline()) getInstance().getConfig().getStringList("messages.graph-draw").forEach(w -> player.getPlayer().sendMessage(c(w)));

                    depositValues(player, graphBet.getCashValue(), graphBet.getMoneyValue());
                    continue;
                }

                if (graphBet.isPositive()) {
                    if (graphBet.getGraphValue() < getGraphManager().getLastPointValue()) {

                        double cashValue = graphBet.getCashValue() + ((getGraphManager().getReceiveValue() / 100.0) * graphBet.getCashValue());
                        double moneyValue = graphBet.getMoneyValue() + ((getGraphManager().getReceiveValue() / 100.0) * graphBet.getMoneyValue());
                        depositValues(player, cashValue, moneyValue);

                        if (player.isOnline()) getInstance().getConfig().getStringList("messages.graph-won").forEach(w -> player.getPlayer().sendMessage(c(w.
                                replace("{money}", letterFormatter(moneyValue)).replace("{cash}", letterFormatter(cashValue)))));

                    } else if (player.isOnline()) {
                        getInstance().getConfig().getStringList("messages.graph-lost").forEach(w -> player.getPlayer().sendMessage(c(w.
                                replace("{money}", letterFormatter(graphBet.getMoneyValue())).replace("{cash}", letterFormatter(graphBet.getCashValue())))));
                    }
                } else {
                    if (graphBet.getGraphValue() > getGraphManager().getLastPointValue()) {

                        double cashValue = graphBet.getCashValue() + ((getGraphManager().getReceiveValue() / 100.0) * graphBet.getCashValue());
                        double moneyValue = graphBet.getMoneyValue() + ((getGraphManager().getReceiveValue() / 100.0) * graphBet.getMoneyValue());
                        depositValues(player, cashValue, moneyValue);

                        if (player.isOnline()) getInstance().getConfig().getStringList("messages.graph-won").forEach(w -> player.getPlayer().sendMessage(c(w.
                                replace("{money}", letterFormatter(moneyValue)).replace("{cash}", letterFormatter(cashValue)))));

                    } else if (player.isOnline()) {
                        getInstance().getConfig().getStringList("messages.graph-lost").forEach(w -> player.getPlayer().sendMessage(c(w.
                                replace("{money}", letterFormatter(graphBet.getMoneyValue())).replace("{cash}", letterFormatter(graphBet.getCashValue())))));

                    }
                }
            }

            // REMOVENDO AS APOSTAS GANHAS/PERDIDAS/EMPATADAS
            getGraphManager().getGraphBets().removeAll(remove);

            if(!remove.isEmpty()) createGraphGamblersGui();

            // ATUALIZANDO O TOTAL DE CASH/MONEY APOSTADO DE ACORDO COM A REMOÇÃO DE APOSTAS ANTIGAS NO ITEM ANTERIOR
            if (change) {
                getGraphManager().updateTotalBetMoney();
                getGraphManager().updateTotalBetCash();

                getGraphManager().updateNegativeBetAverage();
                getGraphManager().updatePositiveBetAverage();

                // RECRIANDO O INVENTÁRIO DOS APOSTADORES NO GRAPH
                createGraphGamblersGui();
            }

            // REVERTENDO A LISTA DE TRAS PRA FRENTE PARA QUE O ULTIMO GRAPHPOINT ADICIONADO SEJA O '0' DA LISTA
            List<GraphPoint> reversed = new ArrayList<>(getGraphManager().getGraphPoints());
            Collections.reverse(reversed);

            for (Map.Entry<String, Inventory> entry : getGraphManager().getGraphInventories().entrySet()) {
                Inventory inv = entry.getValue();

                // LIMPANDO GRÁFICO ANTIGO DE TODOS OS INVENTÁRIO
                getGraphManager().clearGraphic(inv);

                Inventory copy = Bukkit.createInventory(null, 6 * 9, getGraphManager().getMenuTitle());

                copy.setContents(inv.getContents());

                // CRIANDO O NOVO MODELO DE GRÁFICO BASEADO NO ULTIMO GRAPH POINT ADICIONADO A LISTA
                int lastSlotPlaced = 26;
                boolean lastItemPlacedStats = false;
                int num = 0;
                for (GraphPoint graphPoint : reversed) {

                    if (getGraphManager().getPointsNum() - graphPoint.getId() > 15) break;

                    if (getGraphManager().isFirstLine(lastSlotPlaced)) break;

                    if (num > 0) {
                        if (graphPoint.isPositive()) {

                            if (!lastItemPlacedStats) lastSlotPlaced -= 10;
                            else lastSlotPlaced += 8;

                        } else {

                            if (lastItemPlacedStats) lastSlotPlaced += 8;
                            else lastSlotPlaced -= 10;

                        }
                    }

                    lastItemPlacedStats = graphPoint.isPositive();

                    num++;

                    if (lastSlotPlaced > 44 || lastSlotPlaced < 0) continue;

                    Integer emptyColumn = getGraphManager().getEmptyColumn(copy);
                    if (emptyColumn != null) {

                        int primaryColumn = getGraphManager().getPrimaryColumn(lastSlotPlaced);

                        if (primaryColumn < emptyColumn) {

                            int am = emptyColumn - primaryColumn;
                            lastSlotPlaced += am;

                        }
                    }

                    if (getGraphManager().hasAnotherItemInLine(copy, lastSlotPlaced)) continue;

                    copy.setItem(lastSlotPlaced, graphPoint.getGraphItem());
                }

                // LIMPANDO O INVENTÁRIO ANTIGO E SETANDO O NOVO
                inv.clear();
                inv.setContents(copy.getContents());

                // LIMPANDO QUALQUER TIPO DE SLOT DUPLICADO QUE O GRÁFICO GEROU
                for (Integer duplicatedSlot : getGraphManager().getDuplicatedSlots(inv)) {
                    inv.setItem(duplicatedSlot, null);
                }

                // SETANDO O SLOT DO HISTORICO QUE É O QUE VARIA A CADA SEGUNDO
                inv.setItem(getGraphManager().getHistoricSlot(), getGraphManager().getHistoricItem());
                if (change) {
                    BlazeUser user = getManager().getBlazeUser(entry.getKey());
                    entry.getValue().setItem(getGraphManager().getGamblersSlot(), getGraphManager().getGamblersItem());
                    entry.getValue().setItem(getGraphManager().getBetUpSlot(), getGraphManager().getBetUpItem(user));
                    entry.getValue().setItem(getGraphManager().getBetDecreaseSlot(), getGraphManager().getBetDecreaseItem(user));
                }

                // ATUALIZANDO INVENTÁRIO DOS PLAYERS
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) player.updateInventory();
            }

            // DEIXANDO A LISTA COM 30 POINTS APENAS
            if (getGraphManager().getGraphPoints().size() > 30) {
                List<GraphPoint> aa = new ArrayList<>(getGraphManager().getGraphPoints());
                Collections.reverse(aa);

                getGraphManager().getGraphPoints().clear();

                int count = 0;
                for (GraphPoint graphPoint : aa) {
                    if (count == 30) break;
                    getGraphManager().getGraphPoints().add(graphPoint);
                    count++;
                }
                Collections.reverse(getGraphManager().getGraphPoints());
            }

            getGraphManager().updateHistoric();

            getGraphManager().setPointsNum(getGraphManager().getPointsNum() + 1);
        }
    }
}
