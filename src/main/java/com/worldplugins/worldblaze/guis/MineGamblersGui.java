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

import static com.worldplugins.worldblaze.WorldBlaze.getMineManager;
import static com.worldplugins.worldblaze.WorldBlaze.getMinePlayersGuiYml;
import static com.worldplugins.worldblaze.utils.Utils.c;
import static com.worldplugins.worldblaze.utils.Utils.letterFormatter;

public class MineGamblersGui {

    public static Map<Player, ArrayList<Inventory>> invs = new HashMap<>();

    public static void create(Player p){
        FileConfiguration getConfig = getMinePlayersGuiYml().getConfig();

        Inventory inv = MenuHolder.create(MenuType.MINE_PLAYERS, 9 * 6, c(getConfig.getString("title")));

        List<MineBet> sorted = new ArrayList<>(getMineManager().getMineBets());

        sorted = sorted.stream().sorted(Comparator.comparingDouble(MineBet::getMid).reversed()).collect(Collectors.toList());

        ArrayList<Inventory> list = new ArrayList<>();
        list.add(inv);
        invs.remove(p);
        invs.put(p, list);
        int invLine = 10;
        int invSlot = 0;
        int numPage = 0;
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

            for (MineBet mineBet : sorted) {

                if(invs.get(p).get(numPage).getItem(16) != null) invLine = 19;
                if(invs.get(p).get(numPage).getItem(25) != null) invLine = 28;
                if(invs.get(p).get(numPage).getItem(34) != null) invLine = 37;
                if(invs.get(p).get(numPage).getItem(43) != null) {

                    Inventory newInv = MenuHolder.create(MenuType.MINE_PLAYERS, 9 * 6, c(getConfig.getString("title")));

                    ItemBuilder next;
                    if (getConfig.getBoolean("items.next-page.use-head"))
                        next = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.next-page.texture")));
                    else next = new ItemBuilder(Material.getMaterial(getConfig.getString("items.next-page.id")),1,
                            (byte) getConfig.getInt("items.next-page.data"));
                    List<String> nLore = getConfig.getStringList("items.next-page.lore");
                    nLore.replaceAll(Utils::c);
                    next.setName(c(getConfig.getString("items.next-page.name"))).
                            setLore(nLore).setGlow(getConfig.getBoolean("items.next-page.glow"));
                    NBTTag.setNBTString(next.toItemStack().getItemMeta(), "WBlaze_minePag", ""+(numPage+1));
                    int slot = getConfig.getInt("items.next-page.slot");

                    ItemBuilder prev;
                    if (getConfig.getBoolean("items.previous-page.use-head"))
                        prev = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.previous-page.texture")));
                    else prev = new ItemBuilder(Material.getMaterial(getConfig.getString("items.previous-page.id")),1,
                            (byte) getConfig.getInt("items.previous-page.data"));
                    List<String> prevLore = getConfig.getStringList("items.previous-page.lore");
                    prevLore.replaceAll(Utils::c);
                    prev.setName(c(getConfig.getString("items.previous-page.name"))).
                            setLore(prevLore).setGlow(getConfig.getBoolean("items.previous-page.glow"));
                    NBTTag.setNBTString(prev.toItemStack().getItemMeta(), "WBlaze_minePag", ""+numPage);
                    int prevSlot = getConfig.getInt("items.previous-page.slot");

                    newInv.setItem(prevSlot, prev.toItemStack());
                    invs.get(p).get(numPage).setItem(slot, next.toItemStack());

                    invs.get(p).add(newInv);
                    numPage++;
                    invLine = 10;
                    invSlot = 0;
                }

                ItemBuilder item;
                if(getConfig.getBoolean("items.player.use-head-player")){
                    item = new ItemBuilder(SkullCreator.itemFromName(mineBet.getOwner()));
                }else if (getConfig.getBoolean("items.player.use-head"))
                    item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items.player.texture")));
                else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items.player.id")),1,
                            (byte) getConfig.getInt("items.player.data"));

                double wonMoney = mineBet.getMoneyValue() * mineBet.getMultiplier();
                double wonCash = mineBet.getCashValue() * mineBet.getMultiplier();

                getConfig.getStringList("items.player.lore").forEach(w -> item.addLoreLine(c(w.
                        replace("{nick}", mineBet.getOwner()).
                        replace("{money}", letterFormatter(mineBet.getMoneyValue())).
                        replace("{cash}", letterFormatter(mineBet.getCashValue())).
                        replace("{mines}", mineBet.getNumBombs() + "").
                        replace("{slots}", mineBet.getClickedSlots().size() + "").
                        replace("{multiplier}", getMineManager().formatValue(mineBet.getMultiplier())).
                        replace("{total-won-money}", letterFormatter(wonMoney)).
                        replace("{total-won-cash}", letterFormatter(wonCash)).
                        replace("{id}", mineBet.getBetId()))
                ));


                item.setName(c(getConfig.getString("items.player.name").replace("{nick}", mineBet.getOwner()))).
                        setGlow(getConfig.getBoolean("items.player.glow"));

                NBTTag.setNBTString(item.toItemStack().getItemMeta(), "WBlaze_mineJogador", mineBet.getOwner());

                if(invSlot == 7) invSlot = 0;

                invs.get(p).get(numPage).setItem(invLine+invSlot, item.toItemStack());
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

        getMineManager().getPlayersGuiOpen().add(p.getName());
    }

}
