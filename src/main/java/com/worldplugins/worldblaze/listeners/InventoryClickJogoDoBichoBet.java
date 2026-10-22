package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.enums.Bicho;
import com.worldplugins.worldblaze.guis.*;
import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.objects.JogoDoBichoBet;
import com.worldplugins.worldblaze.utils.NBTTag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class InventoryClickJogoDoBichoBet implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!e.getInventory().getName().equalsIgnoreCase(c(getBichosBetGuiYml().getConfig().getString("title")))) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getBichosBetGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        BlazeUser user = getManager().getBlazeUser(p.getName());

        String nbtAnimal = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_animal");

        if(nbtAnimal == null) return;

        Bicho bicho;
        try {
            bicho = Bicho.valueOf(nbtAnimal.toUpperCase());
        }catch (IllegalArgumentException ex){
            p.sendMessage("§cError, contact Miinero ("+nbtAnimal+")");
            return;
        }

        if(e.getSlot() == getConfig.getInt("items.money.slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("messages.jogo-do-bicho-typing-money").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.jogoDoBichoMoneyList.put(p.getName(), bicho);
        }

        if(e.getSlot() == getConfig.getInt("items.cash.slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("messages.jogo-do-bicho-typing-cash").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.jogoDoBichoCashList.put(p.getName(), bicho);
        }

        if(e.getSlot() == getConfig.getInt("items.bettors.slot")){

            InventoryClose.bypassClose.add(p.getName());

            getJogoDoBichoManager().getSeeingMap().put(p.getName(), e.getClickedInventory());

            JogoDoBichoGamblersGui.create(p, bicho);

            InventoryClose.bypassClose.remove(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("items.back.slot")) JogoDoBichoBichosGui.create(p);

        if(e.getSlot() == getConfig.getInt("items.bet.slot")){

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if(getInstance().isLocked()){
                p.sendMessage(getMessages().GAMES_UNAVAILABLE);
                return;
            }

            if(getJogoDoBichoManager().isPlayerBet(p.getName(), bicho)){
                p.sendMessage(getMessages().ALREADY_BET_ON_ANIMAL);
                return;
            }

            if(user.getJogoDoBichoMoneyValue() < getInstance().getConfig().getDouble("config.jogo-do-bicho.min-money-bet")){
                p.sendMessage(getMessages().MINIMUM_MONEY);
                return;
            }

            if(user.getJogoDoBichoCashValue() < getInstance().getConfig().getDouble("config.jogo-do-bicho.min-cash-bet")){
                p.sendMessage(getMessages().MINIMUM_CASH);
                return;
            }

            if(user.getJogoDoBichoMoneyValue() > getInstance().getConfig().getDouble("config.jogo-do-bicho.max-money-bet")){
                p.sendMessage(getMessages().MAXIMUM_MONEY);
                return;
            }

            if(user.getJogoDoBichoCashValue() > getInstance().getConfig().getDouble("config.jogo-do-bicho.max-cash-bet")){
                p.sendMessage(getMessages().MAXIMUM_CASH);
                return;
            }

            if(!checkValues(p, user.getJogoDoBichoCashValue(), user.getJogoDoBichoMoneyValue())) return;

            JogoDoBichoBet jogoDoBichoBet = new JogoDoBichoBet(p.getName(), getNewBetId() + "", bicho, user.getJogoDoBichoCashValue(), user.getJogoDoBichoMoneyValue());

            getJogoDoBichoManager().getJogoDoBichoBets().add(jogoDoBichoBet);

            removeValues(p, user.getJogoDoBichoCashValue(), user.getJogoDoBichoMoneyValue());

            p.closeInventory();

            getInstance().getConfig().getStringList("messages.bet-placed-animal").forEach(w -> p.sendMessage(c(w.replace("{animal}", bicho.getNameStr()))));
        }

    }
}
