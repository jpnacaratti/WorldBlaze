package com.worldplugins.worldblaze.objects;

import org.bukkit.inventory.ItemStack;

public class GraphPoint{

    private final int id;
    private final double pointValue;
    private final ItemStack graphItem;
    private final boolean positive;

    public GraphPoint(int id, double pointValue, ItemStack graphItem, boolean positive) {
        this.id = id;
        this.pointValue = pointValue;
        this.graphItem = graphItem;
        this.positive = positive;
    }

    public int getId() {
        return id;
    }

    public double getPointValue() {
        return pointValue;
    }

    public ItemStack getGraphItem() {
        return graphItem;
    }

    public boolean isPositive() {
        return positive;
    }
}
