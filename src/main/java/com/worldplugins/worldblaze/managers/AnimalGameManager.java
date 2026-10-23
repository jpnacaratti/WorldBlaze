package com.worldplugins.worldblaze.managers;

import com.worldplugins.worldblaze.enums.Animal;
import com.worldplugins.worldblaze.objects.AnimalGameBet;
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

public class AnimalGameManager {

    private final List<AnimalGameBet> animalGameBets;
    private final Map<String, Inventory> seeingMap;
    private final Random rand;

    public AnimalGameManager() {

        this.animalGameBets = new ArrayList<>();
        this.seeingMap = new HashMap<>();

        this.rand = new Random();
    }

    public List<AnimalGameBet> getAnimalGameBets() {
        return animalGameBets;
    }

    public Map<String, Inventory> getSeeingMap() {
        return seeingMap;
    }

    public ItemStack getHeadLastPlayer(){

        String lastPlayer = getLastPlayer();
        if(Objects.equals(lastPlayer, "-/-")) return SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654");
        else return SkullCreator.itemFromName(lastPlayer);
    }

    public ItemStack getHeadLastPlayer(Animal animal){
        String lastPlayer = getAnimalLastPlayer(animal);
        if(Objects.equals(lastPlayer, "-/-")) return SkullCreator.itemFromUrl("https://textures.minecraft.net/texture/afd2400002ad9fbbbd0066941eb5b1a384ab9b0e48a178ee96e4d129a5208654");
        else return SkullCreator.itemFromName(lastPlayer);
    }

    public String getLastPlayer(){
        if(animalGameBets.isEmpty()) return "-/-";

        return animalGameBets.get(animalGameBets.size() - 1).getOwner();
    }

    public double getTotalBetCash(){
        double sum = 0.0;

        for (AnimalGameBet bet : animalGameBets) {
            sum += bet.getCashValue();
        }

        return sum;
    }

    public double getTotalBetMoney(){
        double sum = 0.0;

        for (AnimalGameBet bet : animalGameBets) {
            sum += bet.getMoneyValue();
        }

        return sum;
    }

    public String getAnimalLastPlayer(Animal animal){

        if(animal == null) return "-/-";

        if(animalGameBets.isEmpty()) return "-/-";

        List<AnimalGameBet> collect = animalGameBets.stream().filter(w -> w.getAnimal() == animal).collect(Collectors.toList());

        if(collect.isEmpty()) return "-/-";

        return collect.get(collect.size() - 1).getOwner();
    }

    public int getNumOfBets(Animal animal){

        if(animal == null) return 0;

        if(animalGameBets.isEmpty()) return 0;

        List<AnimalGameBet> collect = animalGameBets.stream().filter(w -> w.getAnimal() == animal).collect(Collectors.toList());

        return collect.size();
    }

    public double getAnimalTotalBetMoney(Animal animal){

        if(animal == null) return 0;

        if(animalGameBets.isEmpty()) return 0;

        List<AnimalGameBet> collect = animalGameBets.stream().filter(w -> w.getAnimal() == animal).collect(Collectors.toList());

        if(collect.isEmpty()) return 0;

        double d = 0;

        for (AnimalGameBet bet : collect) {
            d += bet.getMoneyValue();
        }

        return d;
    }

    public double getAnimalTotalBetCash(Animal animal){

        if(animal == null) return 0;

        if(animalGameBets.isEmpty()) return 0;

        List<AnimalGameBet> collect = animalGameBets.stream().filter(w -> w.getAnimal() == animal).collect(Collectors.toList());

        if(collect.isEmpty()) return 0;

        double d = 0;

        for (AnimalGameBet bet : collect) {
            d += bet.getCashValue();
        }

        return d;
    }

    public boolean isPlayerBet(String name, Animal animal){
        if(animalGameBets.isEmpty()) return false;

        return animalGameBets.stream().anyMatch(w -> w.getOwner().equalsIgnoreCase(name) && w.getAnimal() == animal);
    }

    public Set<Animal> getBlackListRigging(){
        Set<Animal> blackList = new HashSet<>();

        if(!getInstance().getConfig().getBoolean("config.animal-game.rigging.enabled")) return blackList;

        for (AnimalGameBet bet : animalGameBets) {

            if(bet.getCashValue() >= getInstance().getConfig().getDouble("config.animal-game.rigging.cash") ||
                    bet.getMoneyValue() >= getInstance().getConfig().getDouble("config.animal-game.rigging.money")){
                if(rand.nextDouble() * 100 <= getInstance().getConfig().getDouble("config.animal-game.rigging.chance")){
                    blackList.add(bet.getAnimal());
                    sendDebugMessage("[Animal Game] Hit by the rigging");
                }
            }
        }

        return blackList;
    }

    public Animal getRandomAnimal(){
        List<Animal> cache = new ArrayList<>(Arrays.asList(Animal.values()));

        cache.removeAll(getBlackListRigging());

        if(cache.isEmpty()) return Animal.DEER;

        return cache.get(rand.nextInt(cache.size()));
    }

    public void savePlayers(){

        for (String s : getDataYml().getConfig().getConfigurationSection("animal-game").getKeys(false)) {
            getDataYml().getConfig().set("animal-game."+s, null);
            getDataYml().saveFile();
        }

        for (AnimalGameBet bet : animalGameBets) {

            getDataYml().getConfig().set("animal-game."+bet.getOwner()+"."+bet.getAnimal().name()+".id", bet.getBetId());
            getDataYml().saveFile();
            getDataYml().getConfig().set("animal-game."+bet.getOwner()+"."+bet.getAnimal().name()+".Money", bet.getMoneyValue());
            getDataYml().saveFile();
            getDataYml().getConfig().set("animal-game."+bet.getOwner()+"."+bet.getAnimal().name()+".Cash", bet.getCashValue());
            getDataYml().saveFile();

        }

    }

    public void loadPlayers(){

        for (String s : getDataYml().getConfig().getConfigurationSection("animal-game").getKeys(false)) {
            for (String user : getDataYml().getConfig().getConfigurationSection("animal-game." + s).getKeys(false)) {

                AnimalGameBet bet = new AnimalGameBet(s,
                        getDataYml().getConfig().getString("animal-game."+s+"."+user+".id"), Animal.valueOf(user),
                        getDataYml().getConfig().getDouble("animal-game."+s+"."+user+".Cash"),
                        getDataYml().getConfig().getDouble("animal-game."+s+"."+user+".Money"));

                animalGameBets.add(bet);
            }
        }

    }

    public ItemStack getAnimalItem(Animal animal){

        FileConfiguration getConfig = getAnimalsGuiYml().getConfig();

        ItemBuilder item;
        if (getConfig.getBoolean("items." + animal.name().toLowerCase() + ".use-head")) item = new ItemBuilder(SkullCreator.itemFromUrl(getConfig.getString("items." + animal.name().toLowerCase() + ".texture")));
        else item = new ItemBuilder(Material.getMaterial(getConfig.getString("items." + animal.name().toLowerCase() + ".id")), 1, (byte) getConfig.getInt("items." + animal.name().toLowerCase() + ".data"));

        return item.toItemStack();
    }

}
