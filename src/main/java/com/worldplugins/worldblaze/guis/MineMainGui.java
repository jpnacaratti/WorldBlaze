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

public class MineMainGui {

    public static void create(Player p){

        FileConfiguration getConfig = getMainMineGuiYml().getConfig();

        Inventory inv = MenuHolder.create(MenuType.MINE_MAIN, 9 * getConfig.getInt("rows"), c(getConfig.getString("title")));

        inv.setItem(getMineManager().getMainPlayersSlot(), getMineManager().getMainPlayersItem());

        for(String s : getConfig.getConfigurationSection("items").getKeys(false)){

            if(s.equalsIgnoreCase("players")) continue;

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1, (byte) getConfig.getInt("items." + s + ".data"));
            item.setName(c(getConfig.getString("items." + s + ".name"))).setGlow(getConfig.getBoolean("items." + s + ".glow"));

            getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w)));

            int slot = getConfig.getInt("items." + s + ".slot");

            inv.setItem(slot, item.toItemStack());
        }

        getMineManager().getMainInventories().put(p.getName(), inv);

        p.openInventory(inv);
    }

}
