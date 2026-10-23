package com.worldplugins.worldblaze.guis;

import com.worldplugins.worldblaze.enums.Animal;
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

        Inventory inv = MenuHolder.create(MenuType.ANIMAL_GAME_ANIMALS, 9 * getConfig.getInt("rows"), c(getConfig.getString("title")));

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

                Animal animal = Animal.valueOf(s.toUpperCase());

                bets = getJogoDoBichoManager().getNumOfBets(animal) + "";
                lastPlayer = getJogoDoBichoManager().getAnimalLastPlayer(animal);
                totalMoney = letterFormatter(getJogoDoBichoManager().getAnimalTotalBetMoney(animal));
                totalCash = letterFormatter(getJogoDoBichoManager().getAnimalTotalBetCash(animal));

            }catch (IllegalArgumentException ignored){}

            String finalBets = bets;
            String finalLastPlayer = lastPlayer;
            String finalTotalMoney = totalMoney;
            String finalTotalCash = totalCash;
            getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w.replace("{bets}", finalBets).
                    replace("{last-player}", finalLastPlayer).
                    replace("{total-money}", finalTotalMoney).
                    replace("{total-cash}", finalTotalCash))));

            int slot = getConfig.getInt("items." + s + ".slot");

            if(Animal.isAnimal(s.toUpperCase())) NBTTag.setNBTString(item.toItemStack().getItemMeta(), "WBlaze_bicho", s.toUpperCase());

            inv.setItem(slot, item.toItemStack());
        }

        p.openInventory(inv);

        p.updateInventory();
    }

}
