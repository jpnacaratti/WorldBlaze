package com.worldplugins.worldblaze.cash;

import com.grandemc.cash.GrandeCash;
import com.grandemc.cash.api.GrandeCashAPI;

import java.util.UUID;

public class GrandeCashProvider implements CashProvider {

    private final GrandeCashAPI api = GrandeCash.api();

    @Override
    public String getName() {
        return "GrandeCash";
    }

    @Override
    public boolean has(UUID uuid, double amount) {
        return api.has(uuid, amount);
    }

    @Override
    public void withdraw(UUID uuid, double amount) {
        api.withdraw(uuid, amount);
    }

    @Override
    public void deposit(UUID uuid, double amount) {
        api.deposit(uuid, amount);
    }

}
