package com.worldplugins.worldblaze.listeners;

import com.worldplugins.worldblaze.enums.Animal;
import com.worldplugins.worldblaze.guis.*;
import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.objects.AnimalGameBet;
import com.worldplugins.worldblaze.utils.NBTTag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class InventoryClickAnimalGameBet implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!MenuHolder.handleClick(e, MenuType.ANIMAL_GAME_BET)) return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;

        FileConfiguration getConfig = getAnimalsBetGuiYml().getConfig();

        Player p = (Player) e.getWhoClicked();

        BlazeUser user = getManager().getBlazeUser(p.getName());

        String nbtAnimal = NBTTag.getNBTString(e.getCurrentItem().getItemMeta(), "WBlaze_animal");

        if(nbtAnimal == null) return;

        Animal animal;
        try {
            animal = Animal.valueOf(nbtAnimal.toUpperCase());
        }catch (IllegalArgumentException ex){
            p.sendMessage("§cError, contact Miinero ("+nbtAnimal+")");
            return;
        }

        if(e.getSlot() == getConfig.getInt("items.money.slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("messages.animal-game-typing-money").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.animalGameMoneyList.put(p.getName(), animal);
        }

        if(e.getSlot() == getConfig.getInt("items.cash.slot")){
            p.closeInventory();

            getInstance().getConfig().getStringList("messages.animal-game-typing-cash").forEach(w -> p.sendMessage(c(w)));

            AsyncPlayerChat.animalGameCashList.put(p.getName(), animal);
        }

        if(e.getSlot() == getConfig.getInt("items.bettors.slot")){

            InventoryClose.bypassClose.add(p.getName());

            getAnimalGameManager().getSeeingMap().put(p.getName(), e.getClickedInventory());

            AnimalGameGamblersGui.create(p, animal);

            InventoryClose.bypassClose.remove(p.getName());
        }

        if(e.getSlot() == getConfig.getInt("items.back.slot")) AnimalGameAnimalsGui.create(p);

        if(e.getSlot() == getConfig.getInt("items.bet.slot")){

            if(getInstance().isReloading()){
                p.sendMessage(getMessages().RELOADING);
                return;
            }

            if(getInstance().isLocked()){
                p.sendMessage(getMessages().GAMES_UNAVAILABLE);
                return;
            }

            if(getAnimalGameManager().isPlayerBet(p.getName(), animal)){
                p.sendMessage(getMessages().ALREADY_BET_ON_ANIMAL);
                return;
            }

            if(user.getAnimalGameMoneyValue() < getInstance().getConfig().getDouble("config.animal-game.min-money-bet")){
                p.sendMessage(getMessages().MINIMUM_MONEY);
                return;
            }

            if(user.getAnimalGameCashValue() < getInstance().getConfig().getDouble("config.animal-game.min-cash-bet")){
                p.sendMessage(getMessages().MINIMUM_CASH);
                return;
            }

            if(user.getAnimalGameMoneyValue() > getInstance().getConfig().getDouble("config.animal-game.max-money-bet")){
                p.sendMessage(getMessages().MAXIMUM_MONEY);
                return;
            }

            if(user.getAnimalGameCashValue() > getInstance().getConfig().getDouble("config.animal-game.max-cash-bet")){
                p.sendMessage(getMessages().MAXIMUM_CASH);
                return;
            }

            if(!checkValues(p, user.getAnimalGameCashValue(), user.getAnimalGameMoneyValue())) return;

            AnimalGameBet animalGameBet = new AnimalGameBet(p.getName(), getNewBetId() + "", animal, user.getAnimalGameCashValue(), user.getAnimalGameMoneyValue());

            getAnimalGameManager().getAnimalGameBets().add(animalGameBet);

            removeValues(p, user.getAnimalGameCashValue(), user.getAnimalGameMoneyValue());

            p.closeInventory();

            getInstance().getConfig().getStringList("messages.bet-placed-animal").forEach(w -> p.sendMessage(c(w.replace("{animal}", animal.getNameStr()))));
        }

    }
}
