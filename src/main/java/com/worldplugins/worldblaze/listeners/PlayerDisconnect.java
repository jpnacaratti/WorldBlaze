package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.objects.CoinFlipBet;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;

public class PlayerDisconnect implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e){

        Player p = e.getPlayer();

        getJogoDoBichoManager().getSeeingMap().remove(p.getName());

        getGraphManager().getGamblersGuiOpen().remove(p.getName());
        getCrashManager().getGamblersGuiOpen().remove(p.getName());
        getDoubleManager().getGamblersGuiOpen().remove(p.getName());
        getMineManager().getPlayersGuiOpen().remove(p.getName());
        getCoinFlipManager().getCoinFlipGamblersGuiOpen().remove(p.getName());
        getGraphManager().getGraphInventories().remove(p.getName());
        getCrashManager().getCrashInventories().remove(p.getName());
        getDoubleManager().getDoubleInventories().remove(p.getName());
        getMineManager().getPrincipalInventories().remove(p.getName());
        getCoinFlipManager().getPrincipalInventories().remove(p.getName());
        AsyncPlayerChat.graphCashList.remove(p.getName());
        AsyncPlayerChat.graphMoneyList.remove(p.getName());
        AsyncPlayerChat.crashCashList.remove(p.getName());
        AsyncPlayerChat.crashMoneyList.remove(p.getName());
        AsyncPlayerChat.doubleCashList.remove(p.getName());
        AsyncPlayerChat.doubleMoneyList.remove(p.getName());
        AsyncPlayerChat.mineMoneyList.remove(p.getName());
        AsyncPlayerChat.mineCashList.remove(p.getName());
        AsyncPlayerChat.jogoDoBichoMoneyList.remove(p.getName());
        AsyncPlayerChat.jogoDoBichoCashList.remove(p.getName());
        AsyncPlayerChat.coinFlipMoneyList.remove(p.getName());
        AsyncPlayerChat.coinFlipCashList.remove(p.getName());
        AsyncPlayerChat.coinFlipOpponentList.remove(p.getName());

        CoinFlipBet bet = getCoinFlipManager().getCoinFlipBet(p.getName());
        if(bet != null) getCoinFlipManager().getCoinFlipBets().remove(bet);
    }

}
