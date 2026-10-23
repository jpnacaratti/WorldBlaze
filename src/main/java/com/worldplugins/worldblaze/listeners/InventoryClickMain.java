package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.guis.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.getInstance;
import static com.worldplugins.worldblaze.WorldBlaze.getMessages;
import static com.worldplugins.worldblaze.WorldBlaze.getMainGuiYml;
import static com.worldplugins.worldblaze.utils.Utils.c;

public class InventoryClickMain implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!MenuHolder.is(e.getInventory(), MenuType.MAIN)) return;
        e.setCancelled(true);
        if (e.getRawSlot() >= e.getInventory().getSize()) return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getMainGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        if (e.getSlot() == getConfig.getInt("items.graph.slot")) {

            if (!p.hasPermission("worldblaze.graph")) {
                p.sendMessage(getMessages().NO_PERMISSION);
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("items.graph.help")));
            } else GraphGui.create(p);

        }

        if (e.getSlot() == getConfig.getInt("items.crash.slot")) {

            if (!p.hasPermission("worldblaze.crash")) {
                p.sendMessage(getMessages().NO_PERMISSION);
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("items.crash.help")));
            } else CrashGui.create(p);

        }

        if (e.getSlot() == getConfig.getInt("items.double.slot")) {

            if (!p.hasPermission("worldblaze.double")) {
                p.sendMessage(getMessages().NO_PERMISSION);
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("items.double.help")));
            } else DoubleGui.create(p);

        }

        if (e.getSlot() == getConfig.getInt("items.mine.slot")) {

            if (!p.hasPermission("worldblaze.mine")) {
                p.sendMessage(getMessages().NO_PERMISSION);
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("items.mine.help")));
            } else MineMainGui.create(p);

        }

        if (e.getSlot() == getConfig.getInt("items.jogo-do-bicho.slot")) {

            if (!p.hasPermission("worldblaze.jogodobicho")) {
                p.sendMessage(getMessages().NO_PERMISSION);
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("items.jogo-do-bicho.help")));
            } else JogoDoBichoMainGui.create(p);

        }

        if (e.getSlot() == getConfig.getInt("items.coinflip.slot")) {

            if (!p.hasPermission("worldblaze.coinflip")) {
                p.sendMessage(getMessages().NO_PERMISSION);
                return;
            }

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if (e.isRightClick()) {
                p.closeInventory();
                p.sendMessage(c(getConfig.getString("items.coinflip.help")));
            } else CoinFlipMainGui.create(p);

        }

    }
}