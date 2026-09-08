package com.worldplugins.worldblaze.objects;

public class CrashBet {

    private final String owner, betId;
    private final double cashValue, moneyValue, mid;
    private double leftValue;
    private boolean left;

    public CrashBet(String owner, String betId, double cashValue, double moneyValue) {
        this.owner = owner;
        this.betId = betId;
        this.cashValue = cashValue;
        this.moneyValue = moneyValue;
        this.mid = (cashValue + moneyValue) / 2;
    }

    public String getOwner() {
        return owner;
    }

    public String getBetId() {
        return betId;
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

    public double getLeftValue() {
        return leftValue;
    }

    public void setLeftValue(double leftValue) {
        this.leftValue = leftValue;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }
}
