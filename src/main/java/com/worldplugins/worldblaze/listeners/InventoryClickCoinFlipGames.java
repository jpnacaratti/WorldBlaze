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

        if(!e.getInventory().getName().equalsIgnoreCase(c(getCoinFlipGamesGuiYml().getConfig().getString("title")))) return;
        e.setCancelled(true);

        if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        Player p = (Player) e.getWhoClicked();

        String nbtPag = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_coinflipPag");
        String coinflipPlayer = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_coinflipJogador");

        if(nbtPag != null){
            p.openInventory(CoinFlipGamblersGui.invs.get(p).get(Integer.parseInt(nbtPag)));
            return;
        }

        FileConfiguration getConfig = getCoinFlipGamesGuiYml().getConfig();

        if(e.getSlot() == getConfig.getInt("items.back.slot")){

            getCoinFlipManager().getCoinFlipGamblersGuiOpen().remove(p.getName());

            if(getCoinFlipManager().getPrincipalInventories().containsKey(p.getName())) p.openInventory(getCoinFlipManager().getPrincipalInventories().get(p.getName()));
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
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aClique para ver!").create()));

        owner.spigot().sendMessage(textComponent);

        //owner.sendMessage(getMessages().CHALLENGER_BET_ACCEPTED.replace("{player}", owner.getName()));

        getCoinFlipManager().updateGamblersInventories();
        getCoinFlipManager().updatePrincipalInventories();
    }

}
