package com.example.pokemongo1;

import java.util.ArrayList;

public class Trainer {
    private String id;
    private String name;
    private ArrayList<Pokemonp> pokemons;

    private static Trainer instance; // Singleton Pattern

    public Trainer(String id, String name) {
        this.id = id;
        this.name = name;
        pokemons = new ArrayList<>();
    }

    public Trainer(String id, String name, ArrayList<Pokemonp> pokemons) {
        this.id = id;
        this.name = name;
        this.pokemons = pokemons;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Pokemonp> getPokemons() {
        return pokemons;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPokemon(Pokemonp pkm) {
        this.pokemons.add(pkm);
    }

    public void removePokemon(int pkmIndex) {
        this.pokemons.remove(pkmIndex);
    }
}
