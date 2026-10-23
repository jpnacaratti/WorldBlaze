package com.worldplugins.worldblaze.guis;

import com.worldplugins.worldblaze.objects.BlazeUser;
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

public class CoinFlipCreateGameGui {

    public static void create(Player p){

        FileConfiguration getConfig = getCoinFlipCreateGameGuiYml().getConfig();

        Inventory inv = MenuHolder.create(MenuType.COINFLIP_CREATE, 9 * getConfig.getInt("rows"), c(getConfig.getString("title")));

        BlazeUser user = getManager().getBlazeUser(p.getName());

        for(String s : getConfig.getConfigurationSection("items").getKeys(false)){

            if(s.equalsIgnoreCase("games")) continue;

            String choice = user.isCoinFlipHeads() ? "Heads" : "Tails";

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head-player")) item = user.getCoinFlipSpecific() == null ? new ItemBuilder(SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654")) : new ItemBuilder(SkullCreator.itemFromName(user.getCoinFlipSpecific()));
            else if (getConfig.getBoolean("items." + s + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1, (byte) getConfig.getInt("items." + s + ".data"));
            item.setName(c(getConfig.getString("items." + s + ".name").
                    replace("{choice}", choice))).setGlow(getConfig.getBoolean("items." + s + ".glow"));

            getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w.
                    replace("{choice}", choice).
                    replace("{money}", letterFormatter(user.getCoinFlipMoneyValue())).
                    replace("{cash}", letterFormatter(user.getCoinFlipCashValue())).
                    replace("{current-opponent}", user.getCoinFlipSpecific() == null ? "-/-" : user.getCoinFlipSpecific()))));

            int slot = getConfig.getInt("items." + s + ".slot");

            inv.setItem(slot, item.toItemStack());
        }

        p.openInventory(inv);
    }

}
