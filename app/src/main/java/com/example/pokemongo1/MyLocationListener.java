package com.example.pokemongo1;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

public class MyLocationListener implements LocationListener {
    Context context;
    public static Location location;

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
//        Toast.makeText(MyLocationListener.this , "GPS đã được bật, bạn có thể chơi game",
//                 Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
//        Toast.makeText(this, "GPS đã được tắt, bạn không thể tiếp tục chơi game",
//                Toast.LENGTH_LONG).show();
    }
}
