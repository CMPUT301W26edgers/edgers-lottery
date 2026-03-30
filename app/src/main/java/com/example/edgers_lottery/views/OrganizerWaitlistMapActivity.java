package com.example.edgers_lottery.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class OrganizerWaitlistMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String eventId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_map_organizer);

        // 1. Get Event ID from Intent
        eventId = getIntent().getStringExtra("event_id");
        db = FirebaseFirestore.getInstance();

        // 2. Initialize Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 3. Setup Navigation (Matching teammate's style)
        setupNavigation();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Optional: Style the map to be clean
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (eventId != null) {
            loadEntrantLocations();
        }
    }

    private void loadEntrantLocations() {
        db.collection("events").document(eventId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null || documentSnapshot == null || !documentSnapshot.exists()) return;

                    // Clear existing markers before redrawing
                    mMap.clear();

                    // Get the waitingList array from Firestore
                    List<Map<String, Object>> waitingList = (List<Map<String, Object>>) documentSnapshot.get("waitingList");

                    if (waitingList == null || waitingList.isEmpty()) {
                        Toast.makeText(this, "No entrants on the waitlist yet.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    boolean hasMarkers = false;

                    for (Map<String, Object> userMap : waitingList) {
                        Double lat = (Double) userMap.get("latitude");
                        Double lon = (Double) userMap.get("longitude");
                        String name = (String) userMap.get("name");

                        if (lat != null && lon != null) {
                            LatLng position = new LatLng(lat, lon);
                            mMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title(name))
                                    .showInfoWindow();;

                            builder.include(position);
                            hasMarkers = true;
                        }
                    }

                    // Auto-zoom the camera to fit all participants
                    if (hasMarkers) {
                        LatLngBounds bounds = builder.build();
                        int padding = 200; // Offset from edges of the map in pixels
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                    }
                });
    }

    private void setupNavigation() {
        // Back Button
        findViewById(R.id.btnBackEventDetails).setOnClickListener(v -> finish());

        // Tab Navigation
        findViewById(R.id.detailBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, EventDetailsOrganizer.class).putExtra("event_id", eventId));
            finish();
        });

        findViewById(R.id.waitListBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, EventWaitlistTab.class).putExtra("event_id", eventId));
            finish();
        });

        findViewById(R.id.entrantBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, EventEntrantOrganizer.class).putExtra("event_id", eventId));
            finish();
        });

        findViewById(R.id.editEventBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, CreateEditEventActivity.class).putExtra("event_id", eventId));
            finish();
        });
    }
}