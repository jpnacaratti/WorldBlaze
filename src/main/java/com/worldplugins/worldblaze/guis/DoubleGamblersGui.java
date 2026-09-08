package com.worldplugins.worldblaze.guis;

import com.worldplugins.worldblaze.enums.DoubleColor;
import com.worldplugins.worldblaze.objects.DoubleBet;
import com.worldplugins.worldblaze.objects.GraphBet;
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

public class DoubleGamblersGui {

    public static Map<Player, ArrayList<Inventory>> invs = new HashMap<>();

    public static void create(Player p){
        FileConfiguration getConfig = getDoubleGamblersGuiYml().getConfig();

        Inventory inv = Bukkit.createInventory(null, 9 * 6, c(getConfig.getString("Titulo")));

        List<DoubleBet> sorted = new ArrayList<>(getDoubleManager().getDoubleBets());

        sorted = sorted.stream().sorted(Comparator.comparingDouble(DoubleBet::getMid).reversed()).collect(Collectors.toList());

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

            for (DoubleBet doubleBet : sorted) {

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
                    NBTTag.setNBTString(next.toItemStack().getItemMeta(), "WBlaze_doublePag", ""+(numPag+1));
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
                    NBTTag.setNBTString(ant.toItemStack().getItemMeta(), "WBlaze_doublePag", ""+numPag);
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
                    item = new ItemBuilder(SkullCreator.itemFromName(doubleBet.getOwner()));
                }else if (getConfig.getBoolean("Itens.player.Usar-head"))
                    item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens.player.Textura")));
                else item = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens.player.Id")),1,
                            (byte) getConfig.getInt("Itens.player.Data"));

                double wonMoney = 0;
                double wonCash = 0;
                String status = c(getConfig.getString("Itens.player.Status-esperando"));
                if(getDoubleManager().isStopped()){
                    if(getDoubleManager().getColorCache().get(13) == doubleBet.getColor()){
                        status = c(getConfig.getString("Itens.player.Status-ganhou").replace("{multiplicador}", doubleBet.getColor() == DoubleColor.WHITE ? 14 + "" : 2 + ""));
                        wonMoney = doubleBet.getColor() == DoubleColor.WHITE ? doubleBet.getMoneyValue() * 14 : doubleBet.getMoneyValue() * 2;
                        wonCash = doubleBet.getColor() == DoubleColor.WHITE ? doubleBet.getCashValue() * 14 : doubleBet.getCashValue() * 2;
                    } else status = c(getConfig.getString("Itens.player.Status-perdeu"));
                }

                String color;
                if(doubleBet.getColor() == DoubleColor.RED) color = c(getConfig.getString("Itens.player.Apostou-vermelho"));
                else if (doubleBet.getColor() == DoubleColor.WHITE) color = c(getConfig.getString("Itens.player.Apostou-branco"));
                else color = c(getConfig.getString("Itens.player.Apostou-preto"));

                String finalStatus = status;
                double finalWonMoney = wonMoney;
                double finalWonCash = wonCash;
                getConfig.getStringList("Itens.player.Lore").forEach(w -> item.addLoreLine(c(w.
                        replace("{nick}", doubleBet.getOwner()).
                        replace("{money}", letterFormatter(doubleBet.getMoneyValue())).
                        replace("{cash}", letterFormatter(doubleBet.getCashValue())).
                        replace("{cor}", color).
                        replace("{status}", finalStatus).
                        replace("{ganhos-money}", letterFormatter(finalWonMoney)).
                        replace("{ganhos-cash}", letterFormatter(finalWonCash)).
                        replace("{id}", doubleBet.getBetId()))
                ));


                item.setName(c(getConfig.getString("Itens.player.Nome").replace("{nick}", doubleBet.getOwner()))).
                        setGlow(getConfig.getBoolean("Itens.player.Glow"));

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

        getDoubleManager().getGamblersGuiOpen().add(p.getName());
    }

}
