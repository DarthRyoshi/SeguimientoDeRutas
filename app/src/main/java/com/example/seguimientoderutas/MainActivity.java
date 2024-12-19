package com.example.seguimientoderutas;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private Button btnToggleMapType;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private boolean isUserInteracting = false; // Bandera para detectar interacción del usuario
    private HashMap<LatLng, Marker> markers = new HashMap<>();

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Verificar si el usuario está autenticado
        if (currentUser == null) {
            // Si no está autenticado, redirigir al LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        btnToggleMapType = findViewById(R.id.btnToggleMapType);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializar el LocationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null && !isUserInteracting) {
                    LatLng currentLocation = new LatLng(
                            locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude()
                    );
                    // Mover la cámara solo si el usuario no está interactuando
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                }
            }
        };

        btnToggleMapType.setOnClickListener(v -> toggleMapType());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        googleMap.setOnMapClickListener(latLng -> {
            if (markers.containsKey(latLng)) {
                Marker marker = markers.get(latLng);
                marker.remove();
                markers.remove(latLng);
            } else {
                Marker newMarker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Punto de interés"));
                markers.put(latLng, newMarker);
            }
        });

        // Listener para detectar cuando el usuario empieza a interactuar con el mapa
        googleMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                isUserInteracting = true;
            }
        });

        // Listener para detectar cuando el usuario termina de interactuar con el mapa
        googleMap.setOnCameraIdleListener(() -> isUserInteracting = false);
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000) // Mínimo tiempo entre actualizaciones
                .setMaxUpdateDelayMillis(10000) // Máximo tiempo entre actualizaciones
                .build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void toggleMapType() {
        if (googleMap != null) {
            int currentType = googleMap.getMapType();
            if (currentType == GoogleMap.MAP_TYPE_NORMAL) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            } else {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
