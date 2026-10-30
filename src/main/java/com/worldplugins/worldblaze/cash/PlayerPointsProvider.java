package com.worldplugins.worldblaze.cash;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;

import java.util.UUID;

public class PlayerPointsProvider implements CashProvider {

    private final PlayerPointsAPI api = PlayerPoints.getInstance().getAPI();

    @Override
    public String getName() {
        return "PlayerPoints";
    }

    @Override
    public boolean has(UUID uuid, double amount) {
        return api.look(uuid) >= toPoints(amount);
    }

    @Override
    public void withdraw(UUID uuid, double amount) {
        api.take(uuid, toPoints(amount));
    }

    @Override
    public void deposit(UUID uuid, double amount) {
        api.give(uuid, toPoints(amount));
    }

    // PlayerPoints only stores whole points
    private int toPoints(double amount) {
        return (int) Math.round(amount);
    }

}
