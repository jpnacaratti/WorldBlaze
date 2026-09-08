package com.worldplugins.worldblaze.objects;

import java.util.UUID;

public class GraphBet {

    private final String owner, betId;
    private final double cashValue, moneyValue, graphValue, mid;
    private final int graphPointId, numberAtts, whenReceive;
    private final boolean positive;

    public GraphBet(String owner, double cashValue, double moneyValue, double graphValue, int graphPointId, int numberAtts, boolean positive, int betID) {
        this.owner = owner;
        this.cashValue = cashValue;
        this.moneyValue = moneyValue;
        this.graphValue = graphValue;
        this.graphPointId = graphPointId;
        this.numberAtts = numberAtts;
        this.positive = positive;
        this.betId = betID+"";
        this.whenReceive = numberAtts + graphPointId;
        this.mid = (cashValue + moneyValue) / 2;
    }

    public String getOwner() {
        return owner;
    }

    public String getBetId() {
        return betId;
    }

    public double getMid() {
        return mid;
    }

    public double getCashValue() {
        return cashValue;
    }

    public double getMoneyValue() {
        return moneyValue;
    }

    public double getGraphValue() {
        return graphValue;
    }

    public int getGraphPointId() {
        return graphPointId;
    }

    public int getNumberAtts() {
        return numberAtts;
    }

    public boolean isPositive() {
        return positive;
    }

    public int getWhenReceive() {
        return whenReceive;
    }
}
