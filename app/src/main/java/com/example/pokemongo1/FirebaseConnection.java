package com.example.pokemongo1;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirebaseConnection {
    public DatabaseReference dbRef;
    public PokemonWildChangeListener pokemonWildListener;
    public PokemonChangeListener pokemonListener;

    public FirebaseConnection() {
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public void getPokemon(String trainerId) {
        dbRef.child("trainer").child(trainerId).child("pokemons").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<Pokemonp> pokemonps = new ArrayList<>();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Pokemonp pokemon = data.getValue(Pokemonp.class);
                            pokemonps.add(pokemon);
                        }
                        pokemonListener.onPokemonChanged(pokemonps);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    public void getPokemonWild() {
        dbRef.child("pokemonWild").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Pokemon pokemons = dataSnapshot.getValue(Pokemon.class);
                pokemons.setId(dataSnapshot.getKey());
                pokemonWildListener.onPokemonWildAdded(pokemons);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void catchPokemon(String pokemonId, final String trainerId) {
        dbRef.child("pokemonWild").child(pokemonId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Pokemon pokemonWild = dataSnapshot.getValue(Pokemon.class);
                        Pokemonp pokemon = (Pokemonp) pokemonWild;
                        dbRef.child("trainer").child(trainerId).child("pokemons")
                                .push().setValue(pokemon);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
        dbRef.child("pokemonWild").child(pokemonId).removeValue();
    }

    public boolean checkTrainer(String fbId) {
        return false;
    }

    public boolean registerTrainer(String fbId, String trainerName) {
        dbRef.child("trainer").child(fbId).setValue(new Trainer(fbId, trainerName));
        return true;
    }

    public Trainer getTrainer(final String fbId) {
        final Trainer[] trainers = new Trainer[1];
        dbRef.child("trainer").child(fbId).child("name").setValue("thuy");
        dbRef.child("trainer").child(fbId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();

                ArrayList<Pokemonp> pokemons = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.child("pokemons").getChildren()) {
                    Pokemonp pokemon = data.getValue(Pokemonp.class);
                    pokemons.add(pokemon);
                }
                trainers[0] = new Trainer(fbId, name, pokemons);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return trainers[0];
    }
}
