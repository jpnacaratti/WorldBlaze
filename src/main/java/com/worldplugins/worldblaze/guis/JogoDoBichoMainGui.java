package com.worldplugins.worldblaze.guis;

import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;
import static com.worldplugins.worldblaze.utils.Utils.letterFormatter;

public class JogoDoBichoMainGui {

    public static void create(Player p){

        FileConfiguration getConfig = getJogoDoBichoGuiYml().getConfig();

        Inventory inv = MenuHolder.create(MenuType.ANIMAL_GAME_MAIN, 9 * getConfig.getInt("rows"), c(getConfig.getString("title")));

        for(String s : getConfig.getConfigurationSection("items").getKeys(false)){

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head-last-player")) item = new ItemBuilder(getJogoDoBichoManager().getHeadLastPlayer());
            else if (getConfig.getBoolean("items." + s + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1, (byte) getConfig.getInt("items." + s + ".data"));
            item.setName(c(getConfig.getString("items." + s + ".name"))).setGlow(getConfig.getBoolean("items." + s + ".glow"));

            getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w.
                    replace("{players}", getJogoDoBichoManager().getJogoDoBichoBets().size() + "").
                    replace("{last-player}", getJogoDoBichoManager().getLastPlayer()).
                    replace("{total-money}", letterFormatter(getJogoDoBichoManager().getTotalBetMoney())).
                    replace("{total-cash}", letterFormatter(getJogoDoBichoManager().getTotalBetCash())))));

            int slot = getConfig.getInt("items." + s + ".slot");

            inv.setItem(slot, item.toItemStack());
        }

        p.openInventory(inv);
    }

}
