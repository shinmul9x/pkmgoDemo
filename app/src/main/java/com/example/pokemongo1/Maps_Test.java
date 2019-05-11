package com.example.pokemongo1;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;

import java.util.ArrayList;

public class Maps_Test extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLoadedCallback {
    // Mã yêu cầu hỏi người dùng cho phép xem vị trí hiện tại của họ (***).
    // Giá trị mã 8bit (value < 256).
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;

    // Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    // Game entities
    private Trainer trainer; // player
    private ArrayList<Pokemon> pokemonsWilds;

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

        //setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //setup location listener
        setupLocationChangeListener();

        // get info player
        Intent intent = this.getIntent();
        trainer = connection.getTrainer(intent.getStringExtra("idfb"));
        if (trainer == null) {
            Toast.makeText(this,"null trainer", Toast.LENGTH_SHORT).show();
            trainer = new Trainer(intent.getStringExtra("idfb"), "thuy");
        }

        // get pokemons wild
        pokemonsWilds = getPokemonWilds();

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
                    displayListPokemon();
                } else if (mapTab.getTag().equals(tabId)) {
                    startLocationChangeListener();
                }
            }
        });
    }

    /**
     * display list trainer's pokemons on bagtab
     */
    private void displayListPokemon() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this
                , LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        Drawable drawable = ContextCompat.getDrawable(getApplication(), R.drawable.custom_divider);
        dividerItemDecoration.setDrawable(drawable);

        recyclerView.addItemDecoration(dividerItemDecoration);
        PokemonAdapter pokemonAdapter = new PokemonAdapter(trainer.getPokemons());
        recyclerView.setAdapter(pokemonAdapter);
    }

    private void setupLocationChangeListener() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(800);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //Toast.makeText(Maps_Test.this, "location updated", Toast.LENGTH_SHORT).show();

                if (locationResult == null) {
                    return;
                }

                showMyLocation(locationResult.getLastLocation());
                showPokemonsWild(locationResult.getLastLocation());
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

            // TODO: insert trainer's location into Parse Server

        }
    }

    private void showPokemonsWild(Location location) {
        ArrayList<Pokemon> pokemonsWild = pokemonsWilds;
        // TODO: get list pokemon's location from Parse Server

        for (Pokemon pokemon : pokemonsWild) {                       // update DB then delete this
            boolean isExist = false;                                 // part
            for (Marker pkmMarker : pkmWildMarkers) {                //
                if (pkmMarker.getTitle().equals(pokemon.getId())) {  //
                    isExist = true;                                  //
                    break;                                           //
                }                                                    //
            }                                                        //
            if (isExist) {                                           //
                continue;                                            //
            }                                                        //

            LatLng latLng = new LatLng(pokemon.getLocation().getLatitude(), pokemon.getLocation().getLongitude());
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            MarkerOptions options = new MarkerOptions();
            options.icon(BitmapDescriptorFactory.fromResource(pokemon.getImage()));
            options.position(latLng);
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
        for (Marker pkmMarker : pkmWildMarkers) {
            if (pkmMarker.equals(marker)) {
                if (hasCaughtPokemon(marker.getTitle())) {
                    pkmWildMarkers.remove(pkmMarker);
                    Toast.makeText(this, "Catched a pokemon", Toast.LENGTH_LONG).show();
                    return true;
                }
            }
            marker.remove();
        }
        return false;
    }

    private boolean hasCaughtPokemon(String pokemonId) {
        // TODO: edit DB

        for (Pokemon pokemon : pokemonsWilds) {
            if (pokemon.getId().equals(pokemonId)) {
                trainer.addPokemon((Pokemonp) pokemon);
                //pokemonsWilds.remove(pokemon);
                return true;
            }
        }
        Log.v("pkmgo", "catched " + pokemonId);
        return true;
    }

    @Override
    public void onMapLoaded() {
        startLocationChangeListener();
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

    private ArrayList<Pokemon> getPokemonWilds() {
        ArrayList<Pokemon> pokemonsWild = new ArrayList<>();
//        pokemonsWild.add(new Pokemon("1",R.drawable.bulbasaurz, "Bulbasaur", "45", 21.040450, 105.783106));
//        pokemonsWild.add(new Pokemon("2",R.drawable.charmanderz, "Charmander","32",  21.039949, 105.780231));
//        pokemonsWild.add(new Pokemon("3",R.drawable.metapodz, "Metapod","25",  21.037045, 105.784351));
//        pokemonsWild.add(new Pokemon("4",R.drawable.pidgeotz, "Pidgeot", "45", 21.037556, 105.784748));
//        pokemonsWild.add(new Pokemon("5",R.drawable.poliwrathz, "Poliwrathz","60",  21.035483, 105.783729));
//        pokemonsWild.add(new Pokemon("6",R.drawable.arbok ,"Arbok","48",21.008645,105.814592));
//        pokemonsWild.add(new Pokemon("7",R.drawable.bellsprout,"bellsprout","59",21.022596,105.803273));
//        pokemonsWild.add(new Pokemon("8",R.drawable.diglett,"diglett","78",21.026402,105.796160));
//        pokemonsWild.add(new Pokemon("9",R.drawable.dodrio,"dodrio","56",21.036266,105.789358));
//        pokemonsWild.add(new Pokemon("10",R.drawable.dragonite,"dragonite","89",21.035785,105.786204));
//        pokemonsWild.add(new Pokemon("11",R.drawable.exeggutor,"exeggutor","19",21.032030,105.784305));
//        pokemonsWild.add(new Pokemon("12",R.drawable.gengar,"gengar","57",21.028475,105.779895));
//        pokemonsWild.add(new Pokemon("13",R.drawable.growlithe,"growlithe","77",21.027423,105.778318));
//        pokemonsWild.add(new Pokemon("14",R.drawable.haunter,"haunter","74",21.017218,105.790813));
        return pokemonsWild;
    }
}
