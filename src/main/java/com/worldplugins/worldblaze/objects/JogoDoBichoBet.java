package com.worldplugins.worldblaze.objects;

import com.worldplugins.worldblaze.enums.Bicho;
import com.worldplugins.worldblaze.enums.DoubleColor;

public class JogoDoBichoBet {

    private final String owner, betId;
    private final Bicho animal;
    private final double cashValue, moneyValue, mid;

    public JogoDoBichoBet(String owner, String betId, Bicho animal, double cashValue, double moneyValue) {
        this.owner = owner;
        this.betId = betId;
        this.animal = animal;
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

    public Bicho getAnimal() {
        return animal;
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
