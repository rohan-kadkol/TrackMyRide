package com.rohan.trackmyrideversion0;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rohan.trackmyrideversion0.adapters.RidersAdapter;
import com.rohan.trackmyrideversion0.pojos.Rider;
import com.rohan.trackmyrideversion0.utils.RiderUtils;
import com.rohan.trackmyrideversion0.widget.WidgetIntentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class DriverActivity extends AppCompatActivity implements RidersAdapter.RiderClickInterface {
    private static final String TAG = DriverActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST = 1;

    private String rideCode;

    RidersAdapter mAdapter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        rideCode = getIntent().getStringExtra(getString(R.string.ride_code));
        ((TextView) findViewById(R.id.tv_ride_id)).setText(getString(R.string.ride_code_with_string, rideCode));

        createSharedPreferencesWidgetString();

        setupTemporaryPath();

        requestLocation();
        setupRiderList();
    }

    private void setupTemporaryPath() {
        final String path = "rides/" + rideCode + "/temporary";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("temporary", -1);
        ref.setValue(hashMap);
        registerReceiver(mReceiver, new IntentFilter(TrackerService.STOP_SERVICE_ACTION));
    }

    private void requestLocation() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, getString(R.string.error_location_permission), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            int permission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permission == PackageManager.PERMISSION_GRANTED) {
                startDrivingService();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length >= 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDrivingService();
        } else {
            Toast.makeText(this, getString(R.string.error_location_permission), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRiderList() {
        final String path = "rides/" + rideCode + "/riders";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);

        RecyclerView rvRiders = findViewById(R.id.rv_riders);
        mAdapter = new RidersAdapter(this);
        rvRiders.setAdapter(mAdapter);
        rvRiders.setLayoutManager(new LinearLayoutManager(this));
        rvRiders.setHasFixedSize(true);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Rider rider;
                List<Rider> riders = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    rider = snapshot.getValue(Rider.class);
                    riders.add(rider);
                }
                RiderUtils.updateOrders(riders);

                final String path = "rides/" + rideCode + "/riders";
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                HashMap<String, Rider> hashMap = new HashMap<>();
                for (Rider rider1 : riders) {
                    hashMap.put(rider1.id, rider1);
                }
                ref.setValue(hashMap);
                mAdapter.submitList(riders);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Error reading the riders from the database");
            }
        });
    }

    private void startDrivingService() {
        Intent intent = new Intent(this, TrackerService.class);
        intent.putExtra(getString(R.string.ride_code), rideCode);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        removeSharedPreferencesWidgetString();
        Toast.makeText(this, getString(R.string.end_ride_success), Toast.LENGTH_SHORT).show();
        unregisterReceiver(mReceiver);
        endRide();
        super.onDestroy();
    }

    private void endRide() {
        final String path = "rides/" + rideCode;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        Intent intent = new Intent(TrackerService.STOP_SERVICE_ACTION);
        sendBroadcast(intent);
        ref.removeValue();
    }

    @Override
    public void onRiderClick(int position, List<Rider> riders) {
        Rider clickedRider = riders.get(position);
        if (clickedRider.status == Rider.STATUS_TO_BE_PICKED) {
            clickedRider.status = Rider.STATUS_IN_RIDE;
            updateRider(clickedRider);
        } else if (clickedRider.status == Rider.STATUS_IN_RIDE) {
            deleteRider(clickedRider);
        }
        mAdapter.submitList(riders);
    }

    private void updateRider(Rider rider) {
        final String path = "rides/" + rideCode + "/riders/" + rider.id;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        ref.setValue(rider);
    }

    private void deleteRider(Rider rider) {
        final String path = "rides/" + rideCode + "/riders/" + rider.id;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        ref.removeValue();
    }

    @Override
    public void onDirectionsClick(double lat, double lng) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + lat + "," + lng));
        startActivity(intent);
    }

    private void createSharedPreferencesWidgetString() {
        String widgetString = getString(R.string.ride_code_with_string, rideCode);

        SharedPreferences sharedpreferences = getSharedPreferences(getString(R.string.shared_preferences_rides_string), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(getString(R.string.shared_preferences_widget_string_key), widgetString);
        editor.apply();

        startWidgetIntentService();
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
