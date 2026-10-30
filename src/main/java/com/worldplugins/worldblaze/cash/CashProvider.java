package com.worldplugins.worldblaze.cash;

import java.util.UUID;

public interface CashProvider {

    String getName();

    boolean has(UUID uuid, double amount);

    void withdraw(UUID uuid, double amount);

    void deposit(UUID uuid, double amount);

    default boolean isEnabled() {
        return true;
    }

}
