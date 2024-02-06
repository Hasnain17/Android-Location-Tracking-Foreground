package com.app.android.work_manager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.app.android.work_manager.service.TrackingService;
import com.app.android.work_manager.worker.NotificationWorker;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
/**
 * @Author: Muhammad Hasnain Altaf
 * @Date: 19/01/2024
 */
public class MainActivity extends AppCompatActivity {

    private Button btnStartTracking, btnStopTracking;
    private TextView tvTime, tvKm,tvAverageSpeed;

    private static final String NOTIFICATION_WORK_TAG = "notificationWork";

    private static final String UPDATE_LOCATION_ACTION = "com.app.android.work_manager.UPDATE_LOCATION_ACTION";

    private static final String DISTANCE_FORMAT = "%.1f km";
    private static final String SPEED_FORMAT = "%.1f km/h";

    private static final String TIME_FORMAT = "Time: %02d:%02d";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartTracking = findViewById(R.id.btnStartTracking);
        btnStopTracking = findViewById(R.id.btnStopTracking);
        tvTime = findViewById(R.id.tvTime);
        tvKm = findViewById(R.id.tvKm);
        tvAverageSpeed = findViewById(R.id.tvAverageSpeed);



        btnStartTracking.setOnClickListener(view -> startTracking());

        btnStopTracking.setOnClickListener(view -> stopTracking());


        callForPostNotificationPermission();
    }

    @Override
    protected void onResume() {
        // Register the receiver for location updates
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver,
                new IntentFilter(UPDATE_LOCATION_ACTION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);

        super.onPause();
    }

    // Receiver to handle location updates from the service
    private final BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double distance = intent.getDoubleExtra("distance", 0.0);
            long elapsedTimeMillis = intent.getLongExtra("elapsedTimeMillis", 0);
            double averageSpeed = intent.getDoubleExtra("averageSpeed", 0.0);

            // Debugging statements
            Log.d("LocationUpdateReceiver", "Received location update");
            Log.d("LocationUpdateReceiver", "Distance: " + distance);
            Log.d("LocationUpdateReceiver", "Elapsed Time: " + elapsedTimeMillis);
            Log.d("LocationUpdateReceiver", "Average Speed: " + averageSpeed);

            // Update UI with the new location values
            updateUI(distance, elapsedTimeMillis, averageSpeed);
        }
    };

    private void startTracking() {
        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        else {
            callForTracking();
        }
    }

    private void callForTracking() {
        // Start the foreground service
        startService(new Intent(this, TrackingService.class));


        // Schedule a periodic work for notifications every 15 minutes
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 2, TimeUnit.MINUTES)
                .setInitialDelay(2, TimeUnit.MINUTES)
                .build();


        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        NOTIFICATION_WORK_TAG,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        periodicWorkRequest);
    }

    private void callForPostNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS},101);
            }
        }
    }

    private void stopTracking() {
        // Stop the foreground service
        stopService(new Intent(this, TrackingService.class));
        // Cancel the periodic work for notifications
        WorkManager.getInstance(this).cancelUniqueWork(NOTIFICATION_WORK_TAG);
    }



    // Method to update UI with location values
    private void updateUI(double distance, long elapsedTimeMillis, double averageSpeed) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis) - TimeUnit.MINUTES.toSeconds(minutes);

        runOnUiThread(() -> {
            if (tvTime != null) {
                tvTime.setText(String.format(Locale.getDefault(),TIME_FORMAT, minutes, seconds));
            }

            if (tvKm != null) {
                tvKm.setText(String.format(Locale.getDefault(), DISTANCE_FORMAT, distance));
            }

            if (tvAverageSpeed != null) {
                tvAverageSpeed.setText(String.format(Locale.getDefault(), SPEED_FORMAT, averageSpeed));
            }
        });

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start tracking
                callForTracking();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
