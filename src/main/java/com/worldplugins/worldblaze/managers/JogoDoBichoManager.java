package com.worldplugins.worldblaze.managers;

import com.worldplugins.worldblaze.enums.Bicho;
import com.worldplugins.worldblaze.objects.JogoDoBichoBet;
import com.worldplugins.worldblaze.utils.ItemBuilder;
import com.worldplugins.worldblaze.utils.SkullCreator;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.sendDebugMessage;

public class JogoDoBichoManager {

    private final List<JogoDoBichoBet> jogoDoBichoBets;
    private final Map<String, Inventory> seeingMap;
    private final Random rand;

    public JogoDoBichoManager() {

        this.jogoDoBichoBets = new ArrayList<>();
        this.seeingMap = new HashMap<>();

        this.rand = new Random();
    }

    public List<JogoDoBichoBet> getJogoDoBichoBets() {
        return jogoDoBichoBets;
    }

    public Map<String, Inventory> getSeeingMap() {
        return seeingMap;
    }

    public ItemStack getHeadLastPlayer(){

        String lastPlayer = getLastPlayer();
        if(Objects.equals(lastPlayer, "-/-")) return SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654");
        else return SkullCreator.itemFromName(lastPlayer);
    }

    public ItemStack getHeadLastPlayer(Bicho bicho){
        String lastPlayer = getAnimalLastPlayer(bicho);
        if(Objects.equals(lastPlayer, "-/-")) return SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654");
        else return SkullCreator.itemFromName(lastPlayer);
    }

    public String getLastPlayer(){
        if(jogoDoBichoBets.isEmpty()) return "-/-";

        return jogoDoBichoBets.get(jogoDoBichoBets.size() - 1).getOwner();
    }

    public double getTotalBetCash(){
        double sum = 0.0;

        for (JogoDoBichoBet bet : jogoDoBichoBets) {
            sum += bet.getCashValue();
        }

        return sum;
    }

    public double getTotalBetMoney(){
        double sum = 0.0;

        for (JogoDoBichoBet bet : jogoDoBichoBets) {
            sum += bet.getMoneyValue();
        }

        return sum;
    }

    public String getAnimalLastPlayer(Bicho bicho){

        if(bicho == null) return "-/-";

        if(jogoDoBichoBets.isEmpty()) return "-/-";

        List<JogoDoBichoBet> collect = jogoDoBichoBets.stream().filter(w -> w.getAnimal() == bicho).collect(Collectors.toList());

        if(collect.isEmpty()) return "-/-";

        return collect.get(collect.size() - 1).getOwner();
    }

    public int getNumOfBets(Bicho bicho){

        if(bicho == null) return 0;

        if(jogoDoBichoBets.isEmpty()) return 0;

        List<JogoDoBichoBet> collect = jogoDoBichoBets.stream().filter(w -> w.getAnimal() == bicho).collect(Collectors.toList());

        return collect.size();
    }

    public double getAnimalTotalBetMoney(Bicho bicho){

        if(bicho == null) return 0;

        if(jogoDoBichoBets.isEmpty()) return 0;

        List<JogoDoBichoBet> collect = jogoDoBichoBets.stream().filter(w -> w.getAnimal() == bicho).collect(Collectors.toList());

        if(collect.isEmpty()) return 0;

        double d = 0;

        for (JogoDoBichoBet bet : collect) {
            d += bet.getMoneyValue();
        }

        return d;
    }

    public double getAnimalTotalBetCash(Bicho bicho){

        if(bicho == null) return 0;

        if(jogoDoBichoBets.isEmpty()) return 0;

        List<JogoDoBichoBet> collect = jogoDoBichoBets.stream().filter(w -> w.getAnimal() == bicho).collect(Collectors.toList());

        if(collect.isEmpty()) return 0;

        double d = 0;

        for (JogoDoBichoBet bet : collect) {
            d += bet.getCashValue();
        }

        return d;
    }

    public boolean isPlayerBet(String name, Bicho bicho){
        if(jogoDoBichoBets.isEmpty()) return false;

        return jogoDoBichoBets.stream().anyMatch(w -> w.getOwner().equalsIgnoreCase(name) && w.getAnimal() == bicho);
    }

    public Set<Bicho> getBlackListRigging(){
        Set<Bicho> blackList = new HashSet<>();

        if(!getInstance().getConfig().getBoolean("config.jogo-do-bicho.rigging.enabled")) return blackList;

        for (JogoDoBichoBet bet : jogoDoBichoBets) {

            if(bet.getCashValue() >= getInstance().getConfig().getDouble("config.jogo-do-bicho.rigging.cash") ||
                    bet.getMoneyValue() >= getInstance().getConfig().getDouble("config.jogo-do-bicho.rigging.money")){
                if(rand.nextDouble() <= getInstance().getConfig().getDouble("config.jogo-do-bicho.rigging.chance")){
                    blackList.add(bet.getAnimal());
                    sendDebugMessage("[Jogo do Bicho] Hit by the rigging");
                }
            }
        }

        return blackList;
    }

    public Bicho getRandomBicho(){
        List<Bicho> cache = new ArrayList<>(Arrays.asList(Bicho.values()));

        cache.removeAll(getBlackListRigging());

        if(cache.isEmpty()) return Bicho.VEADO;

        return cache.get(rand.nextInt(cache.size()));
    }

    public void savePlayers(){

        for (String s : getDataYml().getConfig().getConfigurationSection("bichos").getKeys(false)) {
            getDataYml().getConfig().set("bichos."+s, null);
            getDataYml().saveFile();
        }

        for (JogoDoBichoBet bet : jogoDoBichoBets) {

            getDataYml().getConfig().set("bichos."+bet.getOwner()+"."+bet.getAnimal().name()+".id", bet.getBetId());
            getDataYml().saveFile();
            getDataYml().getConfig().set("bichos."+bet.getOwner()+"."+bet.getAnimal().name()+".Money", bet.getMoneyValue());
            getDataYml().saveFile();
            getDataYml().getConfig().set("bichos."+bet.getOwner()+"."+bet.getAnimal().name()+".Cash", bet.getCashValue());
            getDataYml().saveFile();

        }

    }

    public void loadPlayers(){

        for (String s : getDataYml().getConfig().getConfigurationSection("bichos").getKeys(false)) {
            for (String user : getDataYml().getConfig().getConfigurationSection("bichos." + s).getKeys(false)) {

                JogoDoBichoBet bet = new JogoDoBichoBet(s,
                        getDataYml().getConfig().getString("bichos."+s+"."+user+".id"), Bicho.valueOf(user),
                        getDataYml().getConfig().getDouble("bichos."+s+"."+user+".Cash"),
                        getDataYml().getConfig().getDouble("bichos."+s+"."+user+".Money"));

                jogoDoBichoBets.add(bet);
            }
        }

    }

    public ItemStack getAnimalItem(Bicho bicho){

        FileConfiguration getConfig = getBichosGuiYml().getConfig();

        ItemBuilder item;
        if (getConfig.getBoolean("items." + bicho.name().toLowerCase() + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + bicho.name().toLowerCase() + ".texture")));
        else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + bicho.name().toLowerCase() + ".id")), 1, (byte) getConfig.getInt("items." + bicho.name().toLowerCase() + ".data"));

        return item.toItemStack();
    }

}
