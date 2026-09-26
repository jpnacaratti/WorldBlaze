package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.MineCreateGui;
import com.worldplugins.worldblaze.guis.MinePlayersGui;
import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.objects.MineBet;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.WorldBlaze.getInstance;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class InventoryClickMineCreate implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!e.getInventory().getName().equalsIgnoreCase(c(getCreateMineGuiYml().getConfig().getString("Titulo")))) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getCreateMineGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        BlazeUser user = getManager().getBlazeUser(p.getName());

        if(e.getSlot() == getConfig.getInt("Itens.money.Slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("Mensagens.Mine-digitando-money").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.mineMoneyList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.cash.Slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("Mensagens.Mine-digitando-cash").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.mineCashList.add(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("Itens.aumentar-bombas.Slot")){

            if(e.isRightClick()){
                int nValue = user.getMineBombs() - 1;

                if(getInstance().getConfig().getInt("Config.Mine.Minimo-bombas") > nValue){
                    p.sendMessage(getMessages().CANNOT_DECREASE_MINES);
                    return;
                }

                user.setMineBombs(nValue);
            }else{
                int nValue = user.getMineBombs() + 1;

                if(getInstance().getConfig().getInt("Config.Mine.Maximo-bombas") < nValue){
                    p.sendMessage(getMessages().CANNOT_INCREASE_MINES);
                    return;
                }

                user.setMineBombs(nValue);
            }

            MineCreateGui.create(p);
        }

        if(e.getSlot() == getConfig.getInt("Itens.iniciar.Slot")){

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if(getInstance().isLocked()){
                p.sendMessage(getMessages().GAMES_UNAVAILABLE);
                return;
            }

            if(getMineManager().isPlayerInGame(p.getName())){
                p.closeInventory();
                p.sendMessage("§cHouve um erro ao iniciar seu jogo, aparentemente você já está em uma partida, contate um STAFF!");
                return;
            }

            if(user.getMineMoneyValue() < getInstance().getConfig().getDouble("Config.Mine.Min-money-aposta")){
                p.sendMessage(getMessages().MINIMUM_MONEY);
                return;
            }

            if(user.getMineCashValue() < getInstance().getConfig().getDouble("Config.Mine.Min-cash-aposta")){
                p.sendMessage(getMessages().MINIMUM_CASH);
                return;
            }

            if(user.getMineMoneyValue() > getInstance().getConfig().getDouble("Config.Mine.Max-money-aposta")){
                p.sendMessage(getMessages().MAXIMUM_MONEY);
                return;
            }

            if(user.getMineCashValue() > getInstance().getConfig().getDouble("Config.Mine.Max-cash-aposta")){
                p.sendMessage(getMessages().MAXIMUM_CASH);
                return;
            }

            if(!checkValues(p, user.getMineCashValue(), user.getMineMoneyValue())) return;

            MineBet mineBet = new MineBet(p.getName(), getNewBetId() + "", user.getMineCashValue(), user.getMineMoneyValue(), user.getMineBombs());

            getMineManager().getMineBets().add(mineBet);

            getMineManager().setTotalBetMoney();
            getMineManager().setTotalBetCash();

            for (Map.Entry<String, Inventory> entry : getMineManager().getPrincipalInventories().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                entry.getValue().setItem(getMineManager().getPrincipalPlayersSlot(), getMineManager().getPrincipalPlayersItem());

                if(player != null) player.updateInventory();
            }

            for (String s : getMineManager().getPlayersGuiOpen()) {
                Player player = Bukkit.getPlayer(s);

                if(player != null) MinePlayersGui.create(player);
            }

            removeValues(p, user.getMineCashValue(), user.getMineMoneyValue());

            p.openInventory(mineBet.getGameInventory());
        }

    }
}
