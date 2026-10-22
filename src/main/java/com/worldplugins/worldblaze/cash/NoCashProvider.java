package com.worldplugins.worldblaze.cash;

import java.util.UUID;

public class NoCashProvider implements CashProvider {

    @Override
    public String getName() {
        return "None";
    }

    @Override
    public boolean has(UUID uuid, double amount) {
        return false;
    }

    @Override
    public void withdraw(UUID uuid, double amount) {
    }

    @Override
    public void deposit(UUID uuid, double amount) {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

}
