package com.worldplugins.worldblaze.schedulers;

import com.worldplugins.worldblaze.enums.DoubleColor;
import com.worldplugins.worldblaze.guis.CrashGamblersGui;
import com.worldplugins.worldblaze.guis.GraphGamblersGui;
import com.worldplugins.worldblaze.guis.MenuHolder;
import com.worldplugins.worldblaze.guis.MenuType;
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

            Map<String, Integer> graphDelaysUpdated = new HashMap<>();
            for (Map.Entry<String, Integer> entry : graphDelays.entrySet()) {
                if(entry.getValue() >= getGraphManager().getDelay()) continue;

                graphDelaysUpdated.put(entry.getKey(), entry.getValue() + 1);
            }
            graphDelays = graphDelaysUpdated;
        }

        if(getInstance().isReloading() && isAvailableForLockOrReload()){
            closeAllInventories();

            getInstance().reloadConfig();

            getMainGuiYml().reloadFile();

            getGraphGuiYml().reloadFile();
            getGraphGamblersGuiYml().reloadFile();

            getCrashGuiYml().reloadFile();
            getCrashGamblersGuiYml().reloadFile();

            getDoubleGuiYml().reloadFile();
            getDoubleGamblersGuiYml().reloadFile();

            getMineGuiYml().reloadFile();
            getMinePlayersGuiYml().reloadFile();
            getCreateMineGuiYml().reloadFile();
            getMainMineGuiYml().reloadFile();

            getAnimalGameGuiYml().reloadFile();
            getAnimalGameGamblersGuiYml().reloadFile();
            getAnimalsGuiYml().reloadFile();
            getAnimalsBetGuiYml().reloadFile();

            getCoinFlipMainGuiYml().reloadFile();
            getCoinFlipCreateGameGuiYml().reloadFile();
            getCoinFlipGamesGuiYml().reloadFile();
            getCoinFlipGuiYml().reloadFile();

            giveCoinFlipCoins();

            getAnimalGameManager().savePlayers();

            getInstance().reloadManagers();

            getAnimalGameManager().loadPlayers();

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
        // MANAGING COINFLIP
        //

        List<CoinFlipBet> toRemove = new ArrayList<>();
        for (CoinFlipBet coinFlipbet : getCoinFlipManager().getCoinFlipStartedGames()) {

            if(coinFlipbet.isStarting()){
                if(coinFlipbet.getSubtract() >= getCoinFlipManager().getStartingTime()){
                    coinFlipbet.setStarting(false);

                    coinFlipbet.setSubtract(0);

                    coinFlipbet.getGameInventory().setItem(getCoinFlipManager().getChoiceSlot(), getCoinFlipManager().getRandomChoice() ? getCoinFlipManager().getHeadsItem(coinFlipbet) : getCoinFlipManager().getTailsItem(coinFlipbet));
                } else {
                    coinFlipbet.getGameInventory().setItem(getCoinFlipManager().getChoiceSlot(), getCoinFlipManager().getStartingItem(coinFlipbet));

                    coinFlipbet.setSubtract(coinFlipbet.getSubtract() + 0.05);
                }

                coinFlipbet.updateInventories();
            }

            if(!coinFlipbet.isStarting() && !coinFlipbet.isFinished()){

                if(coinFlipbet.getSubtract() >= getCoinFlipManager().getFlippingTime()){
                    coinFlipbet.setFinished(true);

                    coinFlipbet.setChoiceHeads(coinFlipbet.getFinalCoinFlip());

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

                            coinFlipbet.setChoiceHeads(getCoinFlipManager().getRandomChoice());

                            coinFlipbet.getGameInventory().setItem(getCoinFlipManager().getChoiceSlot(), coinFlipbet.isChoiceHeads() ? getCoinFlipManager().getHeadsItem(coinFlipbet) : getCoinFlipManager().getTailsItem(coinFlipbet));

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

            getCoinFlipManager().updateMainInventories();
            getCoinFlipManager().updateGamblersInventories();
        }

        //
        // MANAGING DOUBLE
        //

        if(getDoubleManager().isStarting()){

            getDoubleManager().setAnimationItems();

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
        // MANAGING CRASH
        //

        // RUNS EVERYTHING WHEN THE CRASH IS STARTING
        if(getCrashManager().isStarting()){
            getCrashManager().setTimeToSubtract(getCrashManager().getTimeToSubtract() + 0.05);

            // CHECKS IF THE STARTING TIME HAS REACHED THE CONFIG TIME, IF SO IT SETS THE CRASH AS STARTED
            if(getCrashManager().getTimeToSubtract() >= getCrashManager().getSecondsBeforeStart()){
                getCrashManager().setStarting(false);

                getCrashManager().setTimeToSubtract(0);

                // CHANGES ALL THE GLASS PANES FOR WHOEVER HAS THE CRASH OPEN
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

        // RUNS EVERYTHING IF THE CRASH HAS CRASHED
        if(getCrashManager().isCrashed()){
            getCrashManager().setTimeToSubtract(getCrashManager().getTimeToSubtract() + 0.05);

            // CHECKS IF THE CRASHED TIME HAS REACHED THE CONFIG TIME, IF SO IT CHANGES ALL THE VALUES AND RESETS THE MANAGER
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

                // SETS THE INVENTORY GLASS TO THE STARTING COLOR
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

                // UPDATES THE BETTORS GUI FOR WHOEVER HAS IT OPEN
                createCrashGamblersGui();
            }
        }

        // RUNS EVERYTHING WHEN THE CRASH HAS STARTED
        if(!getCrashManager().isStarting() && !getCrashManager().isCrashed()){

            // CHECKS WHETHER IT CRASHED OR NOT
            if(getCrashManager().crash()){
                getCrashManager().setCrashed(true);

                // LOOPS THROUGH ALL THE INVENTORIES OF PEOPLE WHO ARE STILL PLAYING
                for (Map.Entry<String, Inventory> entry : getCrashManager().getCrashInventories().entrySet()) {
                    for (Map.Entry<Integer, ItemStack> glass : getCrashManager().getCrashedGlass().entrySet()) {
                        entry.getValue().setItem(glass.getKey(), glass.getValue());
                    }

                    Player target = Bukkit.getPlayer(entry.getKey());
                    if(target != null) {
                        BlazeUser user = getManager().getBlazeUser(entry.getKey());

                        entry.getValue().setItem(getCrashManager().getCrashCrashedSlot(), getCrashManager().getCrashCrashedItem(user));

                        target.updateInventory();

                        // CHECKS IF THE PLAYER BET
                        if (getCrashManager().getCrashBets().containsKey(entry.getKey())) {
                            CrashBet crashBet = getCrashManager().getCrashBets().get(entry.getKey());

                            // CHECKS IF THE PLAYER HAS ALREADY LEFT THE CRASH
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

                // ADDS UP THE TIME VALUES
                getCrashManager().setDuration(getCrashManager().getDuration() + 0.05);

                getCrashManager().setCrashValue(getCrashManager().getCrashValue() + getCrashManager().getSumRoundValue());
            }
        }

        //
        // MANAGING THE GRAPH
        //

        if(graphInterval >= 5) {
            graphInterval = 0;

            // DEFINING WHETHER THE GRAPH WILL GO UP OR DOWN
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

            // CHECKING THE CURRENT BETS AND WHICH PLAYERS WON, THEN REMOVING THE ALREADY WON BETS FROM THE LIST
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

            // REMOVING THE WON/LOST/DRAWN BETS
            getGraphManager().getGraphBets().removeAll(remove);

            if(!remove.isEmpty()) createGraphGamblersGui();

            // UPDATING THE TOTAL CASH/MONEY BET ACCORDING TO THE REMOVAL OF OLD BETS IN THE PREVIOUS ITEM
            if (change) {
                getGraphManager().updateTotalBetMoney();
                getGraphManager().updateTotalBetCash();

                getGraphManager().updateNegativeBetAverage();
                getGraphManager().updatePositiveBetAverage();

                // RECREATING THE BETTORS INVENTORY IN THE GRAPH
                createGraphGamblersGui();
            }

            // REVERSING THE LIST SO THE LAST GRAPHPOINT ADDED IS THE '0' OF THE LIST
            List<GraphPoint> reversed = new ArrayList<>(getGraphManager().getGraphPoints());
            Collections.reverse(reversed);

            for (Map.Entry<String, Inventory> entry : getGraphManager().getGraphInventories().entrySet()) {
                Inventory inv = entry.getValue();

                // CLEARING THE OLD GRAPH FROM ALL INVENTORIES
                getGraphManager().clearGraphic(inv);

                Inventory copy = MenuHolder.create(MenuType.GRAPH, 6 * 9, getGraphManager().getMenuTitle());

                copy.setContents(inv.getContents());

                // CREATING THE NEW GRAPH MODEL BASED ON THE LAST GRAPH POINT ADDED TO THE LIST
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

                // CLEARING THE OLD INVENTORY AND SETTING THE NEW ONE
                inv.clear();
                inv.setContents(copy.getContents());

                // CLEARING ANY DUPLICATED SLOT THE GRAPH GENERATED
                for (Integer duplicatedSlot : getGraphManager().getDuplicatedSlots(inv)) {
                    inv.setItem(duplicatedSlot, null);
                }

                // SETTING THE HISTORY SLOT, WHICH IS THE ONE THAT CHANGES EVERY SECOND
                inv.setItem(getGraphManager().getHistoricSlot(), getGraphManager().getHistoricItem());
                if (change) {
                    BlazeUser user = getManager().getBlazeUser(entry.getKey());
                    entry.getValue().setItem(getGraphManager().getGamblersSlot(), getGraphManager().getGamblersItem());
                    entry.getValue().setItem(getGraphManager().getBetUpSlot(), getGraphManager().getBetUpItem(user));
                    entry.getValue().setItem(getGraphManager().getBetDecreaseSlot(), getGraphManager().getBetDecreaseItem(user));
                }

                // UPDATING THE PLAYERS' INVENTORY
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) player.updateInventory();
            }

            // KEEPING ONLY 30 POINTS IN THE LIST
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
