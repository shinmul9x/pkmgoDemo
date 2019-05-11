package com.example.pokemongo1;

public class Pokemonp {
    private int image;
    private String name;
    private String power;

    public  Pokemonp(){}

    public Pokemonp(int image, String name, String power) {
        image = image;
        this.name = name;
        this.power = power;
    }


    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }
}
