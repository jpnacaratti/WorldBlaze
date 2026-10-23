package com.worldplugins.worldblaze.enums;

import java.util.ArrayList;
import java.util.List;

public enum Bicho {

    OSTRICH("Ostrich"),
    EAGLE("Eagle"),
    DONKEY("Donkey"),
    BUTTERFLY("Butterfly"),
    DOG("Dog"),
    GOAT("Goat"),
    RAM("Ram"),
    CAMEL("Camel"),
    SNAKE("Snake"),
    RABBIT("Rabbit"),
    HORSE("Horse"),
    ELEPHANT("Elephant"),
    ROOSTER("Rooster"),
    CAT("Cat"),
    ALLIGATOR("Alligator"),
    LION("Lion"),
    MONKEY("Monkey"),
    PIG("Pig"),
    PEACOCK("Peacock"),
    TURKEY("Turkey"),
    BULL("Bull"),
    TIGER("Tiger"),
    BEAR("Bear"),
    DEER("Deer"),
    COW("Cow");

    private final String nameStr;

    Bicho(String name) {
        this.nameStr = name;
    }

    public String getNameStr() {
        return nameStr;
    }

    public static boolean isAnimal(String str){
        List<String> has = new ArrayList<>();
        has.add("OSTRICH");
        has.add("EAGLE");
        has.add("DONKEY");
        has.add("BUTTERFLY");
        has.add("DOG");
        has.add("GOAT");
        has.add("RAM");
        has.add("CAMEL");
        has.add("SNAKE");
        has.add("RABBIT");
        has.add("HORSE");
        has.add("ELEPHANT");
        has.add("ROOSTER");
        has.add("CAT");
        has.add("ALLIGATOR");
        has.add("LION");
        has.add("MONKEY");
        has.add("PIG");
        has.add("PEACOCK");
        has.add("TURKEY");
        has.add("BULL");
        has.add("TIGER");
        has.add("BEAR");
        has.add("DEER");
        has.add("COW");

        return has.contains(str.toUpperCase());
    }

}
