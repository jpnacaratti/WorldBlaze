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

        Inventory inv = Bukkit.createInventory(null, 9 * 6, c(getConfig.getString("title")));

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
            if (getConfig.getBoolean("items.empty.use-head")) {
                item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.empty.texture")));
            } else {
                item = new ItemBuilder(Material.getMaterial(getConfig.getString("items.empty.id")), 1,
                        (byte) getConfig.getInt("items.empty.data"));
            }
            List<String> lore = getConfig.getStringList("items.empty.lore");
            lore.replaceAll(Utils::c);
            item.setName(c(getConfig.getString("items.empty.name"))).setLore(lore).
                    setGlow(getConfig.getBoolean("items.empty.glow"));
            int slot = getConfig.getInt("items.empty.slot");

            inv.setItem(slot, item.toItemStack());
        }else{

            for (DoubleBet doubleBet : sorted) {

                if(invs.get(p).get(numPag).getItem(16) != null) invLine = 19;
                if(invs.get(p).get(numPag).getItem(25) != null) invLine = 28;
                if(invs.get(p).get(numPag).getItem(34) != null) invLine = 37;
                if(invs.get(p).get(numPag).getItem(43) != null) {

                    Inventory newInv = Bukkit.createInventory(null, 9 * 6, c(getConfig.getString("title")));

                    ItemBuilder next;
                    if (getConfig.getBoolean("items.next-page.use-head"))
                        next = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.next-page.texture")));
                    else next = new ItemBuilder(Material.getMaterial(getConfig.getString("items.next-page.id")),1,
                            (byte) getConfig.getInt("items.next-page.data"));
                    List<String> nLore = getConfig.getStringList("items.next-page.lore");
                    nLore.replaceAll(Utils::c);
                    next.setName(c(getConfig.getString("items.next-page.name"))).
                            setLore(nLore).setGlow(getConfig.getBoolean("items.next-page.glow"));
                    NBTTag.setNBTString(next.toItemStack().getItemMeta(), "WBlaze_doublePag", ""+(numPag+1));
                    int slot = getConfig.getInt("items.next-page.slot");

                    ItemBuilder ant;
                    if (getConfig.getBoolean("items.previous-page.use-head"))
                        ant = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.previous-page.texture")));
                    else ant = new ItemBuilder(Material.getMaterial(getConfig.getString("items.previous-page.id")),1,
                            (byte) getConfig.getInt("items.previous-page.data"));
                    List<String> antLore = getConfig.getStringList("items.previous-page.lore");
                    antLore.replaceAll(Utils::c);
                    ant.setName(c(getConfig.getString("items.previous-page.name"))).
                            setLore(antLore).setGlow(getConfig.getBoolean("items.previous-page.glow"));
                    NBTTag.setNBTString(ant.toItemStack().getItemMeta(), "WBlaze_doublePag", ""+numPag);
                    int antSlot = getConfig.getInt("items.previous-page.slot");

                    newInv.setItem(antSlot, ant.toItemStack());
                    invs.get(p).get(numPag).setItem(slot, next.toItemStack());

                    invs.get(p).add(newInv);
                    numPag++;
                    invLine = 10;
                    invSlot = 0;
                }

                ItemBuilder item;
                if(getConfig.getBoolean("items.player.use-head-player")){
                    item = new ItemBuilder(SkullCreator.itemFromName(doubleBet.getOwner()));
                }else if (getConfig.getBoolean("items.player.use-head"))
                    item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.player.texture")));
                else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items.player.id")),1,
                            (byte) getConfig.getInt("items.player.data"));

                double wonMoney = 0;
                double wonCash = 0;
                String status = c(getConfig.getString("items.player.status-waiting"));
                if(getDoubleManager().isStopped()){
                    if(getDoubleManager().getColorCache().get(13) == doubleBet.getColor()){
                        status = c(getConfig.getString("items.player.status-won").replace("{multiplier}", doubleBet.getColor() == DoubleColor.WHITE ? 14 + "" : 2 + ""));
                        wonMoney = doubleBet.getColor() == DoubleColor.WHITE ? doubleBet.getMoneyValue() * 14 : doubleBet.getMoneyValue() * 2;
                        wonCash = doubleBet.getColor() == DoubleColor.WHITE ? doubleBet.getCashValue() * 14 : doubleBet.getCashValue() * 2;
                    } else status = c(getConfig.getString("items.player.status-lost"));
                }

                String color;
                if(doubleBet.getColor() == DoubleColor.RED) color = c(getConfig.getString("items.player.bet-red"));
                else if (doubleBet.getColor() == DoubleColor.WHITE) color = c(getConfig.getString("items.player.bet-white"));
                else color = c(getConfig.getString("items.player.bet-black"));

                String finalStatus = status;
                double finalWonMoney = wonMoney;
                double finalWonCash = wonCash;
                getConfig.getStringList("items.player.lore").forEach(w -> item.addLoreLine(c(w.
                        replace("{nick}", doubleBet.getOwner()).
                        replace("{money}", letterFormatter(doubleBet.getMoneyValue())).
                        replace("{cash}", letterFormatter(doubleBet.getCashValue())).
                        replace("{color}", color).
                        replace("{status}", finalStatus).
                        replace("{total-won-money}", letterFormatter(finalWonMoney)).
                        replace("{total-won-cash}", letterFormatter(finalWonCash)).
                        replace("{id}", doubleBet.getBetId()))
                ));


                item.setName(c(getConfig.getString("items.player.name").replace("{nick}", doubleBet.getOwner()))).
                        setGlow(getConfig.getBoolean("items.player.glow"));

                if(invSlot == 7) invSlot = 0;

                invs.get(p).get(numPag).setItem(invLine+invSlot, item.toItemStack());
                invSlot++;
            }
        }

        for (String s : getConfig.getConfigurationSection("items").getKeys(false)) {

            if(s.equalsIgnoreCase("next-page") || s.equalsIgnoreCase("previous-page") ||
                    s.equalsIgnoreCase("player") || s.equalsIgnoreCase("empty")) continue;

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head")) {
                item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            } else {
                item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1,
                        (byte) getConfig.getInt("items." + s + ".data"));
            }
            List<String> lore = getConfig.getStringList("items." + s + ".lore");
            lore.replaceAll(Utils::c);
            String name = getConfig.getString("items." + s + ".name");
            if(!name.isEmpty()) item.setName(c(name));
            item.setLore(lore).setGlow(getConfig.getBoolean("items." + s + ".glow"));
            int slot = getConfig.getInt("items." + s + ".slot");

            for (int i = 0; i < invs.get(p).size(); i++) {
                invs.get(p).get(i).setItem(slot, item.toItemStack());
            }
        }

        p.openInventory(invs.get(p).get(0));

        getDoubleManager().getGamblersGuiOpen().add(p.getName());
    }

}
