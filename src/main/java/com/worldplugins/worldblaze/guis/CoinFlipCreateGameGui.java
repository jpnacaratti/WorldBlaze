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

public class CoinFlipCreateGameGui {

    public static void create(Player p){

        FileConfiguration getConfig = getCoinFlipCreateGameGuiYml().getConfig();

        Inventory inv = Bukkit.createInventory(null, 9 * getConfig.getInt("Linhas"), c(getConfig.getString("Titulo")));

        BlazeUser user = getManager().getBlazeUser(p.getName());

        for(String s : getConfig.getConfigurationSection("Itens").getKeys(false)){

            if(s.equalsIgnoreCase("jogos")) continue;

            String choice = user.isCoinFlipCara() ? "Cara" : "Coroa";

            ItemBuilder item;
            if (getConfig.getBoolean("Itens." + s + ".Usar-head-player")) item = user.getCoinFlipSpecific() == null ? new ItemBuilder(SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654")) : new ItemBuilder(SkullCreator.itemFromName(user.getCoinFlipSpecific()));
            else if (getConfig.getBoolean("Itens." + s + ".Usar-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens." + s + ".Textura")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens." + s + ".Id")), 1, (byte) getConfig.getInt("Itens." + s + ".Data"));
            item.setName(c(getConfig.getString("Itens." + s + ".Nome").
                    replace("{escolha}", choice))).setGlow(getConfig.getBoolean("Itens." + s + ".Glow"));

            getConfig.getStringList("Itens." + s + ".Lore").forEach(w -> item.addLoreLine(c(w.
                    replace("{escolha}", choice).
                    replace("{money}", letterFormatter(user.getCoinFlipMoneyValue())).
                    replace("{cash}", letterFormatter(user.getCoinFlipCashValue())).
                    replace("{adversario}", user.getCoinFlipSpecific() == null ? "-/-" : user.getCoinFlipSpecific()))));

            int slot = getConfig.getInt("Itens." + s + ".Slot");

            inv.setItem(slot, item.toItemStack());
        }

        p.openInventory(inv);
    }

}
