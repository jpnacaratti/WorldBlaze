package com.worldplugins.worldblaze.objects;

import com.worldplugins.worldblaze.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.worldplugins.worldblaze.WorldBlaze.*;

public class DoubleAnimationSpot {

    private final String secId;
    private final double time;
    private final Map<ItemStack, List<Integer>> itens;

    public DoubleAnimationSpot(String secId) {
        this.secId = secId;
        this.itens = new HashMap<>();

        FileConfiguration getConfig = getDoubleGuiYml().getConfig();

        this.time = getConfig.getDouble("starting-animation."+secId+".time");

        for (String s : getConfig.getConfigurationSection("starting-animation." + secId + ".items").getKeys(false)) {
            ItemBuilder item = new ItemBuilder(Material.getMaterial(getConfig.getString("starting-animation." + secId + ".items." + s + ".id")), 1, (byte)
            getConfig.getInt("starting-animation." + secId + ".items." + s + ".data"));

            itens.put(item.toItemStack(), getConfig.getIntegerList("starting-animation." + secId + ".items." + s + ".slot"));
        }
    }

    public String getSecId() {
        return secId;
    }

    public double getTime() {
        return time;
    }

    public Map<ItemStack, List<Integer>> getItens() {
        return itens;
    }
}
