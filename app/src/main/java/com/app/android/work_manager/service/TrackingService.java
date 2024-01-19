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
import android.os.IBinder;
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

                    // Calculate the distance between location1 and location
                    float[] results = new float[1];
                    Location.distanceBetween(
                            location1.getLatitude(), location1.getLongitude(),
                            location.getLatitude(), location.getLongitude(),
                            results);

                    // The distance is stored in results[0]
                    float distanceInMeters = results[0];

                    // Convert to kilometers
                    double distanceInKm = distanceInMeters / 1000.0;
                    kmValue += distanceInKm;

                    // Set location as the new starting point for the next calculation
                    location1 = location;

                    // Broadcast the updated location values to the MainActivity
                    broadcastLocationUpdate(kmValue, SystemClock.elapsedRealtime() - startTimeMillis);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, // minTime
                    0, // minDistance
                    locationListener);
        }
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
    }

    // Method to broadcast location updates to the MainActivity
    private void broadcastLocationUpdate(double distance, long elapsedTimeMillis) {
        Intent intent = new Intent(UPDATE_LOCATION_ACTION);
        intent.putExtra("distance", distance);
        intent.putExtra("elapsedTimeMillis", elapsedTimeMillis);
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
            notification = new NotificationCompat.Builder(this)
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
