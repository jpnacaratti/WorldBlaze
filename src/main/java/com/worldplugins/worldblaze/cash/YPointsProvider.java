package com.worldplugins.worldblaze.cash;

import com.ystoreplugins.ypoints.api.yPointsAPI;
import com.ystoreplugins.ypoints.models.PlayerPoints;
import org.bukkit.Bukkit;

import java.util.UUID;

public class YPointsProvider implements CashProvider {

    private final yPointsAPI api = yPointsAPI.ypointsapi != null ? yPointsAPI.ypointsapi : new yPointsAPI();

    @Override
    public String getName() {
        return "yPoints";
    }

    @Override
    public boolean has(UUID uuid, double amount) {
        PlayerPoints points = find(uuid);
        return points != null && api.getPoints(points) >= amount;
    }

    @Override
    public void withdraw(UUID uuid, double amount) {
        PlayerPoints points = find(uuid);
        if (points == null) {
            warn(uuid);
            return;
        }
        api.removePoints(points, amount);
    }

    @Override
    public void deposit(UUID uuid, double amount) {
        PlayerPoints points = find(uuid);
        if (points == null) {
            warn(uuid);
            return;
        }
        api.addPoints(points, amount);
    }

    // yPoints identifies players by name instead of UUID
    private PlayerPoints find(UUID uuid) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name == null ? null : api.getPlayer(name);
    }

    private void warn(UUID uuid) {
        Bukkit.getConsoleSender().sendMessage("[WorldBlaze] Could not find the yPoints data of " + uuid + ", cash was not updated");
    }

}
