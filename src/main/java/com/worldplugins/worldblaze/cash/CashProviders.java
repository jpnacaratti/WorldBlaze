package com.worldplugins.worldblaze.cash;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class CashProviders {

    private CashProviders() {
    }

    public static CashProvider detect(String setting) {
        String choice = setting == null ? "auto" : setting.trim().toLowerCase();

        CashProvider provider;
        switch (choice) {
            case "none":
                provider = null;
                break;
            case "grandecash":
                provider = grandeCash();
                if (provider == null) log("GrandeCash is not installed or enabled, cash bets are disabled");
                break;
            case "playerpoints":
                provider = playerPoints();
                if (provider == null) log("PlayerPoints is not installed or enabled, cash bets are disabled");
                break;
            default:
                provider = grandeCash();
                if (provider == null) provider = playerPoints();
                if (provider == null) log("No supported cash provider found, cash bets are disabled");
                break;
        }

        if (provider == null) return new NoCashProvider();

        log("Cash provider: " + provider.getName());
        return provider;
    }

    private static CashProvider grandeCash() {
        if (!isEnabled("GrandeCash")) return null;
        return new GrandeCashProvider();
    }

    private static CashProvider playerPoints() {
        if (!isEnabled("PlayerPoints")) return null;
        return new PlayerPointsProvider();
    }

    private static boolean isEnabled(String name) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        return plugin != null && plugin.isEnabled();
    }

    private static void log(String message) {
        Bukkit.getConsoleSender().sendMessage("[WorldBlaze] " + message);
    }

}
