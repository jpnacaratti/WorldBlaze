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

public class CoinFlipMainGui {

    public static void create(Player p){

        FileConfiguration getConfig = getCoinFlipPrincipalGuiYml().getConfig();

        Inventory inv = Bukkit.createInventory(null, 9 * getConfig.getInt("rows"), c(getConfig.getString("title")));

        inv.setItem(getCoinFlipManager().getPrincipalGamesSlot(), getCoinFlipManager().getPrincipalGamesItem(p.getName()));

        for(String s : getConfig.getConfigurationSection("items").getKeys(false)){

            if(s.equalsIgnoreCase("games")) continue;

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1, (byte) getConfig.getInt("items." + s + ".data"));
            item.setName(c(getConfig.getString("items." + s + ".name"))).setGlow(getConfig.getBoolean("items." + s + ".glow"));

            if(s.equalsIgnoreCase("create-game")){

                if(!getCoinFlipManager().isPlayerParticipating(p.getName())) getConfig.getStringList("items." + s + ".lore-not-participating").forEach(w -> item.addLoreLine(c(w)));
                else getConfig.getStringList("items." + s + ".lore-participating").forEach(w -> item.addLoreLine(c(w)));

            }else getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w)));

            int slot = getConfig.getInt("items." + s + ".slot");

            inv.setItem(slot, item.toItemStack());
        }

        getCoinFlipManager().getPrincipalInventories().put(p.getName(), inv);

        p.openInventory(inv);
    }

}
