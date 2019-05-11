package com.example.pokemongo1;

import android.location.Location;

public class Pokemon  extends Pokemonp{

    //private   int Image;
    //private String name;
    //private boolean isCatch;
    private Location location;
    //private String power;
    private String id;
    private double latitude;
    private double longitude;


    Pokemon(){
        //constructor này để nhận dữ liệu từ firebase ///////////////

    }


    Pokemon(int image, String name, String power, double lat, double log){
        super(image,name,power);
        //this.id = id;
        latitude = lat;
        longitude = log;
    }

//    Pokemon(int Image, String name, String power, double lat, double log){
//        super(Image,name,power);
//        //this.Image = Image;
//        //this.name = name;
//        //this.power = power;
//        //this.isCatch = isCatch;
//        // Location(String provider)
//        //Construct a new Location with a named provider.
//        location = new Location(name);
//        location.setLatitude(lat);
//        location.setLongitude(log);
//    }
   /*
    public int getImage() {
        return Image;
    }

    public void setImage(int image) {
        Image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
*/
   /*
    public boolean isCatch() {
        return isCatch;
    }

    public void setCatch(boolean aCatch) {
        isCatch = aCatch;
    }
*/
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
/*
    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }
    */
    public String getId() {
        return id;
    }

    public void setId(String key) {
        this.id = id;
    }
}
