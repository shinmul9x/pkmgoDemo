package com.example.pokemongo1;

import java.io.File;

public class Pokemonp {
    public static final int[] image = {R.drawable.arbok, R.drawable.bellsprout, R.drawable.bulbasaurz, R.drawable.charmanderz, R.drawable.diglett, R.drawable.dodrio, R.drawable.dragonite, R.drawable.exeggutor, R.drawable.gengar, R.drawable.growlithe, R.drawable.haunter, R.drawable.hitmonlee, R.drawable.jolteon, R.drawable.koffing, R.drawable.krabby, R.drawable.magnemite, R.drawable.mankey, R.drawable.metapodz, R.drawable.nidoran, R.drawable.pidgeotz, R.drawable.pikachu};
    public static final String[] name = {"Arbok", "Bellsprout", "Bulbasaurz", "Charmanderz", "Diglett", "Dodrio", "Dragonite", "Exeggutor", "Gengar", "Growlithe", "Haunter", "Hitmonlee", "Jolteon", "Koffing", "Krabby", "Magnemite", "Mankey", "Metapodz", "Nidoran", "Pidgeotz", "Pikachu"};

    //private String image;
    //private String name;
    private int index;
    private String power;

    public  Pokemonp(){}

    public Pokemonp(int index, String power) {
//        this.image = new File(image).getAbsolutePath();
//        this.name = name;
        this.index = index;
        this.power = power;
    }


    public int getImage() {
        return image[index];
    }

    public String getName() {
        return name[index];
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }
}
