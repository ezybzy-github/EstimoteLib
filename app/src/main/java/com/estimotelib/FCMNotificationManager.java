package com.estimotelib;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class FCMNotificationManager {
    public static final String TAG = "FCMNotificationManager";

    private NotificationManager notificationManager;

    public FCMNotificationManager(Context context) {
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void createNotification(String title, Context context, String msg, String url, String className) {
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;
        if (notificationManager == null) {
            notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = notificationManager.getNotificationChannel(context.getResources().getString(R.string.default_notification_channel_id));
            if (mChannel == null) {
                mChannel = new NotificationChannel(context.getResources().getString(R.string.default_notification_channel_id),
                        "Things near you", importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context,
                    context.getResources().getString(R.string.default_notification_channel_id));

            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context, className));
            intent.putExtra("WEB_VIEW_URL", url);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            builder.setContentTitle(title)                            // required
                    .setSmallIcon(R.drawable.ic_lib_notifications)   // required
                    .setContentText(msg) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        }
        else {
            builder = new NotificationCompat.Builder(context,
                    context.getResources().getString(R.string.default_notification_channel_id));

            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context, className));
            intent.putExtra("WEB_VIEW_URL", url);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            builder.setContentTitle(title)                            // required
                    .setSmallIcon(R.drawable.ic_lib_notifications)   // required
                    .setContentText(msg) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        Notification notification = builder.build();
        notificationManager.notify(generateRandomNotifyId(), notification);
    }

    public void createPictureTypeNotification(Context context, String title, String message, String imageUrl,
                                              String url, String className){
        try {
            createPictureStyleNotification(context,title, message,imageUrl,url,className);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPictureStyleNotification(Context mContext, String title, String message,
                                                String imageUrl, String url, String className) throws IOException {
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;
        if (notificationManager == null) {
            notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = notificationManager.getNotificationChannel(mContext.getResources().getString(R.string.default_notification_channel_id));
            if (mChannel == null) {
                mChannel = new NotificationChannel(mContext.getResources().getString(R.string.default_notification_channel_id),
                        "Things near you", importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(mContext,
                    mContext.getResources().getString(R.string.default_notification_channel_id));

            Intent intent = new Intent();
            intent.setComponent(new ComponentName(mContext, className));
            intent.putExtra("WEB_VIEW_URL", url);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            builder.setContentTitle(title)                            // required
                    .setSmallIcon(R.drawable.ic_lib_notifications)   // required
                    .setContentText(message) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setLargeIcon(Picasso.with(mContext).load(imageUrl).get())
                    .setStyle(new NotificationCompat.BigPictureStyle()
                    //This one is same as large icon but it wont show when its expanded that's why we again setting
                    .bigLargeIcon(Picasso.with(mContext).load(imageUrl).get())
                    //This is Big Banner image
                    .bigPicture(Picasso.with(mContext).load(imageUrl).get())
                    //When Notification expanded title and content text
                    .setBigContentTitle(title)
                    .setSummaryText(message));
        }
        else {
            builder = new NotificationCompat.Builder(mContext,
                    mContext.getResources().getString(R.string.default_notification_channel_id));

            Intent intent = new Intent();
            intent.setComponent(new ComponentName(mContext, className));

            intent.putExtra("WEB_VIEW_URL", url);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            builder.setContentTitle(title)                            // required
                    .setSmallIcon(R.drawable.ic_lib_notifications)   // required
                    .setContentText(message) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setLargeIcon(Picasso.with(mContext).load(imageUrl).get())
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            //This one is same as large icon but it wont show when its expanded that's why we again setting
                            .bigLargeIcon(Picasso.with(mContext).load(imageUrl).get())
                            //This is Big Banner image
                            .bigPicture(Picasso.with(mContext).load(imageUrl).get())
                            //When Notification expanded title and content text
                            .setBigContentTitle(title)
                            .setSummaryText(message));
        }
        Notification notification = builder.build();
        notificationManager.notify(generateRandomNotifyId(), notification);
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    private int generateRandomNotifyId(){
        int min = 10;
        int max = 80;
        Random random = new Random();
        int randomNumber = random.nextInt(max-min) + 65;
        return randomNumber;
    }
}
