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

    // VARIAVEIS INICIAIS QUE NÃO VÃO MUDAR
    private final List<MineBet> mineBets;
    private final Map<String, Inventory> principalInventories;
    private final Map<Integer, Double> multipliers;
    private final Set<String> playersGuiOpen;
    private final List<Integer> availableSlots;
    private final String menuTitle, mineMenuTitle;
    private final int mineLines, minSlots, dopChanceMultiplier;
    private final double alertCash, alertMoney;
    private final Random rand;
    private final boolean dop;
    private final LinkedHashMap<Double, Double> cashDop, moneyDop;

    // VARIAVEIS QUE VAO MUDAR CONFORME O MINE
    private double totalBetCash, totalBetMoney;

    // VARIAVEIS QUE NAO VAO MUDAR EM HIPOTESE ALGUMA
    private final ItemBuilder principalPlayersItem, hiddenItem, minedItem, bombItem, leftItem;
    private final String principalPlayersName, leftName, leftFormat;
    private final List<String> principalPlayersLore, leftLore;
    private final boolean principalUsePlayersHead;
    private final int principalPlayersSlot, leftSlot;

    public MineManager() {

        this.mineBets = new ArrayList<>();
        this.playersGuiOpen = new HashSet<>();
        this.availableSlots = new ArrayList<>(getInstance().getConfig().getIntegerList("Config.Mine.Slots-jogo"));
        this.principalInventories = new HashMap<>();
        this.multipliers = new HashMap<>();
        this.cashDop = new LinkedHashMap<>();
        this.moneyDop = new LinkedHashMap<>();

        this.dopChanceMultiplier = getInstance().getConfig().getInt("Config.Mine.Dopagem.Multiplicador-de-chance");

        this.dop = getInstance().getConfig().getBoolean("Config.Mine.Dopagem.Ativar");

        this.rand = new Random();

        this.mineLines = getInstance().getConfig().getInt("Config.Mine.Linhas-menu");
        this.minSlots = getInstance().getConfig().getInt("Config.Mine.Minimo-slots-sair");

        this.alertCash = getInstance().getConfig().getDouble("Config.Mine.Alerta-cash");
        this.alertMoney = getInstance().getConfig().getDouble("Config.Mine.Alerta-money");

        for (String s : getInstance().getConfig().getStringList("Config.Mine.Multiplicadores")) {
            String[] split = s.split(";");

            multipliers.put(Integer.parseInt(split[0]), Double.parseDouble(split[1]));
        }

        for (String s : getInstance().getConfig().getStringList("Config.Mine.Dopagem.Money")) {
            String[] split = s.split(";");

            moneyDop.put(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
        }
        List<Map.Entry<Double, Double>> moneyEntryList = new ArrayList<>(moneyDop.entrySet());
        moneyEntryList.sort(Map.Entry.comparingByKey());
        Collections.reverse(moneyEntryList);
        moneyDop.clear();
        moneyEntryList.forEach(w -> moneyDop.put(w.getKey(), w.getValue()));

        for (String s : getInstance().getConfig().getStringList("Config.Mine.Dopagem.Cash")) {
            String[] split = s.split(";");

            cashDop.put(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
        }
        List<Map.Entry<Double, Double>> cashEntryList = new ArrayList<>(cashDop.entrySet());
        cashEntryList.sort(Map.Entry.comparingByKey());
        Collections.reverse(cashEntryList);
        cashDop.clear();
        cashEntryList.forEach(w -> cashDop.put(w.getKey(), w.getValue()));

        FileConfiguration getMineConfig = getMineGuiYml().getConfig();

        this.mineMenuTitle = c(getMineConfig.getString("Titulo"));

        if (getMineConfig.getBoolean("oculto.Usar-head")) hiddenItem = new ItemBuilder(SkullCreator.itemFromUrl(getMineConfig.getString("oculto.Textura")));
        else hiddenItem = new ItemBuilder(Material.getMaterial(getMineConfig.getString("oculto.Id")), 1, (byte) getMineConfig.getInt("oculto.Data"));
        hiddenItem.setGlow(getMineConfig.getBoolean("oculto.Glow")).setName(c(getMineConfig.getString("oculto.Nome")));

        getMineConfig.getStringList("oculto.Lore").forEach(w -> hiddenItem.addLoreLine(c(w)));


        if (getMineConfig.getBoolean("revelado.Usar-head")) minedItem = new ItemBuilder(SkullCreator.itemFromUrl(getMineConfig.getString("revelado.Textura")));
        else minedItem = new ItemBuilder(Material.getMaterial(getMineConfig.getString("revelado.Id")), 1, (byte) getMineConfig.getInt("revelado.Data"));
        minedItem.setGlow(getMineConfig.getBoolean("revelado.Glow")).setName(c(getMineConfig.getString("revelado.Nome")));

        getMineConfig.getStringList("revelado.Lore").forEach(w -> minedItem.addLoreLine(c(w)));


        if (getMineConfig.getBoolean("bomba.Usar-head")) bombItem = new ItemBuilder(SkullCreator.itemFromUrl(getMineConfig.getString("bomba.Textura")));
        else bombItem = new ItemBuilder(Material.getMaterial(getMineConfig.getString("bomba.Id")), 1, (byte) getMineConfig.getInt("bomba.Data"));
        bombItem.setGlow(getMineConfig.getBoolean("bomba.Glow")).setName(c(getMineConfig.getString("bomba.Nome")));

        getMineConfig.getStringList("bomba.Lore").forEach(w -> bombItem.addLoreLine(c(w)));


        if (getMineConfig.getBoolean("Itens.sair.Usar-head")) leftItem = new ItemBuilder(SkullCreator.itemFromUrl(getMineConfig.getString("Itens.sair.Textura")));
        else leftItem = new ItemBuilder(Material.getMaterial(getMineConfig.getString("Itens.sair.Id")), 1, (byte) getMineConfig.getInt("Itens.sair.Data"));
        leftItem.setGlow(getMineConfig.getBoolean("Itens.sair.Glow"));

        this.leftLore = getMineConfig.getStringList("Itens.sair.Lore");
        this.leftName = c(getMineConfig.getString("Itens.sair.Nome"));
        this.leftFormat = getMineConfig.getString("Itens.sair.Formatacao-multiplicador");

        this.leftSlot = getMineConfig.getInt("Itens.sair.Slot");



        FileConfiguration getPrincipalConfig = getPrincipalMineGuiYml().getConfig();

        this.menuTitle = c(getPrincipalConfig.getString("Titulo"));

        if (getPrincipalConfig.getBoolean("Itens.jogadores.Usar-head-ultimo-player")) principalPlayersItem = new ItemBuilder(SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654"));
        else if (getPrincipalConfig.getBoolean("Itens.jogadores.Usar-head")) principalPlayersItem = new ItemBuilder(SkullCreator.itemFromUrl(getPrincipalConfig.getString("Itens.jogadores.Textura")));
        else principalPlayersItem = new ItemBuilder(Material.getMaterial(getPrincipalConfig.getString("Itens.jogadores.Id")), 1, (byte) getPrincipalConfig.getInt("Itens.jogadores.Data"));
        principalPlayersItem.setGlow(getPrincipalConfig.getBoolean("Itens.jogadores.Glow"));

        this.principalPlayersLore = getPrincipalConfig.getStringList("Itens.jogadores.Lore");
        this.principalPlayersName = c(getPrincipalConfig.getString("Itens.jogadores.Nome"));

        this.principalPlayersSlot = getPrincipalConfig.getInt("Itens.jogadores.Slot");

        this.principalUsePlayersHead = getPrincipalConfig.getBoolean("Itens.jogadores.Usar-head-ultimo-player");

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
            if(s.contains("{espectadores}")){
                for (String spectator : mineBet.getSpectators()) {
                    clone.addLoreLine(c(s.replace("{espectadores}", spectator)));
                }
            }else{
                clone.addLoreLine(c(s.replace("{slots}", mineBet.getClickedSlots().size()+"").
                        replace("{multiplicador}", formatValue(mineBet.getMultiplier()))));
            }
        }

        return clone.toItemStack();
    }

    public ItemStack getPrincipalPlayersItem() {
        ItemBuilder clone;
        if(!principalUsePlayersHead || mineBets.size() == 0) clone = principalPlayersItem.clone();
        else clone = new ItemBuilder(SkullCreator.itemFromName(getLastPlayer()));

        clone.setName(principalPlayersName);

        principalPlayersLore.forEach(w -> clone.addLoreLine(c(w.
                replace("{jogos}", mineBets.size()+"").
                replace("{last_player}", getLastPlayer()).
                replace("{total_money}", letterFormatter(totalBetMoney)).
                replace("{total_cash}", letterFormatter(totalBetCash)))));

        return clone.toItemStack();
    }

    public int getPrincipalPlayersSlot() {
        return principalPlayersSlot;
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

    public Map<String, Inventory> getPrincipalInventories() {
        return principalInventories;
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

        for(String s : getConfig.getConfigurationSection("Itens").getKeys(false)){

            if(s.equalsIgnoreCase("sair")) continue;

            ItemBuilder item;
            if (getConfig.getBoolean("Itens." + s + ".Usar-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("Itens." + s + ".Textura")));
            else item = new ItemBuilder(Material.getMaterial(getConfig.getString("Itens." + s + ".Id")), 1, (byte) getConfig.getInt("Itens." + s + ".Data"));
            item.setName(c(getConfig.getString("Itens." + s + ".Nome"))).setGlow(getConfig.getBoolean("Itens." + s + ".Glow"));

            getConfig.getStringList("Itens." + s + ".Lore").forEach(w -> item.addLoreLine(c(w)));

            int slot = getConfig.getInt("Itens." + s + ".Slot");

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

    public int getDopChanceMultiplier() {
        return dopChanceMultiplier;
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

        p.sendMessage("§cVocê minerou uma bomba... Infelizmente perdeu tudo que apostou. Já pode fechar esse menu");

        getMineManager().getMineBets().remove(mineBet);

        getMineManager().setTotalBetMoney();
        getMineManager().setTotalBetCash();

        for (String spectator : mineBet.getSpectators()) {
            Player player = Bukkit.getPlayer(spectator);

            if(player != null){
                player.sendMessage("§cO jogador que estava jogando essa partida minerou uma bomba, esse menu não será mais atualizado!");
                player.sendMessage("§cVocê já pode fechar ele!");
                player.updateInventory();
            }
        }

        for (String s : getMineManager().getPlayersGuiOpen()) {
            Player player = Bukkit.getPlayer(s);

            if(player != null) MinePlayersGui.create(player);
        }

        for (Map.Entry<String, Inventory> entry : principalInventories.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());

            if(player != null){
                entry.getValue().setItem(getPrincipalPlayersSlot(), getPrincipalPlayersItem());
                player.updateInventory();
            }
        }
    }

    public MineBet getWhoPlayerIsSeen(String player){
        return mineBets.stream().filter(w -> w.getSpectators().contains(player)).findFirst().orElse(null);
    }

    public double getDopChance(MineBet mineBet){
        if(!dop) return 0.0;

        boolean cashPass = false;
        double cashPrct = 0.0;
        if (mineBet.getCashValue() > 0) {
            for (Map.Entry<Double, Double> entry : cashDop.entrySet()) {
                if (mineBet.getCashValue() >= entry.getKey()){
                    cashPass = true;
                    cashPrct = entry.getValue();
                    sendDebugMessage("[Mine] Passou na dopagem (cash): " + entry.getValue());
                    break;
                }
            }
        }

        if(cashPass) return cashPrct;

        boolean moneyPass = false;
        double moneyPrct = 0.0;
        if (mineBet.getMoneyValue() > 0) {
            for (Map.Entry<Double, Double> entry : moneyDop.entrySet()) {
                if (mineBet.getMoneyValue() >= entry.getKey()){
                    moneyPass = true;
                    moneyPrct = entry.getValue();
                    sendDebugMessage("[Mine] Passou na dopagem (money): " + entry.getValue());
                    break;
                }
            }
        }

        if(moneyPass) return moneyPrct;

        return 0.0;
    }

}
