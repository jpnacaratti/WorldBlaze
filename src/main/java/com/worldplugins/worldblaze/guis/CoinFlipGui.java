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

        Inventory inv = Bukkit.createInventory(null, 9 * getConfig.getInt("Linhas"), c(getConfig.getString("Titulo")));

        inv.setItem(getCoinFlipManager().getChoiceSlot(), getCoinFlipManager().getStartingItem(bet));

        for(String s : getConfig.getConfigurationSection("Itens").getKeys(false)){

            ItemBuilder item;
            if (getConfig.getBoolean("Itens." + s + ".Usar-head-player")){

                if(s.equalsIgnoreCase("dono")) item = new ItemBuilder(SkullCreator.itemFromName(bet.getOwner()));
                else item = new ItemBuilder(SkullCreator.itemFromName(bet.getOpponent()));

            } else if (getConfig.getBoolean("Itens." + s + ".Usar-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens." + s + ".Textura")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens." + s + ".Id")), 1, (byte) getConfig.getInt("Itens." + s + ".Data"));
            item.setName(c(getConfig.getString("Itens." + s + ".Nome"))).setGlow(getConfig.getBoolean("Itens." + s + ".Glow"));

            getConfig.getStringList("Itens." + s + ".Lore").forEach(w -> item.addLoreLine(c(w.
                    replace("{escolhaOponente}", bet.isCara() ? "Coroa" : "Cara").
                    replace("{escolhaDono}", bet.isCara() ? "Cara" : "Coroa"))));

            int slot = getConfig.getInt("Itens." + s + ".Slot");

            inv.setItem(slot, item.toItemStack());
        }

        return inv;
    }

}
