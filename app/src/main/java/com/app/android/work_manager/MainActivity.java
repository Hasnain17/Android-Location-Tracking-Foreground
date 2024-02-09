package com.app.android.work_manager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.app.android.work_manager.data.LocationEvent;
import com.app.android.work_manager.service.LocationService;

import com.app.android.work_manager.worker.NotificationWorker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
/**
 * @Author: Muhammad Hasnain Altaf
 * @Date: 19/01/2024
 */
public class MainActivity extends AppCompatActivity {

    private Button btnStartTracking, btnStopTracking;
    private TextView tvTime, tvKm,tvAverageSpeed,tvLat,tvLng;

    private static final String NOTIFICATION_WORK_TAG = "notificationWork";

    private static final String DISTANCE_FORMAT = "%.1f km";
    private static final String SPEED_FORMAT = "%.1f km/h";

    private static final String TIME_FORMAT = "Time: %02d:%02d";

    private static final String latPlaceholder="Latitude -> ";
    private  static final String logPlaceholder="Longitude -> ";
    private  static final String distancePlaceholder="Distance -> ";
    private  static final String averagePlaceholder="Average Speed -> ";


    private final ActivityResultLauncher<String> backgroundLocationLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (result) {
            // Permission granted, do something if needed
            callForTracking();
        }
    });

    private final ActivityResultLauncher<String[]> locationPermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Boolean.TRUE.equals(permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false))) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    }
                }
            }
        }
        else {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    });

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionsLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        } else {
            callForTracking();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartTracking = findViewById(R.id.btnStartTracking);
        btnStopTracking = findViewById(R.id.btnStopTracking);
        tvTime = findViewById(R.id.tvTime);
        tvKm = findViewById(R.id.tvKm);
        tvAverageSpeed = findViewById(R.id.tvAverageSpeed);
        tvLat=findViewById(R.id.tvLat);
        tvLng=findViewById(R.id.tvLng);

        btnStartTracking.setOnClickListener(view -> checkPermissions());

        btnStopTracking.setOnClickListener(view -> stopTracking());

        callForPostNotificationPermission();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }


    private void callForTracking() {
        // Start the foreground service
        startService(new Intent(this, LocationService.class));

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
        stopService(new Intent(this, LocationService.class));

        // Cancel the periodic work for notifications
        WorkManager.getInstance(this).cancelUniqueWork(NOTIFICATION_WORK_TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(new Intent(this, LocationService.class));
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe
    public void receiveLocationEvent(LocationEvent locationEvent) {

        tvLat.setText(latPlaceholder.concat(String.valueOf(locationEvent.getLocation().getLatitude())));
        tvLng.setText(logPlaceholder.concat(String.valueOf(locationEvent.getLocation().getLongitude())));

        tvKm.setText(String.format(distancePlaceholder.concat(String.format(Locale.getDefault(), DISTANCE_FORMAT, locationEvent.getTotalDistance()))));

        if (locationEvent.getAverageSpeed()>10.0){
            //Just for temp purposes, In our we will just send the average in parameter of API POST REQUEST.......
            tvAverageSpeed.setText(averagePlaceholder.concat(String.format(Locale.getDefault(), SPEED_FORMAT, locationEvent.getAverageSpeed())));
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(locationEvent.getTotalTime());
        long seconds = TimeUnit.MILLISECONDS.toSeconds(locationEvent.getTotalTime()) - TimeUnit.MINUTES.toSeconds(minutes);

        tvTime.setText(String.format(Locale.getDefault(),TIME_FORMAT, minutes, seconds));
    }
}


