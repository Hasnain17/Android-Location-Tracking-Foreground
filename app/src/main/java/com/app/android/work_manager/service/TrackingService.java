package com.app.android.work_manager.service;

/**
 * @Author: Muhammad Hasnain Altaf
 * @Date: 19/01/2024
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.app.android.work_manager.MainActivity;
import com.app.android.work_manager.R;
import com.app.android.work_manager.utils.BitmapUtils;

public class TrackingService extends Service {

    private static final int NOTIFICATION_ID = 100;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Location location1;
    private static final String UPDATE_LOCATION_ACTION = "com.app.android.work_manager.UPDATE_LOCATION_ACTION";

    private double kmValue = 0.0;
    private long startTimeMillis;


    private Handler handler;
    private Runnable updateTimerRunnable;
    private static final long TIMER_UPDATE_INTERVAL = 1000; // 1 second


    private double totalDistance = 0.0;
    private long totalTimeMillis = 0;



    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, createNotification());

        // Initialize location manager and listener
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);



        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (location1 == null) {
                    // First time getting a location, set it as the starting point
                    location1 = location;
                } else {
                    float distanceInMeters;
                    float distanceInKilometers;
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
                        distanceInMeters = location1.distanceTo(location);
                     distanceInKilometers = distanceInMeters / 1000.0f;
                    }
                    else {
                        // Calculate the distance between location1 and location
                        float[] results = new float[1];
                        Location.distanceBetween(
                                location1.getLatitude(), location1.getLongitude(),
                                location.getLatitude(), location.getLongitude(),
                                results);

                        // The distance is stored in results[0]
                        distanceInMeters = results[0];
                        distanceInKilometers= (float) (distanceInMeters / 1000.0);
                    }


                    kmValue += distanceInKilometers;
                    // Calculate total distance
                    totalDistance += distanceInKilometers;

                    // Calculate total time
                    totalTimeMillis = SystemClock.elapsedRealtime() - startTimeMillis;

                    // Set location as the new starting point for the next calculation
                    location1 = location;

                    // Broadcast the updated location values to the MainActivity
                    broadcastLocationUpdate(kmValue, totalTimeMillis, totalDistance);
                }
            }



            @Override
            public void onProviderEnabled(@NonNull String provider) {
                // Handle provider enabled
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                // Handle provider disabled
            }
        };
        startTimeMillis = SystemClock.elapsedRealtime();

        // Request location updates
        if (locationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,         // (time interval)
                    0,       // (distance interval)
                    locationListener);
        }


        handler = new Handler(Looper.getMainLooper());
        // Create a runnable to update the timer at regular intervals
        updateTimerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimer();
                handler.postDelayed(this, TIMER_UPDATE_INTERVAL);
            }
        };

        // Start the timer update runnable
        handler.postDelayed(updateTimerRunnable, TIMER_UPDATE_INTERVAL);
    }

    private void updateTimer() {
        long elapsedTimeMillis = SystemClock.elapsedRealtime() - startTimeMillis;
        broadcastLocationUpdate(kmValue, elapsedTimeMillis, totalDistance);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove location updates when service is destroyed
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }

        // Remove the timer update runnable
        handler.removeCallbacks(updateTimerRunnable);
    }

    private void broadcastLocationUpdate(double distance, long elapsedTimeMillis, double totalDistance) {
        double elapsedTimeHours = elapsedTimeMillis / (1000.0 * 60.0 * 60.0); // Convert millis to hours

        double averageSpeed = (totalDistance / elapsedTimeHours);

        Intent intent = new Intent(UPDATE_LOCATION_ACTION);
        intent.putExtra("distance", distance);
        intent.putExtra("elapsedTimeMillis", elapsedTimeMillis);
        intent.putExtra("averageSpeed", averageSpeed);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private Notification createNotification() {
        // Customize the notification as needed
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        // Create a notification channel for Android 8.0 (Oreo) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("newNotification", "NotificationForFuelTrack", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("NotificationForFuelTrack");
            notificationManager.createNotificationChannel(channel);
        }

        BitmapUtils bitmapUtils = new BitmapUtils();
        Bitmap bitmapBadge = bitmapUtils.vectorToBitmap(this, R.drawable.ic_logo_splash_new);
        Bitmap bitmapLargeIcon = bitmapUtils.vectorToBitmap(this, R.mipmap.ic_logo_new);

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, "newNotification")
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setSmallIcon(Icon.createWithBitmap(bitmapBadge)) // Set your notification icon
                    .setLargeIcon(bitmapLargeIcon)
                    .setContentTitle("Tracking Service")
                    .setContentText("Tracking in progress...")
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .build();

            notificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            notification = new NotificationCompat.Builder(this,"newNotification")
                    .setContentIntent(pendingIntent)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setSmallIcon(R.mipmap.ic_logo_new) // Set your notification icon
                    .setLargeIcon(bitmapLargeIcon)
                    .setContentTitle("Tracking Service")
                    .setContentText("Tracking in progress...")
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        return notification;
    }
}
