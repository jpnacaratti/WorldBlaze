package com.worldplugins.worldblaze.guis;

import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.objects.DoubleAnimationSpot;
import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;
import static com.worldplugins.worldblaze.utils.Utils.letterFormatter;

public class DoubleGui {

    public static void create(Player p){

        FileConfiguration getConfig = getDoubleGuiYml().getConfig();

        Inventory inv = Bukkit.createInventory(null, 9 * 4, getDoubleManager().getMenuTitle());

        BlazeUser blazeUser = getManager().getBlazeUser(p.getName());

        inv.setItem(getDoubleManager().getMoneySlot(), getDoubleManager().getMoneyItem(blazeUser));
        inv.setItem(getDoubleManager().getCashSlot(), getDoubleManager().getCashItem(blazeUser));

        inv.setItem(getDoubleManager().getBetRedSlot(), getDoubleManager().getBetRedItem(blazeUser));
        inv.setItem(getDoubleManager().getBetWhiteSlot(), getDoubleManager().getBetWhiteItem(blazeUser));
        inv.setItem(getDoubleManager().getBetBlackSlot(), getDoubleManager().getBetBlackItem(blazeUser));

        inv.setItem(getDoubleManager().getHistoricSlot(), getDoubleManager().getHistoricItem());
        inv.setItem(getDoubleManager().getGamblersSlot(), getDoubleManager().getGamblersItem());

        for(String s : getConfig.getConfigurationSection("items").getKeys(false)){

            if(s.equalsIgnoreCase("cash") || s.equalsIgnoreCase("money") ||
                    s.equalsIgnoreCase("bet-red") || s.equalsIgnoreCase("bet-white") ||
                    s.equalsIgnoreCase("bet-black") || s.equalsIgnoreCase("history") ||
                    s.equalsIgnoreCase("bettors")) continue;

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1, (byte) getConfig.getInt("items." + s + ".data"));
            item.setName(c(getConfig.getString("items." + s + ".name"))).setGlow(getConfig.getBoolean("items." + s + ".glow"));

            getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w)));

            int slot = getConfig.getInt("items." + s + ".slot");

            inv.setItem(slot, item.toItemStack());
        }

        for (DoubleAnimationSpot animationSpot : getDoubleManager().getSetAnimationSpots()) {
            for (Map.Entry<ItemStack, List<Integer>> entry : animationSpot.getItems().entrySet()) {
                entry.getValue().forEach(w -> inv.setItem(w, entry.getKey()));
            }
        }

        getDoubleManager().setColorItems(inv);

        getDoubleManager().getDoubleInventories().put(p.getName(), inv);

        p.openInventory(inv);
    }

}
