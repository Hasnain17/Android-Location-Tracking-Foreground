package com.app.android.work_manager.service;

/**
 * @Author: Muhammad Hasnain Altaf
 * @Date: 07/02/2024
 */

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;

import com.app.android.work_manager.MainActivity;
import com.app.android.work_manager.R;
import com.app.android.work_manager.data.LocationEvent;
import com.app.android.work_manager.utils.BitmapUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

public class LocationService extends Service {

    private static final String CHANNEL_ID = "12345";
    private static final int NOTIFICATION_ID = 12345;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private Location location;

    private Location location1;

    private double kmValue = 0.0;

    private long totalTimeMillis = 0;
    private long startTimeMillis;


    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult);
            }
        };

        startForeground(NOTIFICATION_ID, createNotification());


        startTimeMillis = SystemClock.elapsedRealtime();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        createLocationRequest();
        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void createLocationRequest() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        stopForeground(true);
        stopSelf();
    }

    private void onNewLocation(LocationResult locationResult) {
        location = locationResult.getLastLocation();

        if (location1 != null) {
            float distanceInMeters;
            float distanceInKilometers;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                distanceInMeters = location1.distanceTo(location);
                distanceInKilometers = distanceInMeters / 1000.0f;
            } else {
                float[] results = new float[1];
                Location.distanceBetween(
                        location1.getLatitude(), location1.getLongitude(),
                        location.getLatitude(), location.getLongitude(),
                        results);

                // The distance is stored in results[0]
                distanceInMeters = results[0];
                distanceInKilometers = (float) (distanceInMeters / 1000.0);
            }
            kmValue += distanceInKilometers;

            // Calculate total time
            totalTimeMillis = SystemClock.elapsedRealtime() - startTimeMillis;

            callForTransaction(location,kmValue,totalTimeMillis);
        }
        location1=location;
    }

    private void callForTransaction(Location location, double kmValue, long totalTimeMillis) {
        double elapsedTimeHours = totalTimeMillis / (1000.0 * 60.0 * 60.0); // Convert millis to hours

        double averageSpeed = (kmValue / elapsedTimeHours);

        EventBus.getDefault().post(new LocationEvent(
                location, kmValue,averageSpeed,totalTimeMillis));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private Notification createNotification() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        // Create a notification channel for Android 8.0 (Oreo) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("newNotification", "NotificationForFuelTrack", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("NotificationForFuelTrack");
            notificationManager.createNotificationChannel(channel);
        }


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


    @Override
    public void onDestroy() {
        super.onDestroy();
        removeLocationUpdates();
    }

}
