package com.worldplugins.worldblaze.guis;

import com.worldplugins.worldblaze.enums.Animal;
import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.NBTTag;
import com.worldplugins.worldblaze.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;
import static com.worldplugins.worldblaze.utils.Utils.letterFormatter;

public class JogoDoBichoBetGui {

    public static void create(Player p, Animal animal){

        FileConfiguration getConfig = getBichosBetGuiYml().getConfig();

        Inventory inv = MenuHolder.create(MenuType.ANIMAL_GAME_BET, 9 * getConfig.getInt("rows"), c(getConfig.getString("title")));

        BlazeUser user = getManager().getBlazeUser(p.getName());

        for(String s : getConfig.getConfigurationSection("items").getKeys(false)){

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head-last-player")) item = new ItemBuilder(getJogoDoBichoManager().getHeadLastPlayer(animal));
            else if (getConfig.getBoolean("items." + s + ".use-head-animal")) item = new ItemBuilder(getJogoDoBichoManager().getAnimalItem(animal));
            else if (getConfig.getBoolean("items." + s + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1, (byte) getConfig.getInt("items." + s + ".data"));
            item.setName(c(getConfig.getString("items." + s + ".name"))).setGlow(getConfig.getBoolean("items." + s + ".glow"));

            getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w.
                    replace("{cash}", letterFormatter(user.getJogoDoBichoCashValue())).
                    replace("{money}", letterFormatter(user.getJogoDoBichoMoneyValue())).
                    replace("{players}", getJogoDoBichoManager().getNumOfBets(animal) + "").
                    replace("{last-player}", getJogoDoBichoManager().getAnimalLastPlayer(animal)).
                    replace("{total-money}", letterFormatter(getJogoDoBichoManager().getAnimalTotalBetMoney(animal))).
                    replace("{total-cash}", letterFormatter(getJogoDoBichoManager().getAnimalTotalBetCash(animal))).
                    replace("{animal}", animal.getNameStr()))));

            int slot = getConfig.getInt("items." + s + ".slot");

            NBTTag.setNBTString(item.toItemStack().getItemMeta(), "WBlaze_animal", animal.name());

            inv.setItem(slot, item.toItemStack());
        }

        p.openInventory(inv);
    }

}
