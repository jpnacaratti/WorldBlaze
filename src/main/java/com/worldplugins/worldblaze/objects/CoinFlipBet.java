package com.worldplugins.worldblaze.objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Random;

import static com.worldplugins.worldblaze.WorldBlaze.getInstance;

public class CoinFlipBet {

    private final String owner, betId, specific;
    private final boolean cara;
    private String opponent;
    private Inventory gameInventory;
    private final double cashValue, moneyValue, mid;
    private boolean starting, finished, choiceCara;
    private double subtract;
    private int ticks;
    private final Random rand;

    public CoinFlipBet(String owner, String betId, double cashValue, double moneyValue, String specific, boolean cara) {
        this.owner = owner;
        this.betId = betId;
        this.cashValue = cashValue;
        this.moneyValue = moneyValue;
        this.mid = (moneyValue + cashValue) / 2;
        this.opponent = null;
        this.specific = specific;
        this.rand = new Random();

        this.cara = cara;
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

    public void setGameInventory(Inventory gameInventory) {
        this.gameInventory = gameInventory;
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

    public String getOpponent() {
        return opponent;
    }

    public String getSpecific() {
        return specific;
    }

    public boolean isCara() {
        return cara;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public boolean isStarting() {
        return starting;
    }

    public void setStarting(boolean starting) {
        this.starting = starting;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void updateInventories(){
        Bukkit.getScheduler().runTask(getInstance(), ()->{
            Player player = Bukkit.getPlayer(owner);
            Player opponent = Bukkit.getPlayer(this.opponent);

            player.updateInventory();
            opponent.updateInventory();
        });
    }

    public double getSubtract() {
        return subtract;
    }

    public void setSubtract(double subtract) {
        this.subtract = subtract;
    }

    public int getTicks() {
        return ticks;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    public boolean isChoiceCara() {
        return choiceCara;
    }

    public void setChoiceCara(boolean choiceCara) {
        this.choiceCara = choiceCara;
    }

    public boolean getFinalCoinFlip(){
        return rand.nextBoolean();
    }
}
