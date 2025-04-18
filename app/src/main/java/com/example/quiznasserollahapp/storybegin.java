package com.example.quiznasserollahapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class storybegin extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvLocation;
    private Button btnBeginStory;
    private String detectedCountry = "";
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storybegin);

        tvLocation = findViewById(R.id.tvLocation);
        btnBeginStory = findViewById(R.id.btnBeginStory);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Load Google Map fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Get user's location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Show user's location on map if it's ready
                        if (mMap != null) {
                            LatLng userLocation = new LatLng(latitude, longitude);
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(userLocation).title("Vous êtes ici"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10f));
                        }

                        Geocoder geocoder = new Geocoder(storybegin.this, Locale.getDefault());
                        try {
                            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addresses != null && addresses.size() > 0) {
                                detectedCountry = addresses.get(0).getCountryName();
                                tvLocation.setText("Pays détecté : " + detectedCountry);
                                Log.d("CountryName", "Pays détecté : " + detectedCountry);

                                // Start story on button click
                                btnBeginStory.setOnClickListener(v -> startStoryBasedOnLocation(detectedCountry));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            tvLocation.setText("Impossible de détecter le pays.");
                        }
                    } else {
                        tvLocation.setText("Emplacement introuvable.");
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Show default location (Casablanca) until user location is available
        LatLng defaultLocation = new LatLng(33.5731, -7.5898);
        mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Casablanca"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 8f));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate(); // Re-run to get location
            } else {
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startStoryBasedOnLocation(String countryName) {
        String cleanedCountry = countryName.trim().toLowerCase();
        Toast.makeText(this, "Pays : " + cleanedCountry, Toast.LENGTH_SHORT).show();

        if (cleanedCountry.contains("morocco") || cleanedCountry.contains("maroc")) {
            startActivity(new Intent(this, MarocStoryActivity.class));
        } else if (cleanedCountry.contains("united kingdom") || cleanedCountry.contains("uk")) {
            startActivity(new Intent(this, UkStoryActivity.class));
        } else {
            startActivity(new Intent(this, DefaultStoryActivity.class));
        }
    }
}
