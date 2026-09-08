package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.getInstance;
import static com.worldplugins.worldblaze.WorldBlaze.getPrincipalGuiYml;
import static com.worldplugins.worldblaze.utils.Utils.c;

public class InventoryClickMain implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!e.getInventory().getName().equalsIgnoreCase(c(getPrincipalGuiYml().getConfig().getString("Titulo"))))
            return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getPrincipalGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == getConfig.getInt("Itens.graph.Slot")) {

            if (!p.hasPermission("worldblaze.graph")) {
                p.sendMessage("§cVocê não tem permissão para fazer isso.");
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage("§cO plugin será atualizado e portanto não é possível jogar nesse modo agora! Aguarde alguns segundos...");
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("Itens.graph.Help")));
            } else GraphGui.create(p);

        }

        if (e.getSlot() == getConfig.getInt("Itens.crash.Slot")) {

            if (!p.hasPermission("worldblaze.crash")) {
                p.sendMessage("§cVocê não tem permissão para fazer isso.");
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage("§cO plugin será atualizado e portanto não é possível jogar nesse modo agora! Aguarde alguns segundos...");
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("Itens.crash.Help")));
            } else CrashGui.create(p);

        }

        if (e.getSlot() == getConfig.getInt("Itens.double.Slot")) {

            if (!p.hasPermission("worldblaze.double")) {
                p.sendMessage("§cVocê não tem permissão para fazer isso.");
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage("§cO plugin será atualizado e portanto não é possível jogar nesse modo agora! Aguarde alguns segundos...");
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("Itens.double.Help")));
            } else DoubleGui.create(p);

        }

        if (e.getSlot() == getConfig.getInt("Itens.mine.Slot")) {

            if (!p.hasPermission("worldblaze.mine")) {
                p.sendMessage("§cVocê não tem permissão para fazer isso.");
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage("§cO plugin será atualizado e portanto não é possível jogar nesse modo agora! Aguarde alguns segundos...");
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("Itens.mine.Help")));
            } else MineMainGui.create(p);

        }

        if (e.getSlot() == getConfig.getInt("Itens.jogo-do-bicho.Slot")) {

            if (!p.hasPermission("worldblaze.jogodobicho")) {
                p.sendMessage("§cVocê não tem permissão para fazer isso.");
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage("§cO plugin será atualizado e portanto não é possível jogar nesse modo agora! Aguarde alguns segundos...");
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("Itens.jogo-do-bicho.Help")));
            } else JogoDoBichoMainGui.create(p);

        }

        if (e.getSlot() == getConfig.getInt("Itens.coinflip.Slot")) {

            if (!p.hasPermission("worldblaze.coinflip")) {
                p.sendMessage("§cVocê não tem permissão para fazer isso.");
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage("§cO plugin será atualizado e portanto não é possível jogar nesse modo agora! Aguarde alguns segundos...");
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("Itens.coinflip.Help")));
            } else CoinFlipMainGui.create(p);

        }

    }
}