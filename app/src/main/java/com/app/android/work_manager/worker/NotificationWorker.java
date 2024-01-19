package com.app.android.work_manager.worker;

/**
 * @Author: Muhammad Hasnain Altaf
 * @Date: 19/01/2024
 */

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.android.work_manager.utils.NotificationHelper;

public class NotificationWorker extends Worker {



    private final Context mContext;
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext=context;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Implement the logic for displaying the notification
        NotificationHelper notificationHelper=new NotificationHelper();
        notificationHelper.showNotification(mContext,"Hasnain","Notification for Tracking");
        return Result.success();
    }
}
