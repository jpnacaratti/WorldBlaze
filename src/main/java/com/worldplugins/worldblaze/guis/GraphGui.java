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

        Inventory inv = Bukkit.createInventory(null, 9 * 6, c(getConfig.getString("Titulo")));

        BlazeUser blazeUser = getManager().getBlazeUser(p.getName());

        inv.setItem(getGraphManager().getMoneySlot(), getGraphManager().getMoneyItem(blazeUser));
        inv.setItem(getGraphManager().getCashSlot(), getGraphManager().getCashItem(blazeUser));

        inv.setItem(getGraphManager().getBetDecreaseSlot(), getGraphManager().getBetDecreaseItem(blazeUser));
        inv.setItem(getGraphManager().getTimeSlot(), getGraphManager().getTimeItem(blazeUser));
        inv.setItem(getGraphManager().getBetUpSlot(), getGraphManager().getBetUpItem(blazeUser));

        inv.setItem(getGraphManager().getHistoricSlot(), getGraphManager().getHistoricItem());
        inv.setItem(getGraphManager().getGamblersSlot(), getGraphManager().getGamblersItem());

        for(String s : getConfig.getConfigurationSection("Itens").getKeys(false)){

            if(s.equalsIgnoreCase("money") || s.equalsIgnoreCase("cash") ||
                    s.equalsIgnoreCase("apostar-diminuir") || s.equalsIgnoreCase("tempo") ||
                    s.equalsIgnoreCase("apostar-aumentar") || s.equalsIgnoreCase("historico") ||
                    s.equalsIgnoreCase("apostadores")) continue;

            ItemBuilder item;
            if (getConfig.getBoolean("Itens." + s + ".Usar-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens." + s + ".Textura")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens." + s + ".Id")), 1, (byte) getConfig.getInt("Itens." + s + ".Data"));
            item.setName(c(getConfig.getString("Itens." + s + ".Nome"))).setGlow(getConfig.getBoolean("Itens." + s + ".Glow"));

            getConfig.getStringList("Itens." + s + ".Lore").forEach(w -> item.addLoreLine(c(w)));

            int slot = getConfig.getInt("Itens." + s + ".Slot");

            inv.setItem(slot, item.toItemStack());
        }

        getGraphManager().getGraphInventories().put(p.getName(), inv);

        p.openInventory(inv);
    }

}
