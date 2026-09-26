package com.worldplugins.worldblaze.commands;

import com.worldplugins.worldblaze.guis.MainGui;
import com.worldplugins.worldblaze.objects.BlazeUser;
import com.worldplugins.worldblaze.schedulers.MainTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

import static com.worldplugins.worldblaze.WorldBlaze.*;

public class MainCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 1 && args[0].equalsIgnoreCase("help")){
            sender.sendMessage("§7/apostar §7- Abre o menu para você escolher qual jogo deseja jogar.");
            sender.sendMessage("§7/graph §7- Abre o graph direto.");
            sender.sendMessage("§7/crash §7- Abre o crash direto.");
            sender.sendMessage("§7/double §7- Abre o double direto.");
            sender.sendMessage("§7/mine §7- Abre o mine direto.");
            sender.sendMessage("§7/jogodobicho §7- Abre o jogo do bicho direto.");
            sender.sendMessage("§7/coinflip §7- Abre o CoinFlip direto.");
            if(sender.hasPermission("worldblaze.admin")) sender.sendMessage("§7/blaze lock §7- Desativa e proibi os futuros jogos ate você ativar novamente.");
            if(sender.hasPermission("worldblaze.admin")) sender.sendMessage("§7/blaze unlock §7- Ativa novamente os jogos.");
            if(sender.hasPermission("worldblaze.admin")) sender.sendMessage("§7/blaze reload §7- Recarrega todos os arquivos de configurações.");
            return true;
        }

        if(args.length == 0){

            if(!(sender instanceof Player)){
                sender.sendMessage(getMessages().ONLY_INGAME);
                return true;
            }
            Player p = (Player) sender;

            BlazeUser blazeUser = getManager().getBlazeUser(p.getName());
            if(blazeUser == null){
                blazeUser = new BlazeUser(p.getName());
                getManager().getUsers().add(blazeUser);
            }

            MainGui.create(p);
            return true;
        }

        if(args.length == 1){

            if(args[0].equalsIgnoreCase("lock")){
                if(!sender.hasPermission("worldblaze.admin")){
                    sender.sendMessage(getMessages().NO_PERMISSION);
                    return true;
                }

                if(getInstance().isLocked()){
                    sender.sendMessage(getMessages().GAMES_ALREADY_LOCKED);
                    return true;
                }

                getInstance().setLocked(true);

                sender.sendMessage(getMessages().GAMES_LOCKED);
                return true;
            }

            if(args[0].equalsIgnoreCase("unlock")){
                if(!sender.hasPermission("worldblaze.admin")){
                    sender.sendMessage(getMessages().NO_PERMISSION);
                    return true;
                }

                if(!getInstance().isLocked()){
                    sender.sendMessage(getMessages().GAMES_ALREADY_UNLOCKED);
                    return true;
                }

                sender.sendMessage(getMessages().GAMES_UNLOCKED);

                Bukkit.getScheduler().runTaskLater(getInstance(), ()->{
                    getInstance().setLocked(false);
                    MainTask.locked = false;
                }, 100);
                return true;
            }

            if(args[0].equalsIgnoreCase("reload")){

                if(!sender.hasPermission("worldblaze.admin")){
                    sender.sendMessage(getMessages().NO_PERMISSION);
                    return true;
                }

                getInstance().getReloadingMessages().add(sender.getName());

                if(getInstance().isReloading()){
                    sender.sendMessage(getMessages().ALREADY_RELOADING);
                    return true;
                }

                getInstance().setReloading(true);

                sender.sendMessage(getMessages().WAITING_RELOAD);
                return true;
            }

        }

        Bukkit.dispatchCommand(sender, "apostar help");
        return false;
    }
}
