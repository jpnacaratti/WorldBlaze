package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.*;
import com.worldplugins.worldblaze.objects.CoinFlipBet;
import com.worldplugins.worldblaze.utils.NBTTag;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class InventoryClickCoinFlipGames implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if (!MenuHolder.is(e.getInventory(), MenuType.COINFLIP_GAMES)) return;
        e.setCancelled(true);
        if (e.getRawSlot() >= e.getInventory().getSize()) return;

        if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        Player p = (Player) e.getWhoClicked();

        String nbtPage = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_coinflipPag");
        String coinflipPlayer = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_coinflipJogador");

        if(nbtPage != null){
            p.openInventory(CoinFlipGamblersGui.invs.get(p).get(Integer.parseInt(nbtPage)));
            return;
        }

        FileConfiguration getConfig = getCoinFlipGamesGuiYml().getConfig();

        if(e.getSlot() == getConfig.getInt("items.back.slot")){

            getCoinFlipManager().getCoinFlipGamblersGuiOpen().remove(p.getName());

            if(getCoinFlipManager().getMainInventories().containsKey(p.getName())) p.openInventory(getCoinFlipManager().getMainInventories().get(p.getName()));
            else CoinFlipMainGui.create(p);

        }

        if(coinflipPlayer == null) return;

        CoinFlipBet coinFlipBet = getCoinFlipManager().getCoinFlipBet(coinflipPlayer);

        if(coinFlipBet.getOwner().equalsIgnoreCase(p.getName())){
            p.sendMessage(getMessages().JOINED_OWN_GAME);
            return;
        }

        if(coinFlipBet.getOpponent() != null){
            p.sendMessage(getMessages().GAME_ALREADY_STARTED);
            return;
        }

        if(coinFlipBet.getSpecific() != null && !coinFlipBet.getSpecific().equalsIgnoreCase(p.getName())){
            p.sendMessage(getMessages().INVALID_GAME);
            return;
        }

        if(!checkValues(p, coinFlipBet.getCashValue(), coinFlipBet.getMoneyValue())) return;

        Player owner = Bukkit.getPlayer(coinFlipBet.getOwner());
        if(owner == null){
            p.sendMessage(getMessages().COIN_FLIP_OWNER_OFFLINE);
            return;
        }

        removeValues(p, coinFlipBet.getCashValue(), coinFlipBet.getMoneyValue());

        coinFlipBet.setOpponent(p.getName());

        coinFlipBet.setGameInventory(CoinFlipGui.create(coinFlipBet));

        coinFlipBet.setStarting(true);

        p.openInventory(coinFlipBet.getGameInventory());
        //owner.openInventory(coinFlipBet.getGameInventory());

        p.sendMessage(getMessages().CHALLENGED_PLAYER_ACCEPTED_BET.replace("{player}", owner.getName()));

        TextComponent textComponent = new TextComponent(c(getMessages().CHALLENGER_BET_ACCEPTED.replace("{player}", p.getName())));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/verx1coinflip"));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aClick to view!").create()));

        owner.spigot().sendMessage(textComponent);

        //owner.sendMessage(getMessages().CHALLENGER_BET_ACCEPTED.replace("{player}", owner.getName()));

        getCoinFlipManager().updateGamblersInventories();
        getCoinFlipManager().updateMainInventories();
    }

}
