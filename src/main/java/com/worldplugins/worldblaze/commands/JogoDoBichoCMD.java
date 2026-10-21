package com.worldplugins.worldblaze.commands;

import com.worldplugins.worldblaze.enums.Bicho;
import com.worldplugins.worldblaze.guis.JogoDoBichoMainGui;
import com.worldplugins.worldblaze.objects.JogoDoBichoBet;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.worldplugins.worldblaze.WorldBlaze.*;
import static com.worldplugins.worldblaze.utils.Utils.*;

public class JogoDoBichoCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 0){

            if(!(sender instanceof Player)){
                sender.sendMessage(getMessages().ONLY_INGAME);
                return true;
            }
            Player p = (Player) sender;

            if(!p.hasPermission("worldblaze.jogodobicho")){
                p.sendMessage(getMessages().NO_PERMISSION);
                return true;
            }

            JogoDoBichoMainGui.create(p);
            return true;
        }

        if(args.length == 1){

            if(args[0].equalsIgnoreCase("help")){

                sender.sendMessage("§cUse /jogodobicho");
                if(sender.hasPermission("worldblaze.admin")) sender.sendMessage("§e/jogodobicho sortear");
                if(sender.hasPermission("worldblaze.admin")) sender.sendMessage("§e/jogodobicho sortear <bicho>");

                return true;
            }

            if(args[0].equalsIgnoreCase("sortear")){

                if(!sender.hasPermission("worldblaze.admin")){
                    sender.sendMessage(getMessages().NO_PERMISSION);
                    return true;
                }

                Bicho randomBicho = getJogoDoBichoManager().getRandomBicho();

                getInstance().getConfig().getStringList("Messages.Animal-drawn-broadcast").forEach(w -> Bukkit.broadcastMessage(c(w.
                        replace("{bicho}", randomBicho.getNameStr()))));

                for (JogoDoBichoBet bet : getJogoDoBichoManager().getJogoDoBichoBets()) {
                    Player player = Bukkit.getPlayer(bet.getOwner());

                    if(bet.getAnimal() == randomBicho){

                        double moneyWon = bet.getMoneyValue() * 2;
                        double cashWon = bet.getCashValue() * 2;

                        depositValues(Bukkit.getOfflinePlayer(bet.getOwner()), cashWon, moneyWon);

                        if ((moneyWon > 0 && moneyWon >= getInstance().getConfig().getDouble("Config.Jogo-do-bicho.Alerta-money")) ||
                                (cashWon > 0 && cashWon >= getInstance().getConfig().getDouble("Config.Jogo-do-bicho.Alerta-cash"))){
                            getInstance().getConfig().getStringList("Messages.Broadcast-animal-profit").forEach(w -> Bukkit.broadcastMessage(c(w.
                                    replace("{player}", bet.getOwner()).
                                    replace("{money}", letterFormatter(moneyWon)).
                                    replace("{cash}", letterFormatter(cashWon)))));
                        }

                        if(player != null) getInstance().getConfig().getStringList("Messages.Won-jogo-do-bicho").forEach(w -> Bukkit.broadcastMessage(c(w.
                                replace("{money}", letterFormatter(moneyWon)).
                                replace("{cash}", letterFormatter(cashWon)))));

                    }else if(player != null) getInstance().getConfig().getStringList("Messages.Lost-jogo-do-bicho").forEach(w -> player.sendMessage(c(w)));
                }

                getJogoDoBichoManager().getJogoDoBichoBets().clear();

                return true;
            }

        }

        if(args.length == 2){

            if(args[0].equalsIgnoreCase("sortear")){

                if(!sender.hasPermission("worldblaze.admin")){
                    sender.sendMessage(getMessages().NO_PERMISSION);
                    return true;
                }

                Bicho chose;
                try {
                    chose = Bicho.valueOf(args[1]);
                }catch (IllegalArgumentException e){
                    sender.sendMessage(getMessages().INVALID_ANIMAL);
                    return true;
                }

                getInstance().getConfig().getStringList("Messages.Animal-drawn-broadcast").forEach(w -> Bukkit.broadcastMessage(c(w.
                        replace("{bicho}", chose.getNameStr()))));

                for (JogoDoBichoBet bet : getJogoDoBichoManager().getJogoDoBichoBets()) {
                    Player player = Bukkit.getPlayer(bet.getOwner());

                    if(bet.getAnimal() == chose){

                        double moneyWon = bet.getMoneyValue() * 2;
                        double cashWon = bet.getCashValue() * 2;

                        depositValues(Bukkit.getOfflinePlayer(bet.getOwner()), cashWon, moneyWon);

                        if ((moneyWon > 0 && moneyWon >= getInstance().getConfig().getDouble("Config.Jogo-do-bicho.Alerta-money")) ||
                                (cashWon > 0 && cashWon >= getInstance().getConfig().getDouble("Config.Jogo-do-bicho.Alerta-cash"))){
                            getInstance().getConfig().getStringList("Messages.Broadcast-animal-profit").forEach(w -> Bukkit.broadcastMessage(c(w.
                                    replace("{player}", bet.getOwner()).
                                    replace("{money}", letterFormatter(moneyWon)).
                                    replace("{cash}", letterFormatter(cashWon)))));
                        }

                        if(player != null) getInstance().getConfig().getStringList("Messages.Won-jogo-do-bicho").forEach(w -> Bukkit.broadcastMessage(c(w.
                                replace("{money}", letterFormatter(moneyWon)).
                                replace("{cash}", letterFormatter(cashWon)))));

                    }else if(player != null) getInstance().getConfig().getStringList("Messages.Lost-jogo-do-bicho").forEach(w -> player.sendMessage(c(w)));
                }

                getJogoDoBichoManager().getJogoDoBichoBets().clear();

                sender.sendMessage(getMessages().ANIMAL_DRAWN);
                return true;
            }

        }

        Bukkit.dispatchCommand(sender, "jogodobicho help");
        return false;
    }
}
