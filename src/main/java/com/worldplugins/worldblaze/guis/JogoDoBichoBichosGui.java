package com.worldplugins.worldblaze.guis;

import com.worldplugins.worldblaze.enums.Bicho;
import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.NBTTag;
import com.worldplugins.worldblaze.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;
import static com.worldplugins.worldblaze.utils.Utils.letterFormatter;

public class JogoDoBichoBichosGui {

    public static void create(Player p){

        FileConfiguration getConfig = getBichosGuiYml().getConfig();

        Inventory inv = Bukkit.createInventory(null, 9 * getConfig.getInt("rows"), c(getConfig.getString("title")));

        for(String s : getConfig.getConfigurationSection("items").getKeys(false)){

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1, (byte) getConfig.getInt("items." + s + ".data"));
            item.setName(c(getConfig.getString("items." + s + ".name"))).setGlow(getConfig.getBoolean("items." + s + ".glow"));

            String bets = "0";
            String lastPlayer = "-/-";
            String totalMoney = "0";
            String totalCash = "0";
            try{

                Bicho bicho = Bicho.valueOf(s.toUpperCase());

                bets = getJogoDoBichoManager().getNumOfBets(bicho) + "";
                lastPlayer = getJogoDoBichoManager().getAnimalLastPlayer(bicho);
                totalMoney = letterFormatter(getJogoDoBichoManager().getAnimalTotalBetMoney(bicho));
                totalCash = letterFormatter(getJogoDoBichoManager().getAnimalTotalBetCash(bicho));

            }catch (IllegalArgumentException ignored){}

            String finalBets = bets;
            String finalLastPlayer = lastPlayer;
            String finalTotalMoney = totalMoney;
            String finalTotalCash = totalCash;
            getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w.replace("{apostas}", finalBets).
                    replace("{last_player}", finalLastPlayer).
                    replace("{total_money}", finalTotalMoney).
                    replace("{total_cash}", finalTotalCash))));

            int slot = getConfig.getInt("items." + s + ".slot");

            if(Bicho.isAnimal(s.toUpperCase())) NBTTag.setNBTString(item.toItemStack().getItemMeta(), "WBlaze_bicho", s.toUpperCase());

            inv.setItem(slot, item.toItemStack());
        }

        p.openInventory(inv);

        p.updateInventory();
    }

}
