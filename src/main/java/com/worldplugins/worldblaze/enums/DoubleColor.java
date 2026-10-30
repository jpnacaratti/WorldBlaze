package com.worldplugins.worldblaze.enums;

import java.util.List;

public enum DoubleColor {

    RED("Red"),
    WHITE("White"),
    BLACK("Black");

    private final String nameStr;

    DoubleColor(String name) {
        this.nameStr = name;
    }

    public String getNameStr() {
        return nameStr;
    }
}
