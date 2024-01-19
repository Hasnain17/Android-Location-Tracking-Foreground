package com.app.android.work_manager.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.core.app.NotificationCompat;


import com.app.android.work_manager.MainActivity;
import com.app.android.work_manager.R;
import com.app.android.work_manager.utils.BitmapUtils;

import java.util.Random;
/**
 * @Author: Muhammad Hasnain Altaf
 * @Date: 19/01/2024
 */
public class NotificationHelper {

    private static final int NOTIFICATION_ID = 1;

    public  void showNotification(Context context, String title, String content) {

        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationManager notificationManager;
        notificationManager = context.getSystemService(NotificationManager.class);
        // Create a notification channel for Android 8.0 (Oreo) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("newNotification", "NotificationForFuelTrack", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("NotificationForFuelTrack");
            notificationManager.createNotificationChannel(channel);
        }

        BitmapUtils bitmapUtils=new BitmapUtils();
        Bitmap bitmapBadge = bitmapUtils.vectorToBitmap(context, R.drawable.ic_logo_splash_new);
        Bitmap bitmapLargeIcon = bitmapUtils.vectorToBitmap(context,R.mipmap
                .ic_logo_new);


        // Build the notification
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(context, "newNotification")
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setSmallIcon(Icon.createWithBitmap(bitmapBadge)) // Set your notification icon
                    .setLargeIcon(bitmapLargeIcon)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .build();


            notificationManager.notify(NOTIFICATION_ID, notification);
        }
        else {
            notification = new NotificationCompat.Builder(context)
                    .setContentIntent(pendingIntent)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setSmallIcon(R.mipmap.ic_logo_new) // Set your notification icon
                    .setLargeIcon(bitmapLargeIcon)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .build();
            assert notificationManager != null;
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private int getRandomNumber() {
        Random r = new Random();
        return r.nextInt(1000);
    }

}
