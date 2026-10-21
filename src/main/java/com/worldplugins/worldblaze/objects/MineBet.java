package com.worldplugins.worldblaze.objects;

import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static com.worldplugins.worldblaze.WorldBlaze.getInstance;
import static com.worldplugins.worldblaze.WorldBlaze.getMineManager;
import static com.worldplugins.worldblaze.utils.Utils.sendDebugMessage;

public class MineBet {

    private final String owner, betId;
    private final Inventory gameInventory;
    private final double cashValue, moneyValue, mid, multiplierSum;
    private final int numBombs;
    private final List<Integer> clickedSlots, bombSlots;
    private final Set<String> spectators;

    private double multiplier;
    private int chanceMultiplier;

    public MineBet(String owner, String betId, double cashValue, double moneyValue, int numBombs) {
        this.owner = owner;
        this.betId = betId;
        this.cashValue = cashValue;
        this.moneyValue = moneyValue;
        this.numBombs = numBombs;
        this.mid = (moneyValue + cashValue) / 2;
        this.multiplierSum = getMineManager().getMultipliers().get(numBombs) / (getInstance().getConfig().getIntegerList("config.mine.game-slots").size() - numBombs);

        this.clickedSlots = new ArrayList<>();
        this.bombSlots = new ArrayList<>();
        this.spectators = new HashSet<>();

        this.multiplier = 1.0;

        for (int i = 0; i < numBombs - 1; i++) {
            getMineManager().addSlots(bombSlots);
        }

        this.chanceMultiplier = 1;

        double dopChance = getMineManager().getDopChance(this);
        if(dopChance > 0){
            if (ThreadLocalRandom.current().nextDouble(0, 100) <= dopChance) {
                this.chanceMultiplier = getMineManager().getDopChanceMultiplier();
                sendDebugMessage("[Mine] Foi pego na dopagem...");
            }
        }



        this.gameInventory = getMineManager().createMineInventory(this);
    }

    public String getOwner() {
        return owner;
    }

    public String getBetId() {
        return betId;
    }

    public Inventory getGameInventory() {
        return gameInventory;
    }

    public double getCashValue() {
        return cashValue;
    }

    public double getMoneyValue() {
        return moneyValue;
    }

    public double getMid() {
        return mid;
    }

    public List<Integer> getClickedSlots() {
        return clickedSlots;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public Set<String> getSpectators() {
        return spectators;
    }

    public int getNumBombs() {
        return numBombs;
    }

    public double getMultiplierSum() {
        return multiplierSum;
    }

    public List<Integer> getBombSlots() {
        return bombSlots;
    }

    public int getChanceMultiplier() {
        return chanceMultiplier;
    }
}
