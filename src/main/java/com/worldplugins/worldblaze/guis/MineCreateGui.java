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

public class MineCreateGui {

    public static void create(Player p){

        FileConfiguration getConfig = getCreateMineGuiYml().getConfig();

        Inventory inv = Bukkit.createInventory(null, 9 * getConfig.getInt("Linhas"), c(getConfig.getString("Titulo")));

        BlazeUser blazeUser = getManager().getBlazeUser(p.getName());

        for(String s : getConfig.getConfigurationSection("Itens").getKeys(false)){

            ItemBuilder item;
            if (getConfig.getBoolean("Itens." + s + ".Usar-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens." + s + ".Textura")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens." + s + ".Id")), 1, (byte) getConfig.getInt("Itens." + s + ".Data"));
            item.setName(c(getConfig.getString("Itens." + s + ".Nome"))).setGlow(getConfig.getBoolean("Itens." + s + ".Glow"));

            getConfig.getStringList("Itens." + s + ".Lore").forEach(w -> item.addLoreLine(c(w.
                    replace("{money}", letterFormatter(blazeUser.getMineMoneyValue())).
                    replace("{cash}", letterFormatter(blazeUser.getMineCashValue())).
                    replace("{bombas}",blazeUser.getMineBombs()+"").
                    replace("{chance}", String.format("%.2f", 100.0 - ((double) blazeUser.getMineBombs() / (double) getInstance().getConfig().getIntegerList("config.mine.game-slots").size()) * 100.0)))));

            int slot = getConfig.getInt("Itens." + s + ".Slot");

            inv.setItem(slot, item.toItemStack());
        }

        p.openInventory(inv);
    }

}
