package com.worldplugins.worldblaze.objects;

public class CrashChance {

    private final double chance, crashStart, crashFinal;

    public CrashChance(double chance, double crashStart, double crashFinal) {
        this.chance = chance;
        this.crashStart = crashStart;
        this.crashFinal = crashFinal;
    }

    public double getChance() {
        return chance;
    }

    public double getCrashStart() {
        return crashStart;
    }

    public double getCrashFinal() {
        return crashFinal;
    }
}
