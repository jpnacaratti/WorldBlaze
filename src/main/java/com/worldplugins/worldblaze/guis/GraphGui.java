package com.worldplugins.worldblaze.guis;

import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;
import static com.worldplugins.worldblaze.utils.Utils.letterFormatter;

public class GraphGui {

    public static void create(Player p){

        FileConfiguration getConfig = getGraphGuiYml().getConfig();

        Inventory inv = MenuHolder.create(MenuType.GRAPH, 9 * 6, c(getConfig.getString("title")));

        BlazeUser blazeUser = getManager().getBlazeUser(p.getName());

        inv.setItem(getGraphManager().getMoneySlot(), getGraphManager().getMoneyItem(blazeUser));
        inv.setItem(getGraphManager().getCashSlot(), getGraphManager().getCashItem(blazeUser));

        inv.setItem(getGraphManager().getBetDecreaseSlot(), getGraphManager().getBetDecreaseItem(blazeUser));
        inv.setItem(getGraphManager().getTimeSlot(), getGraphManager().getTimeItem(blazeUser));
        inv.setItem(getGraphManager().getBetUpSlot(), getGraphManager().getBetUpItem(blazeUser));

        inv.setItem(getGraphManager().getHistoricSlot(), getGraphManager().getHistoricItem());
        inv.setItem(getGraphManager().getGamblersSlot(), getGraphManager().getGamblersItem());

        for(String s : getConfig.getConfigurationSection("items").getKeys(false)){

            if(s.equalsIgnoreCase("money") || s.equalsIgnoreCase("cash") ||
                    s.equalsIgnoreCase("bet-decrease") || s.equalsIgnoreCase("time") ||
                    s.equalsIgnoreCase("bet-increase") || s.equalsIgnoreCase("history") ||
                    s.equalsIgnoreCase("bettors")) continue;

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1, (byte) getConfig.getInt("items." + s + ".data"));
            item.setName(c(getConfig.getString("items." + s + ".name"))).setGlow(getConfig.getBoolean("items." + s + ".glow"));

            getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w)));

            int slot = getConfig.getInt("items." + s + ".slot");

            inv.setItem(slot, item.toItemStack());
        }

        getGraphManager().getGraphInventories().put(p.getName(), inv);

        p.openInventory(inv);
    }

}
