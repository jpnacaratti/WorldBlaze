package com.worldplugins.worldblaze.enums;

import java.util.ArrayList;
import java.util.List;

public enum Bicho {

    AVESTRUZ("Avestruz"),
    AGUIA("Águia"),
    BURRO("Burro"),
    BORBOLETA("Borboleta"),
    CACHORRO("Cachorro"),
    CABRA("Cabra"),
    CARNEIRO("Carneiro"),
    CAMELO("Camelo"),
    COBRA("Cobra"),
    COELHO("Coelho"),
    CAVALO("Cavalo"),
    ELEFANTE("Elefante"),
    GALO("Galo"),
    GATO("Gato"),
    JACARE("Jacaré"),
    LEAO("Leão"),
    MACACO("Macaco"),
    PORCO("Porco"),
    PAVAO("Pavão"),
    PERU("Peru"),
    TOURO("Touro"),
    TIGRE("Tigre"),
    URSO("Urso"),
    VEADO("Veado"),
    VACA("Vaca");

    private final String nameStr;

    Bicho(String name) {
        this.nameStr = name;
    }

    public String getNameStr() {
        return nameStr;
    }

    public static boolean isAnimal(String str){
        List<String> has = new ArrayList<>();
        has.add("AVESTRUZ");
        has.add("AGUIA");
        has.add("BURRO");
        has.add("BORBOLETA");
        has.add("CACHORRO");
        has.add("CABRA");
        has.add("CARNEIRO");
        has.add("CAMELO");
        has.add("COBRA");
        has.add("COELHO");
        has.add("CAVALO");
        has.add("ELEFANTE");
        has.add("GALO");
        has.add("GATO");
        has.add("JACARE");
        has.add("LEAO");
        has.add("MACACO");
        has.add("PORCO");
        has.add("PAVAO");
        has.add("PERU");
        has.add("TOURO");
        has.add("TIGRE");
        has.add("URSO");
        has.add("VEADO");
        has.add("VACA");

        return has.contains(str.toUpperCase());
    }

}
