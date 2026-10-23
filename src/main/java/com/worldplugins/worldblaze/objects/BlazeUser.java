package com.worldplugins.worldblaze.objects;

import org.bukkit.inventory.ItemStack;

import static com.worldplugins.worldblaze.WorldBlaze.*;

public class BlazeUser {

    private final String owner;
    private double graphCashValue, graphMoneyValue, crashCashValue, crashMoneyValue, doubleCashValue, doubleMoneyValue, mineCashValue, mineMoneyValue, jogoDoBichoMoneyValue, jogoDoBichoCashValue, coinFlipCashValue, coinFlipMoneyValue;
    private int graphTime, mineBombs;
    private String coinFlipSpecific;
    private boolean coinFlipHeads;

    public BlazeUser(String owner) {
        this.owner = owner;
        this.graphTime = getInstance().getConfig().getInt("config.graph.default-time-update");
        this.mineBombs = getInstance().getConfig().getInt("config.mine.default-mines");
        this.coinFlipHeads = true;
    }

    public String getOwner() {
        return owner;
    }

    public double getGraphCashValue() {
        return graphCashValue;
    }

    public void setGraphCashValue(double graphCashValue) {
        this.graphCashValue = graphCashValue;
    }

    public double getGraphMoneyValue() {
        return graphMoneyValue;
    }

    public void setGraphMoneyValue(double graphMoneyValue) {
        this.graphMoneyValue = graphMoneyValue;
    }

    public int getGraphTime() {
        return graphTime;
    }

    public void setGraphTime(int graphTime) {
        this.graphTime = graphTime;
    }

    public double getCrashCashValue() {
        return crashCashValue;
    }

    public void setCrashCashValue(double crashCashValue) {
        this.crashCashValue = crashCashValue;
    }

    public double getCrashMoneyValue() {
        return crashMoneyValue;
    }

    public void setCrashMoneyValue(double crashMoneyValue) {
        this.crashMoneyValue = crashMoneyValue;
    }

    public double getDoubleCashValue() {
        return doubleCashValue;
    }

    public void setDoubleCashValue(double doubleCashValue) {
        this.doubleCashValue = doubleCashValue;
    }

    public double getDoubleMoneyValue() {
        return doubleMoneyValue;
    }

    public void setDoubleMoneyValue(double doubleMoneyValue) {
        this.doubleMoneyValue = doubleMoneyValue;
    }

    public double getMineCashValue() {
        return mineCashValue;
    }

    public void setMineCashValue(double mineCashValue) {
        this.mineCashValue = mineCashValue;
    }

    public double getMineMoneyValue() {
        return mineMoneyValue;
    }

    public void setMineMoneyValue(double mineMoneyValue) {
        this.mineMoneyValue = mineMoneyValue;
    }

    public int getMineBombs() {
        return mineBombs;
    }

    public void setMineBombs(int mineBombs) {
        this.mineBombs = mineBombs;
    }

    public double getJogoDoBichoMoneyValue() {
        return jogoDoBichoMoneyValue;
    }

    public void setJogoDoBichoMoneyValue(double jogoDoBichoMoneyValue) {
        this.jogoDoBichoMoneyValue = jogoDoBichoMoneyValue;
    }

    public double getJogoDoBichoCashValue() {
        return jogoDoBichoCashValue;
    }

    public void setJogoDoBichoCashValue(double jogoDoBichoCashValue) {
        this.jogoDoBichoCashValue = jogoDoBichoCashValue;
    }

    public double getCoinFlipCashValue() {
        return coinFlipCashValue;
    }

    public void setCoinFlipCashValue(double coinFlipCashValue) {
        this.coinFlipCashValue = coinFlipCashValue;
    }

    public double getCoinFlipMoneyValue() {
        return coinFlipMoneyValue;
    }

    public void setCoinFlipMoneyValue(double coinFlipMoneyValue) {
        this.coinFlipMoneyValue = coinFlipMoneyValue;
    }

    public String getCoinFlipSpecific() {
        return coinFlipSpecific;
    }

    public void setCoinFlipSpecific(String coinFlipSpecific) {
        this.coinFlipSpecific = coinFlipSpecific;
    }

    public boolean isCoinFlipHeads() {
        return coinFlipHeads;
    }

    public void setCoinFlipHeads(boolean coinFlipHeads) {
        this.coinFlipHeads = coinFlipHeads;
    }
}
