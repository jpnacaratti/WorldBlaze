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
import static com.worldplugins.worldblaze.utils.Utils.letterFormatter;

public class JogoDoBichoMainGui {

    public static void create(Player p){

        FileConfiguration getConfig = getJogoDoBichoGuiYml().getConfig();

        Inventory inv = Bukkit.createInventory(null, 9 * getConfig.getInt("Linhas"), c(getConfig.getString("Titulo")));

        for(String s : getConfig.getConfigurationSection("Itens").getKeys(false)){

            ItemBuilder item;
            if (getConfig.getBoolean("Itens." + s + ".Usar-head-ultimo-player")) item = new ItemBuilder(getJogoDoBichoManager().getHeadLastPlayer());
            else if (getConfig.getBoolean("Itens." + s + ".Usar-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens." + s + ".Textura")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens." + s + ".Id")), 1, (byte) getConfig.getInt("Itens." + s + ".Data"));
            item.setName(c(getConfig.getString("Itens." + s + ".Nome"))).setGlow(getConfig.getBoolean("Itens." + s + ".Glow"));

            getConfig.getStringList("Itens." + s + ".Lore").forEach(w -> item.addLoreLine(c(w.
                    replace("{jogadores}", getJogoDoBichoManager().getJogoDoBichoBets().size() + "").
                    replace("{last_player}", getJogoDoBichoManager().getLastPlayer()).
                    replace("{total_money}", letterFormatter(getJogoDoBichoManager().getTotalBetMoney())).
                    replace("{total_cash}", letterFormatter(getJogoDoBichoManager().getTotalBetCash())))));

            int slot = getConfig.getInt("Itens." + s + ".Slot");

            inv.setItem(slot, item.toItemStack());
        }

        p.openInventory(inv);
    }

}
