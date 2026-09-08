package com.worldplugins.worldblaze.guis;

import com.worldplugins.worldblaze.objects.MineBet;
import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.NBTTag;
import com.worldplugins.worldblaze.utils.SkullCreator;
import com.worldplugins.worldblaze.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.stream.Collectors;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.c;
import static com.worldplugins.worldblaze.utils.Utils.letterFormatter;

public class MinePlayersGui {

    public static Map<Player, ArrayList<Inventory>> invs = new HashMap<>();

    public static void create(Player p){
        FileConfiguration getConfig = getMinePlayersGuiYml().getConfig();

        Inventory inv = Bukkit.createInventory(null, 9 * 6, c(getConfig.getString("Titulo")));

        List<MineBet> sorted = new ArrayList<>(getMineManager().getMineBets());

        sorted = sorted.stream().sorted(Comparator.comparingDouble(MineBet::getMid).reversed()).collect(Collectors.toList());

        ArrayList<Inventory> list = new ArrayList<>();
        list.add(inv);
        invs.remove(p);
        invs.put(p, list);
        int invLine = 10;
        int invSlot = 0;
        int numPag = 0;
        if(sorted.isEmpty()){

            ItemBuilder item;
            if (getConfig.getBoolean("Itens.vazio.Usar-head")) {
                item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.vazio.Textura")));
            } else {
                item = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.vazio.Id")), 1,
                        (byte) getConfig.getInt("Itens.vazio.Data"));
            }
            List<String> lore = getConfig.getStringList("Itens.vazio.Lore");
            lore.replaceAll(Utils::c);
            item.setName(c(getConfig.getString("Itens.vazio.Nome"))).setLore(lore).
                    setGlow(getConfig.getBoolean("Itens.vazio.Glow"));
            int slot = getConfig.getInt("Itens.vazio.Slot");

            inv.setItem(slot, item.toItemStack());
        }else{

            for (MineBet mineBet : sorted) {

                if(invs.get(p).get(numPag).getItem(16) != null) invLine = 19;
                if(invs.get(p).get(numPag).getItem(25) != null) invLine = 28;
                if(invs.get(p).get(numPag).getItem(34) != null) invLine = 37;
                if(invs.get(p).get(numPag).getItem(43) != null) {

                    Inventory newInv = Bukkit.createInventory(null, 9 * 6, c(getConfig.getString("Titulo")));

                    ItemBuilder next;
                    if (getConfig.getBoolean("Itens.proxpag.Usar-head"))
                        next = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.proxpag.Textura")));
                    else next = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.proxpag.Id")),1,
                            (byte) getConfig.getInt("Itens.proxpag.Data"));
                    List<String> nLore = getConfig.getStringList("Itens.proxpag.Lore");
                    nLore.replaceAll(Utils::c);
                    next.setName(c(getConfig.getString("Itens.proxpag.Nome"))).
                            setLore(nLore).setGlow(getConfig.getBoolean("Itens.proxpag.Glow"));
                    NBTTag.setNBTString(next.toItemStack().getItemMeta(), "WBlaze_minePag", ""+(numPag+1));
                    int slot = getConfig.getInt("Itens.proxpag.Slot");

                    ItemBuilder ant;
                    if (getConfig.getBoolean("Itens.pagant.Usar-head"))
                        ant = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.pagant.Textura")));
                    else ant = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.pagant.Id")),1,
                            (byte) getConfig.getInt("Itens.pagant.Data"));
                    List<String> antLore = getConfig.getStringList("Itens.pagant.Lore");
                    antLore.replaceAll(Utils::c);
                    ant.setName(c(getConfig.getString("Itens.pagant.Nome"))).
                            setLore(antLore).setGlow(getConfig.getBoolean("Itens.pagant.Glow"));
                    NBTTag.setNBTString(ant.toItemStack().getItemMeta(), "WBlaze_minePag", ""+numPag);
                    int antSlot = getConfig.getInt("Itens.pagant.Slot");

                    newInv.setItem(antSlot, ant.toItemStack());
                    invs.get(p).get(numPag).setItem(slot, next.toItemStack());

                    invs.get(p).add(newInv);
                    numPag++;
                    invLine = 10;
                    invSlot = 0;
                }

                ItemBuilder item;
                if(getConfig.getBoolean("Itens.player.Usar-head-player")){
                    item = new ItemBuilder(SkullCreator.itemFromName(mineBet.getOwner()));
                }else if (getConfig.getBoolean("Itens.player.Usar-head"))
                    item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.player.Textura")));
                else item = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.player.Id")),1,
                            (byte) getConfig.getInt("Itens.player.Data"));

                double wonMoney = mineBet.getMoneyValue() * mineBet.getMultiplier();
                double wonCash = mineBet.getCashValue() * mineBet.getMultiplier();

                getConfig.getStringList("Itens.player.Lore").forEach(w -> item.addLoreLine(c(w.
                        replace("{nick}", mineBet.getOwner()).
                        replace("{money}", letterFormatter(mineBet.getMoneyValue())).
                        replace("{cash}", letterFormatter(mineBet.getCashValue())).
                        replace("{bombas}", mineBet.getNumBombs() + "").
                        replace("{slots}", mineBet.getClickedSlots().size() + "").
                        replace("{multiplicador}", getMineManager().formatValue(mineBet.getMultiplier())).
                        replace("{ganhos-money}", letterFormatter(wonMoney)).
                        replace("{ganhos-cash}", letterFormatter(wonCash)).
                        replace("{id}", mineBet.getBetId()))
                ));


                item.setName(c(getConfig.getString("Itens.player.Nome").replace("{nick}", mineBet.getOwner()))).
                        setGlow(getConfig.getBoolean("Itens.player.Glow"));

                NBTTag.setNBTString(item.toItemStack().getItemMeta(), "WBlaze_mineJogador", mineBet.getOwner());

                if(invSlot == 7) invSlot = 0;

                invs.get(p).get(numPag).setItem(invLine+invSlot, item.toItemStack());
                invSlot++;
            }
        }

        for (String s : getConfig.getConfigurationSection("Itens").getKeys(false)) {

            if(s.equalsIgnoreCase("proxpag") || s.equalsIgnoreCase("pagant") ||
                    s.equalsIgnoreCase("player") || s.equalsIgnoreCase("vazio")) continue;

            ItemBuilder item;
            if (getConfig.getBoolean("Itens." + s + ".Usar-head")) {
                item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens." + s + ".Textura")));
            } else {
                item = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens." + s + ".Id")), 1,
                        (byte) getConfig.getInt("Itens." + s + ".Data"));
            }
            List<String> lore = getConfig.getStringList("Itens." + s + ".Lore");
            lore.replaceAll(Utils::c);
            String name = getConfig.getString("Itens." + s + ".Nome");
            if(!name.isEmpty()) item.setName(c(name));
            item.setLore(lore).setGlow(getConfig.getBoolean("Itens." + s + ".Glow"));
            int slot = getConfig.getInt("Itens." + s + ".Slot");

            for (int i = 0; i < invs.get(p).size(); i++) {
                invs.get(p).get(i).setItem(slot, item.toItemStack());
            }
        }

        p.openInventory(invs.get(p).get(0));

        getMineManager().getPlayersGuiOpen().add(p.getName());
    }

}
