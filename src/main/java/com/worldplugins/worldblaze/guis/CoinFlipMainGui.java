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

        Inventory inv = Bukkit.createInventory(null, 9 * getConfig.getInt("Linhas"), c(getConfig.getString("Titulo")));

        inv.setItem(getCoinFlipManager().getPrincipalGamesSlot(), getCoinFlipManager().getPrincipalGamesItem(p.getName()));

        for(String s : getConfig.getConfigurationSection("Itens").getKeys(false)){

            if(s.equalsIgnoreCase("jogos")) continue;

            ItemBuilder item;
            if (getConfig.getBoolean("Itens." + s + ".Usar-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens." + s + ".Textura")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens." + s + ".Id")), 1, (byte) getConfig.getInt("Itens." + s + ".Data"));
            item.setName(c(getConfig.getString("Itens." + s + ".Nome"))).setGlow(getConfig.getBoolean("Itens." + s + ".Glow"));

            if(s.equalsIgnoreCase("criarjogo")){

                if(!getCoinFlipManager().isPlayerParticipating(p.getName())) getConfig.getStringList("Itens." + s + ".Lore-naoparticipando").forEach(w -> item.addLoreLine(c(w)));
                else getConfig.getStringList("Itens." + s + ".Lore-participando").forEach(w -> item.addLoreLine(c(w)));

            }else getConfig.getStringList("Itens." + s + ".Lore").forEach(w -> item.addLoreLine(c(w)));

            int slot = getConfig.getInt("Itens." + s + ".Slot");

            inv.setItem(slot, item.toItemStack());
        }

        getCoinFlipManager().getPrincipalInventories().put(p.getName(), inv);

        p.openInventory(inv);
    }

}
