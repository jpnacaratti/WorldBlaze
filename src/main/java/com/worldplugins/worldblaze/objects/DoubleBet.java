package com.worldplugins.worldblaze.objects;

import com.worldplugins.worldblaze.enums.DoubleColor;

public class DoubleBet {

    private final String owner, betId;
    private final DoubleColor color;
    private final double cashValue, moneyValue, mid;

    public DoubleBet(String owner, String betId, DoubleColor color, double cashValue, double moneyValue) {
        this.owner = owner;
        this.betId = betId;
        this.color = color;
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

    public DoubleColor getColor() {
        return color;
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
}
