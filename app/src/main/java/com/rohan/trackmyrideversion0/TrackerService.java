package com.rohan.trackmyrideversion0;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TrackerService extends Service {
    private static final String TAG = TrackerService.class.getSimpleName();
    private static final String TRACKING_NOTIFICATION_CHANNEL_ID = "tracking-notification-channel";

    public static final String STOP_SERVICE_ACTION = "stop-service";

    private FusedLocationProviderClient mLocationProviderClient;

    private String rideCode;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        rideCode = intent.getStringExtra(getString(R.string.ride_code));
        return START_STICKY;
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            final String path = "rides/" + rideCode + "/driver_location/123";
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
            Location location = locationResult.getLastLocation();
            if (location != null) {
                Log.d(TAG, "location update = " + location);
                ref.setValue(location);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        buildNotification();

        requestLocationUpdates();
    }

    private void buildNotification() {
        registerReceiver(stopReceiver, new IntentFilter(STOP_SERVICE_ACTION));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(STOP_SERVICE_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,TRACKING_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.mipmap.ic_launcher_0)
                .setPriority(NotificationCompat.PRIORITY_MIN);
        startForeground(1, builder.build());
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received stop broadcast");
            // Stop the service when the notification is tapped
            unregisterReceiver(stopReceiver);
            stopSelf();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            mLocationProviderClient.requestLocationUpdates(request, mLocationCallback, null);
        }
    }
}
