package com.example.pokemongo1;

public class Pokemon  extends Pokemonp{

    private String id;
    private double latitude;
    private double longitude;

    Pokemon(int index, String power, double lat, double log){
        super(index,power);
        //this.id = id;
        latitude = lat;
        longitude = log;
    }

    public String getId() {
        return id;
    }

    public void setId(String key) {
        this.id = key;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
