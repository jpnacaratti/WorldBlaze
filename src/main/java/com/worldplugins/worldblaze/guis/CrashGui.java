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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class CrashGui {

    public static void create(Player p){

        FileConfiguration getConfig = getCrashGuiYml().getConfig();

        Inventory inv = Bukkit.createInventory(null, 9 * getConfig.getInt("rows"), getCrashManager().getMenuTitle());

        BlazeUser blazeUser = getManager().getBlazeUser(p.getName());

        inv.setItem(getCrashManager().getMoneySlot(), getCrashManager().getMoneyItem(blazeUser));
        inv.setItem(getCrashManager().getCashSlot(), getCrashManager().getCashItem(blazeUser));

        if (getCrashManager().isStarting()){
            inv.setItem(getCrashManager().getCrashStartingSlot(), getCrashManager().getCrashStartingItem(blazeUser));
            for (Map.Entry<Integer, ItemStack> entry : getCrashManager().getStartingGlass().entrySet()) {
                inv.setItem(entry.getKey(), entry.getValue());
            }
        }else if (!getCrashManager().isStarting() && !getCrashManager().isCrashed()){
            inv.setItem(getCrashManager().getCrashStartedSlot(), getCrashManager().getCrashStartedItem(blazeUser));
            for (Map.Entry<Integer, ItemStack> entry : getCrashManager().getStartedGlass().entrySet()) {
                inv.setItem(entry.getKey(), entry.getValue());
            }
        }else{
            inv.setItem(getCrashManager().getCrashCrashedSlot(), getCrashManager().getCrashCrashedItem(blazeUser));
            for (Map.Entry<Integer, ItemStack> entry : getCrashManager().getCrashedGlass().entrySet()) {
                inv.setItem(entry.getKey(), entry.getValue());
            }
        }

        inv.setItem(getCrashManager().getHistoricSlot(), getCrashManager().getHistoricItem());
        inv.setItem(getCrashManager().getGamblersSlot(), getCrashManager().getGamblersItem());

        for(String s : getConfig.getConfigurationSection("items").getKeys(false)){

            if(s.equalsIgnoreCase("money") || s.equalsIgnoreCase("cash") ||
                    s.equalsIgnoreCase("crash-starting") || s.equalsIgnoreCase("crash-started") ||
                    s.equalsIgnoreCase("crash-crashed") || s.equalsIgnoreCase("history") ||
                    s.equalsIgnoreCase("bettors")) continue;

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1, (byte) getConfig.getInt("items." + s + ".data"));
            item.setName(c(getConfig.getString("items." + s + ".name"))).setGlow(getConfig.getBoolean("items." + s + ".glow"));

            getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w)));

            int slot = getConfig.getInt("items." + s + ".slot");

            inv.setItem(slot, item.toItemStack());
        }

        getCrashManager().getCrashInventories().put(p.getName(), inv);

        p.openInventory(inv);
    }

}
