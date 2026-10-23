package com.worldplugins.worldblaze.managers;

import com.worldplugins.worldblaze.guis.MinePlayersGui;
import com.worldplugins.worldblaze.objects.MineBet;
import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class MineManager {

    // INITIAL VARIABLES THAT WILL NOT CHANGE
    private final List<MineBet> mineBets;
    private final Map<String, Inventory> mainInventories;
    private final Map<Integer, Double> multipliers;
    private final Set<String> playersGuiOpen;
    private final List<Integer> availableSlots;
    private final String menuTitle, mineMenuTitle;
    private final int mineLines, minSlots, riggingChanceMultiplier;
    private final double alertCash, alertMoney;
    private final Random rand;
    private final boolean rigging;
    private final LinkedHashMap<Double, Double> cashRigging, moneyRigging;

    // VARIABLES THAT WILL CHANGE AS THE MINE PROGRESSES
    private double totalBetCash, totalBetMoney;

    // VARIABLES THAT WILL NOT CHANGE UNDER ANY CIRCUMSTANCES
    private final ItemBuilder mainPlayersItem, hiddenItem, minedItem, bombItem, leftItem;
    private final String mainPlayersName, leftName, leftFormat;
    private final List<String> mainPlayersLore, leftLore;
    private final boolean mainUsePlayersHead;
    private final int mainPlayersSlot, leftSlot;

    public MineManager() {

        this.mineBets = new ArrayList<>();
        this.playersGuiOpen = new HashSet<>();
        this.availableSlots = new ArrayList<>(getInstance().getConfig().getIntegerList("config.mine.game-slots"));
        this.mainInventories = new HashMap<>();
        this.multipliers = new HashMap<>();
        this.cashRigging = new LinkedHashMap<>();
        this.moneyRigging = new LinkedHashMap<>();

        this.riggingChanceMultiplier = getInstance().getConfig().getInt("config.mine.rigging.chance-multiplier");

        this.rigging = getInstance().getConfig().getBoolean("config.mine.rigging.enabled");

        this.rand = new Random();

        this.mineLines = getInstance().getConfig().getInt("config.mine.menu-rows");
        this.minSlots = getInstance().getConfig().getInt("config.mine.min-slots-to-exit");

        this.alertCash = getInstance().getConfig().getDouble("config.mine.cash-alert");
        this.alertMoney = getInstance().getConfig().getDouble("config.mine.money-alert");

        for (String s : getInstance().getConfig().getStringList("config.mine.multipliers")) {
            String[] split = s.split(";");

            multipliers.put(Integer.parseInt(split[0]), Double.parseDouble(split[1]));
        }

        for (String s : getInstance().getConfig().getStringList("config.mine.rigging.money")) {
            String[] split = s.split(";");

            moneyRigging.put(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
        }
        List<Map.Entry<Double, Double>> moneyEntryList = new ArrayList<>(moneyRigging.entrySet());
        moneyEntryList.sort(Map.Entry.comparingByKey());
        Collections.reverse(moneyEntryList);
        moneyRigging.clear();
        moneyEntryList.forEach(w -> moneyRigging.put(w.getKey(), w.getValue()));

        for (String s : getInstance().getConfig().getStringList("config.mine.rigging.cash")) {
            String[] split = s.split(";");

            cashRigging.put(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
        }
        List<Map.Entry<Double, Double>> cashEntryList = new ArrayList<>(cashRigging.entrySet());
        cashEntryList.sort(Map.Entry.comparingByKey());
        Collections.reverse(cashEntryList);
        cashRigging.clear();
        cashEntryList.forEach(w -> cashRigging.put(w.getKey(), w.getValue()));

        FileConfiguration getMineConfig = getMineGuiYml().getConfig();

        this.mineMenuTitle = c(getMineConfig.getString("title"));

        if (getMineConfig.getBoolean("hidden.use-head")) hiddenItem = new ItemBuilder(SkullCreator.itemFromUrl(getMineConfig.getString("hidden.texture")));
        else hiddenItem = new ItemBuilder(Material.getMaterial(getMineConfig.getString("hidden.id")), 1, (byte) getMineConfig.getInt("hidden.data"));
        hiddenItem.setGlow(getMineConfig.getBoolean("hidden.glow")).setName(c(getMineConfig.getString("hidden.name")));

        getMineConfig.getStringList("hidden.lore").forEach(w -> hiddenItem.addLoreLine(c(w)));


        if (getMineConfig.getBoolean("revealed.use-head")) minedItem = new ItemBuilder(SkullCreator.itemFromUrl(getMineConfig.getString("revealed.texture")));
        else minedItem = new ItemBuilder(Material.getMaterial(getMineConfig.getString("revealed.id")), 1, (byte) getMineConfig.getInt("revealed.data"));
        minedItem.setGlow(getMineConfig.getBoolean("revealed.glow")).setName(c(getMineConfig.getString("revealed.name")));

        getMineConfig.getStringList("revealed.lore").forEach(w -> minedItem.addLoreLine(c(w)));


        if (getMineConfig.getBoolean("bomb.use-head")) bombItem = new ItemBuilder(SkullCreator.itemFromUrl(getMineConfig.getString("bomb.texture")));
        else bombItem = new ItemBuilder(Material.getMaterial(getMineConfig.getString("bomb.id")), 1, (byte) getMineConfig.getInt("bomb.data"));
        bombItem.setGlow(getMineConfig.getBoolean("bomb.glow")).setName(c(getMineConfig.getString("bomb.name")));

        getMineConfig.getStringList("bomb.lore").forEach(w -> bombItem.addLoreLine(c(w)));


        if (getMineConfig.getBoolean("items.exit.use-head")) leftItem = new ItemBuilder(SkullCreator.itemFromUrl(getMineConfig.getString("items.exit.texture")));
        else leftItem = new ItemBuilder(Material.getMaterial(getMineConfig.getString("items.exit.id")), 1, (byte) getMineConfig.getInt("items.exit.data"));
        leftItem.setGlow(getMineConfig.getBoolean("items.exit.glow"));

        this.leftLore = getMineConfig.getStringList("items.exit.lore");
        this.leftName = c(getMineConfig.getString("items.exit.name"));
        this.leftFormat = getMineConfig.getString("items.exit.multiplier-format");

        this.leftSlot = getMineConfig.getInt("items.exit.slot");



        FileConfiguration getMainConfig = getMainMineGuiYml().getConfig();

        this.menuTitle = c(getMainConfig.getString("title"));

        if (getMainConfig.getBoolean("items.players.use-head-last-player")) mainPlayersItem = new ItemBuilder(SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654"));
        else if (getMainConfig.getBoolean("items.players.use-head")) mainPlayersItem = new ItemBuilder(SkullCreator.itemFromUrl(getMainConfig.getString("items.players.texture")));
        else mainPlayersItem = new ItemBuilder(Material.getMaterial(getMainConfig.getString("items.players.id")), 1, (byte) getMainConfig.getInt("items.players.data"));
        mainPlayersItem.setGlow(getMainConfig.getBoolean("items.players.glow"));

        this.mainPlayersLore = getMainConfig.getStringList("items.players.lore");
        this.mainPlayersName = c(getMainConfig.getString("items.players.name"));

        this.mainPlayersSlot = getMainConfig.getInt("items.players.slot");

        this.mainUsePlayersHead = getMainConfig.getBoolean("items.players.use-head-last-player");

    }

    public List<Integer> getAvailableSlots() {
        return availableSlots;
    }

    public Map<Integer, Double> getMultipliers() {
        return multipliers;
    }

    public ItemStack getHiddenItem() {
        return hiddenItem.toItemStack();
    }

    public ItemStack getMinedItem() {
        return minedItem.toItemStack();
    }

    public ItemStack getBombItem() {
        return bombItem.toItemStack();
    }

    public ItemStack getLeftItem(MineBet mineBet) {
        ItemBuilder clone = leftItem.clone();

        clone.setName(leftName);

        for (String s : leftLore) {
            if(s.contains("{spectators}")){
                for (String spectator : mineBet.getSpectators()) {
                    clone.addLoreLine(c(s.replace("{spectators}", spectator)));
                }
            }else{
                clone.addLoreLine(c(s.replace("{slots}", mineBet.getClickedSlots().size()+"").
                        replace("{multiplier}", formatValue(mineBet.getMultiplier()))));
            }
        }

        return clone.toItemStack();
    }

    public ItemStack getMainPlayersItem() {
        ItemBuilder clone;
        if(!mainUsePlayersHead || mineBets.size() == 0) clone = mainPlayersItem.clone();
        else clone = new ItemBuilder(SkullCreator.itemFromName(getLastPlayer()));

        clone.setName(mainPlayersName);

        mainPlayersLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{games}", mineBets.size()+"").
                replace("{last-player}", getLastPlayer()).
                replace("{total-money}", letterFormatter(totalBetMoney)).
                replace("{total-cash}", letterFormatter(totalBetCash)))));

        return clone.toItemStack();
    }

    public int getMainPlayersSlot() {
        return mainPlayersSlot;
    }

    public String getLastPlayer() {

        if (mineBets.size() == 0) return "-/-";

        return mineBets.get(mineBets.size() - 1).getOwner();
    }

    public MineBet getMineBet(String player){
        return mineBets.stream().filter(w -> w.getOwner().equalsIgnoreCase(player)).findFirst().orElse(null);
    }

    public MineBet getMineBet(Inventory inventory){
        return mineBets.stream().filter(w -> w.getGameInventory() == inventory).findFirst().orElse(null);
    }

    public int getMinSlots() {
        return minSlots;
    }

    public String formatValue(double value){
        return String.format("%."+leftFormat+"f", value);
    }

    public Map<String, Inventory> getMainInventories() {
        return mainInventories;
    }

    public Set<String> getPlayersGuiOpen() {
        return playersGuiOpen;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public String getMineMenuTitle() {
        return mineMenuTitle;
    }

    public int getLeftSlot() {
        return leftSlot;
    }

    public double getAlertCash() {
        return alertCash;
    }

    public double getAlertMoney() {
        return alertMoney;
    }

    public boolean isPlayerInGame(String player){
        if(mineBets.size() == 0) return false;

        return mineBets.stream().anyMatch(w -> w.getOwner().equalsIgnoreCase(player));
    }

    public Inventory createMineInventory(MineBet bet){

        FileConfiguration getConfig = getMineGuiYml().getConfig();

        Inventory inv = Bukkit.createInventory(null, 9 * mineLines, mineMenuTitle);

        for(String s : getConfig.getConfigurationSection("items").getKeys(false)){

            if(s.equalsIgnoreCase("exit")) continue;

            ItemBuilder item;
            if (getConfig.getBoolean("items." + s + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + s + ".texture")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + s + ".id")), 1, (byte) getConfig.getInt("items." + s + ".data"));
            item.setName(c(getConfig.getString("items." + s + ".name"))).setGlow(getConfig.getBoolean("items." + s + ".glow"));

            getConfig.getStringList("items." + s + ".lore").forEach(w -> item.addLoreLine(c(w)));

            int slot = getConfig.getInt("items." + s + ".slot");

            inv.setItem(slot, item.toItemStack());
        }

        for (Integer slot : availableSlots) {
            inv.setItem(slot, getHiddenItem());
        }

        inv.setItem(leftSlot, getLeftItem(bet));

        return inv;
    }

    public List<MineBet> getMineBets() {
        return mineBets;
    }

    public int getRiggingChanceMultiplier() {
        return riggingChanceMultiplier;
    }

    public void setTotalBetMoney(){
        double toR = 0;

        for (MineBet mineBet : mineBets) {
            toR += mineBet.getMoneyValue();
        }

        this.totalBetMoney = toR;
    }

    public void setTotalBetCash(){
        double toR = 0;

        for (MineBet mineBet : mineBets) {
            toR += mineBet.getCashValue();
        }

        this.totalBetCash = toR;
    }

    public void addSlots(List<Integer> list){
        while (true){

            int random = availableSlots.get(rand.nextInt(availableSlots.size()));

            if(!list.contains(random)){
                list.add(random);
                return;
            }
        }
    }

    public int getRandomAvailableSlot(MineBet mineBet){
        while (true){

            int random = availableSlots.get(rand.nextInt(availableSlots.size()));

            if(!mineBet.getClickedSlots().contains(random) && !mineBet.getBombSlots().contains(random)){
                return random;
            }
        }
    }

    public void minedBomb(Player p, MineBet mineBet, Inventory inv){
        p.getWorld().playEffect(p.getLocation(), Effect.EXPLOSION_LARGE, 50, 50);
        p.getWorld().playEffect(p.getLocation(), Effect.EXPLOSION_LARGE, 50, 50);
        p.getWorld().playEffect(p.getLocation(), Effect.EXPLOSION_LARGE, 50, 50);
        p.getWorld().playSound(p.getLocation(), Sound.EXPLODE, 50, 50);

        for (Integer bombSlot : mineBet.getBombSlots()) {
            inv.setItem(bombSlot, getMineManager().getBombItem());
        }

        p.sendMessage("§cYou hit a mine... Unfortunately you lost everything you bet. You can close this menu now");

        getMineManager().getMineBets().remove(mineBet);

        getMineManager().setTotalBetMoney();
        getMineManager().setTotalBetCash();

        for (String spectator : mineBet.getSpectators()) {
            Player player = Bukkit.getPlayer(spectator);

            if(player != null){
                player.sendMessage("§cThe player who was playing this match hit a mine, this menu will no longer be updated!");
                player.sendMessage("§cYou can close it now!");
                player.updateInventory();
            }
        }

        for (String s : getMineManager().getPlayersGuiOpen()) {
            Player player = Bukkit.getPlayer(s);

            if(player != null) MinePlayersGui.create(player);
        }

        for (Map.Entry<String, Inventory> entry : mainInventories.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());

            if(player != null){
                entry.getValue().setItem(getMainPlayersSlot(), getMainPlayersItem());
                player.updateInventory();
            }
        }
    }

    public MineBet getWhoPlayerIsSeen(String player){
        return mineBets.stream().filter(w -> w.getSpectators().contains(player)).findFirst().orElse(null);
    }

    public double getRiggingChance(MineBet mineBet){
        if(!rigging) return 0.0;

        boolean cashPass = false;
        double cashPercent = 0.0;
        if (mineBet.getCashValue() > 0) {
            for (Map.Entry<Double, Double> entry : cashRigging.entrySet()) {
                if (mineBet.getCashValue() >= entry.getKey()){
                    cashPass = true;
                    cashPercent = entry.getValue();
                    sendDebugMessage("[Mine] Reached the rigging threshold (cash): " + entry.getValue());
                    break;
                }
            }
        }

        if(cashPass) return cashPercent;

        boolean moneyPass = false;
        double moneyPercent = 0.0;
        if (mineBet.getMoneyValue() > 0) {
            for (Map.Entry<Double, Double> entry : moneyRigging.entrySet()) {
                if (mineBet.getMoneyValue() >= entry.getKey()){
                    moneyPass = true;
                    moneyPercent = entry.getValue();
                    sendDebugMessage("[Mine] Reached the rigging threshold (money): " + entry.getValue());
                    break;
                }
            }
        }

        if(moneyPass) return moneyPercent;

        return 0.0;
    }

}
