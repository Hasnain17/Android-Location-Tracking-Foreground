package com.app.android.work_manager.appWidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.app.android.work_manager.R;
import com.app.android.work_manager.service.LocationService;

/**
 * @Author: Muhammad Hasnain Altaf
 * @Date: 30/01/2024
 */
public class TrackingWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_START_TRACKING = "com.app.android.work_manager.START_TRACKING";
    private static final String ACTION_STOP_TRACKING = "com.app.android.work_manager.STOP_TRACKING";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // Set click listeners for buttons
        setButtonClickListeners(context, views, appWidgetId);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void setButtonClickListeners(Context context, RemoteViews views, int appWidgetId) {
        // Start Tracking Button
        Intent startTrackingIntent = new Intent(context, TrackingWidgetProvider.class);
        startTrackingIntent.setAction(ACTION_START_TRACKING);
        PendingIntent startTrackingPendingIntent = PendingIntent.getBroadcast(context, 0, startTrackingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widgetStartButton, startTrackingPendingIntent);

        // Stop Tracking Button
        Intent stopTrackingIntent = new Intent(context, TrackingWidgetProvider.class);
        stopTrackingIntent.setAction(ACTION_STOP_TRACKING);
        PendingIntent stopTrackingPendingIntent = PendingIntent.getBroadcast(context, 1, stopTrackingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widgetStopButton, stopTrackingPendingIntent);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_START_TRACKING.equals(intent.getAction())) {
            // Handle start tracking button click
            startTrackingService(context);
            Toast.makeText(context, "Start Tracking Clicked", Toast.LENGTH_SHORT).show();
        } else if (ACTION_STOP_TRACKING.equals(intent.getAction())) {
            // Handle stop tracking button click
            stopTrackingService(context);
            Toast.makeText(context, "Stop Tracking Clicked", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTrackingService(Context context) {
        // Start the tracking service
        Intent serviceIntent = new Intent(context, LocationService.class);
        context.startService(serviceIntent);
    }

    private void stopTrackingService(Context context) {
        // Stop the tracking service
        Intent serviceIntent = new Intent(context, LocationService.class);
        context.stopService(serviceIntent);
    }
}
