package com.worldplugins.worldblaze.commands;

import com.worldplugins.worldblaze.guis.DoubleGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.worldplugins.worldblaze.WorldBlaze.getInstance;
import static com.worldplugins.worldblaze.WorldBlaze.getMessages;

public class DoubleCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)){
            sender.sendMessage(getMessages().ONLY_INGAME);
            return true;
        }
        Player p = (Player) sender;

        if(!p.hasPermission("worldblaze.double")){
            p.sendMessage(getMessages().SEM_PERMISSAO);
            return true;
        }

        if(getInstance().isReloading()){
            p.sendMessage(getMessages().RELOADING);
            return true;
        }

        if(args.length != 0){
            p.sendMessage("§cUse /double");
            return true;
        }

        DoubleGui.create(p);
        return false;
    }
}
