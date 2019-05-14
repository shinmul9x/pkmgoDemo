package com.example.pokemongo1;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Maps_Test extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLoadedCallback {
    // Mã yêu cầu hỏi người dùng cho phép xem vị trí hiện tại của họ (***).
    // Giá trị mã 8bit (value < 256).
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;

    // Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    Location myLoc;

    // Game entities
    private Trainer trainer; // player

    // Map objects
    private GoogleMap mMap;
    private Marker mMarker;
    private ArrayList<Marker> pkmWildMarkers = new ArrayList<>();

    // firebase
    private FirebaseConnection connection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabhost);

        //setup firebase
        connection = new FirebaseConnection();
        connection.setOnGetPokemon(new PokemonChangeListener() {
            @Override
            public void onGetPokemon(ArrayList<Pokemonp> pokemons) {
                displayListPokemon(pokemons);
                Log.v("pkm", "pkm in bag: " + pokemons.size() + "");
            }
        });
        connection.setPokemonWildChangeListener(new PokemonWildChangeListener() {
            @Override
            public void onPokemonWildChange(ArrayList<Pokemon> pokemons) {
                showPokemonsWild(pokemons);
                Log.v("pkm", "pkm wild: " + pokemons.size());
            }
        });

        //setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //setup location listener
        setupLocationChangeListener();

        // get info player
        Intent intent = this.getIntent();
        trainer = new Trainer(intent.getStringExtra("idfb"), "thuy", getPokemons());

        //setup tabhost
        setupTabhost();

    }

    private void setupTabhost() {
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        // setup map tab
        final TabHost.TabSpec mapTab = tabHost.newTabSpec("mapTab");
        mapTab.setContent(R.id.tab1);
        mapTab.setIndicator("Map");
        tabHost.addTab(mapTab);

        // setup bag tab
        final TabHost.TabSpec bagTab = tabHost.newTabSpec("bagTab");
        bagTab.setContent(R.id.tab2);
        bagTab.setIndicator("Bag");
        tabHost.addTab(bagTab);

        // onclick bagtab
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (bagTab.getTag().equals(tabId)) {
                    stopLocationChangeListener();
                    connection.getPokemon(trainer.getId());
                } else if (mapTab.getTag().equals(tabId)) {
                    startLocationChangeListener();
                }
            }
        });
    }

    /**
     * display list trainer's pokemons on bagtab
     */
    private void displayListPokemon(ArrayList<Pokemonp> pokemons) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view);

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.VERTICAL);
        Drawable drawable = ContextCompat.getDrawable(getApplication(),R.drawable.custom_divider);
        dividerItemDecoration.setDrawable(drawable);
        recyclerView.addItemDecoration(dividerItemDecoration);

        PokemonAdapter pokemonAdapter = new PokemonAdapter(pokemons);
        recyclerView.setAdapter(pokemonAdapter);
    }

    private void setupLocationChangeListener() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(3000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //Toast.makeText(Maps_Test.this, "location updated", Toast.LENGTH_SHORT).show();

                if (locationResult == null) {
                    return;
                }

                if (myLoc != null) {
                    if (locationResult.getLastLocation().distanceTo(myLoc) > 3) {
                        showMyLocation(locationResult.getLastLocation());
                    }
                } else {
                    showMyLocation(locationResult.getLastLocation());
                }
                connection.updateLocation(trainer.getId(), locationResult.getLastLocation());
                myLoc = locationResult.getLastLocation();
                connection.getPokemonWild(myLoc);
                locationResult.getLocations().clear();
            }
        };
    }

    /**
     * Get my location by GPS
     * @return my location
     */
    private void startLocationChangeListener() {
        // Check permission use GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions,
                    REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest
                , locationCallback, null);
    }

    private void stopLocationChangeListener() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_ACCESS_COURSE_FINE_LOCATION:
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startLocationChangeListener();
                }
        }
    }

    private void showMyLocation(Location myLocation) {
        if (myLocation != null) {
            if (mMarker != null) mMarker.remove();

            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(19)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            MarkerOptions options = new MarkerOptions();

            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.mez));

            options.position(latLng);
            mMarker = mMap.addMarker(options);

        }
    }

    private void showPokemonsWild(ArrayList<Pokemon> pokemons) {
        if (pkmWildMarkers != null) {
            for ( Marker marker : pkmWildMarkers ) {
                marker.remove();
            }
            pkmWildMarkers.clear();
        }

        for (Pokemon pokemon : pokemons) {
            MarkerOptions options = new MarkerOptions();
            options.icon(BitmapDescriptorFactory.fromResource(pokemon.getImage()));
            options.position(new LatLng(pokemon.getLatitude(), pokemon.getLongitude()));
            options.title(pokemon.getId());
            Marker marker = mMap.addMarker(options);
            marker.hideInfoWindow();
            pkmWildMarkers.add(marker);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //getMyLocation();
        Toast.makeText(this, "onLocationChanged", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Setup map
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLoadedCallback(this);
        mMap.setTrafficEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setMaxZoomPreference((float) 19.5);
        mMap.setMinZoomPreference((float) 18.5);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!marker.equals(mMarker)) {
            if (connection.catchPokemon(marker.getTitle(), trainer.getId())) {
                Log.v("pkm", "Caught a pokemon");
            }

            pkmWildMarkers.remove(marker);
            marker.remove();
            return true;
        }
        Log.v("pkm", "trainerMaker");
        return false;
    }

    @Override
    public void onMapLoaded() {
        startLocationChangeListener();
        connection.getPokemonWild();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationChangeListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationChangeListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationChangeListener();
    }

    private ArrayList<Pokemonp> getPokemons() {
        ArrayList<Pokemonp> pokemons = new ArrayList<>();
        pokemons.add(new Pokemonp(1,74));
        return pokemons;
    }
}
