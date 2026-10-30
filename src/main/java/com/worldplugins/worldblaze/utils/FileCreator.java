package com.worldplugins.worldblaze.utils;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

import static com.worldplugins.worldblaze.WorldBlaze.*;

public class FileCreator {

    private FileConfiguration fileConfiguration;
    private File file;
    private final String name, directory;

    public FileCreator(String name, String directory) {
        this.name = name;
        this.directory = directory;
        file = new File(directory+name);
        if(!file.exists()) {
            try {
                FileUtils.copyInputStreamToFile(getInstance().getResource(name), file);
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage("[WorldBlaze] File " + name + " created successfully!");
            }
        }

        fileConfiguration = YamlConfiguration.loadConfiguration(file);

    }

    public FileConfiguration getConfig(){
        return fileConfiguration;
    }

    public void saveFile(){

        try {
            fileConfiguration.save(file);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("[WorldBlaze] An error occurred while saving the file "+name+", contact a developer!");
        }

    }

    public void reloadFile(){
        try{

            file = new File(directory+name);

            if(!file.exists()) {
                try {
                    FileUtils.copyInputStreamToFile(getInstance().getResource(name), file);
                } catch (IOException e) {
                    Bukkit.getConsoleSender().sendMessage("[WorldBlaze] File " + name + " created successfully!");
                }
            }

            fileConfiguration = YamlConfiguration.loadConfiguration(file);

        }catch (Exception e){
            Bukkit.getConsoleSender().sendMessage( "[WorldBlaze] An error occurred while reloading the file "+name);
        }
    }

}
