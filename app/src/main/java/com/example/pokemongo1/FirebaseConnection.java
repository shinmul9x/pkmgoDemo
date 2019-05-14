package com.example.pokemongo1;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class FirebaseConnection {
    private DatabaseReference dbRef;
    private PokemonWildChangeListener pokemonWildListener;
    private PokemonChangeListener pokemonListener;
    private Location trainerLocation;

    public FirebaseConnection() {
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public void getPokemon(String trainerId) {
        dbRef.child("trainer").child(trainerId).child("pokemons").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Pokemonp> pokemons = new ArrayList<>();
                Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                while (dataSnapshotIterator.hasNext()) {
                    DataSnapshot data = dataSnapshotIterator.next();
                    String key = data.getKey();
                    int index = Integer.parseInt(dataSnapshot.child(key).child("index").getValue().toString());
                    int power = Integer.parseInt(dataSnapshot.child(key).child("power").getValue().toString());
                    pokemons.add(new Pokemonp(index, power));
                }
                if (pokemonListener != null && pokemons != null) {
                    pokemonListener.onGetPokemon(pokemons);
                } else {
                    Log.v("pkm", "listener Null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getPokemonWild(Location location) {
        trainerLocation = location;

        dbRef.child("pokemon wild").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Pokemon> pokemons = new ArrayList<>();
                Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                while (dataSnapshotIterator.hasNext()) {
                    String key = dataSnapshotIterator.next().getKey();
                    double latitude = Double.parseDouble(dataSnapshot.child(key).child("latitude").getValue().toString());
                    double longitude = Double.parseDouble(dataSnapshot.child(key).child("longitude").getValue().toString());
                    int index = Integer.parseInt(dataSnapshot.child(key).child("index").getValue().toString());
                    int power = Integer.parseInt(dataSnapshot.child(key).child("power").getValue().toString());
                    Pokemon pokemon = new Pokemon(index, power, latitude, longitude);
                    pokemon.setId(key);

                    Location pkmLocation = new Location("pkmgo");
                    pkmLocation.setLatitude(latitude);
                    pkmLocation.setLongitude(longitude);
                    if (trainerLocation.distanceTo(pkmLocation) <= 30) {
                        pokemons.add(pokemon);
                    }

                }

                if (pokemonWildListener != null && pokemons != null) {
                    pokemonWildListener.onPokemonWildChange(pokemons);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getPokemonWild() {
        if (trainerLocation == null) {
            return;
        }

        dbRef.child("pokemon wild").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    ArrayList<Pokemon> pokemons = new ArrayList<>();
                    Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                    while (dataSnapshotIterator.hasNext()) {
                        String key = dataSnapshotIterator.next().getKey();
                        double latitude = Double.parseDouble(dataSnapshot.child(key).child("latitude").getValue().toString());
                        double longitude = Double.parseDouble(dataSnapshot.child(key).child("longitude").getValue().toString());
                        int index = Integer.parseInt(dataSnapshot.child(key).child("index").getValue().toString());
                        int power = Integer.parseInt(dataSnapshot.child(key).child("power").getValue().toString());
                        Pokemon pokemon = new Pokemon(index, power, latitude, longitude);
                        pokemon.setId(key);

                        Location pkmLocation = new Location("pkmgo");
                        pkmLocation.setLatitude(latitude);
                        pkmLocation.setLongitude(longitude);
                        if (trainerLocation.distanceTo(pkmLocation) <= 20) {
                            pokemons.add(pokemon);
                        }

                    }

                    if (pokemonWildListener != null && pokemons != null) {
                        pokemonWildListener.onPokemonWildChange(pokemons);
                    }
                } catch (NullPointerException e) {
                    Log.v("pkm", "getPokemonWild() nullpoiter");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean catchPokemon(String pokemonId, final String trainerId) {
        try {
            dbRef.child("pokemon wild").child(pokemonId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int index = Integer.parseInt(dataSnapshot.child("index").getValue().toString());
                    int power = Integer.parseInt(dataSnapshot.child("power").getValue().toString());
                    Pokemonp pokemon = new Pokemonp(index, power);
                    dbRef.child("trainer").child(trainerId).child("pokemons").push().setValue(pokemon);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            dbRef.child("pokemon wild").child(pokemonId).removeValue();
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    public boolean checkTrainer(String fbId) {
        return false;
    }

    public boolean registerTrainer(String fbId, String trainerName) {

        return true;
    }

    public void updateLocation(String trainerID, Location location) {
        dbRef.child("trainer").child(trainerID).child("location").setValue(location);
    }

    public void setOnGetPokemon(PokemonChangeListener pokemonListener) {
        this.pokemonListener = pokemonListener;
    }

    public void setPokemonWildChangeListener(PokemonWildChangeListener pokemonWildListener) {
        this.pokemonWildListener = pokemonWildListener;
    }
}
