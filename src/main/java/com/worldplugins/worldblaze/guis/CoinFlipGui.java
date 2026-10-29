package com.worldplugins.worldblaze.guis;

import com.worldplugins.worldblaze.objects.CoinFlipBet;
import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;

public class CoinFlipGui {

    public static Inventory create(CoinFlipBet bet){

        FileConfiguration getConfig = getCoinFlipGuiYml().getConfig();

        Inventory inv = MenuHolder.create(MenuType.COINFLIP_GAME, 9 * getConfig.getInt("rows"), c(getConfig.getString("title")));

        inv.setItem(getCoinFlipManager().getChoiceSlot(), getCoinFlipManager().getStartingItem(bet));

        for(String s : getConfig.getConfigurationSection("items").getKeys(false)){

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head-player")){

                if(s.equalsIgnoreCase("owner")) item = new ItemBuilder(SkullCreator.itemFromName(bet.getOwner()));
                else item = new ItemBuilder(SkullCreator.itemFromName(bet.getOpponent()));

            } else if (getConfig.getBoolean("items." + s + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1, (byte) getConfig.getInt("items." + s + ".data"));
            String nick = s.equalsIgnoreCase("owner") ? bet.getOwner() : bet.getOpponent();

            item.setName(c(getConfig.getString("items." + s + ".name").replace("{nick}", nick))).setGlow(getConfig.getBoolean("items." + s + ".glow"));

            getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w.
                    replace("{nick}", nick).
                    replace("{opponent-choice}", bet.isHeads() ? "Tails" : "Heads").
                    replace("{owner-choice}", bet.isHeads() ? "Heads" : "Tails"))));

            int slot = getConfig.getInt("items." + s + ".slot");

            inv.setItem(slot, item.toItemStack());
        }

        return inv;
    }

}
