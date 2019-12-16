package com.rohan.trackmyrideversion0;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rohan.trackmyrideversion0.pojos.Rider;
import com.rohan.trackmyrideversion0.widget.WidgetIntentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RiderActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = RiderActivity.class.getSimpleName();
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private GoogleMap mMap;

    @BindView(R.id.tv_riders)
    TextView tvRiders;
    @BindView(R.id.map_fragment_view)
    View mapFragmentView;

    private String rideCode;
    private String name;
    private Place origin;
    private Place destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);

        ButterKnife.bind(this);

        rideCode = getIntent().getStringExtra(getString(R.string.ride_code));
        name = getIntent().getStringExtra(getString(R.string.name));
        origin = getIntent().getParcelableExtra(getString(R.string.origin));
        destination = getIntent().getParcelableExtra(getString(R.string.destination));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        addRiderToDatabase();
    }

    private void observeRiderChanges() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                "rides/" + rideCode + "/riders/" + FirebaseAuth.getInstance().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(RiderActivity.this, getString(R.string.ride_ended), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Rider rider = dataSnapshot.getValue(Rider.class);
                if (rider==null) {
                    Log.d(TAG, "onDataChange: rider is null");
                    return;
                }
                updateRiderOrder(rider.orderNumber);
                updateRiderStatus(rider.status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Error reading the riders from the database");
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(16);

        if (origin.getLatLng() != null) {
            mMarkers.put("Origin", mMap.addMarker(new MarkerOptions().title("Origin").position(origin.getLatLng())));
        } else {
            Log.d(TAG, "onMapReady: origin's latlng is null");
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
        subscribeToMapUpdates();
    }

    private void subscribeToMapUpdates() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rides/" + rideCode + "/driver_location");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                setMarker(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                setMarker(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Error to read location", databaseError.toException());
            }
        });
    }

    private void setMarker(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();
        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
        double lat = Double.parseDouble(value.get("latitude").toString());
        double lng = Double.parseDouble(value.get("longitude").toString());
        LatLng location = new LatLng(lat, lng);
        if (!mMarkers.containsKey(key)) {
            mMarkers.put(key, mMap.addMarker(new MarkerOptions().title(getString(R.string.driver)).position(location)));
        } else {
            mMarkers.get(key).setPosition(location);
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
    }

    private void updateRiderOrder(int order) {
        TextView tvRiders = findViewById(R.id.tv_riders);
        tvRiders.setText(getString(R.string.riders_before_you, (order-1)));
    }

    private void updateRiderStatus(int status) {
        if (status == Rider.STATUS_IN_RIDE) {
            tvRiders.setText(getString(R.string.in_ride));
            mapFragmentView.setVisibility(View.GONE);
        } else {
            mapFragmentView.setVisibility(View.VISIBLE);
        }
    }

    private void addRiderToDatabase() {
        final String path = "rides/" + rideCode + "/riders";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path).push();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                        "rides/" + rideCode + "/riders");

                Map<String, Object> hashMap = new HashMap<>();
                int orderNumber = dataSnapshot.exists() ? (int) dataSnapshot.getChildrenCount() + 1 : 1;
                Rider rider = new Rider(FirebaseAuth.getInstance().getUid(), name, Rider.STATUS_TO_BE_PICKED,
                        orderNumber, origin.getLatLng().latitude, origin.getLatLng().longitude, destination.getLatLng().latitude, destination.getLatLng().longitude);
                hashMap.put(FirebaseAuth.getInstance().getUid(), rider);
                ref.updateChildren(hashMap);
                observeRiderChanges();

                createSharedPreferencesWidgetString(orderNumber);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Error reading the riders from the database");
            }
        });
    }

    private void createSharedPreferencesWidgetString(int order) {
        String widgetString = getString(R.string.riders_before_you, order-1);

        SharedPreferences sharedpreferences = getSharedPreferences(getString(R.string.shared_preferences_rides_string), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(getString(R.string.shared_preferences_widget_string_key), widgetString);
        editor.apply();

        startWidgetIntentService();
    }

    @Override
    protected void onDestroy() {
        removeSharedPreferencesWidgetString();
        Toast.makeText(this, getString(R.string.left_ride_success), Toast.LENGTH_SHORT).show();
        deleteRider(FirebaseAuth.getInstance().getUid());
        super.onDestroy();
    }

    private void deleteRider(String id) {
        final String path = "rides/" + rideCode + "/riders/" + id;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        ref.removeValue();
    }

    private void removeSharedPreferencesWidgetString() {
        SharedPreferences sharedpreferences = getSharedPreferences(getString(R.string.shared_preferences_rides_string), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(getString(R.string.shared_preferences_widget_string_key));
        editor.apply();

        startWidgetIntentService();
    }

    private void startWidgetIntentService() {
        Intent intent = new Intent(this, WidgetIntentService.class);
        intent.setAction(WidgetIntentService.ACTION_SHOW_RIDE_CODE);
        startService(intent);
    }
}
