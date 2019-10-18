package com.estimotelib;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class FCMNotificationManager {
    public static final String TAG = "FCMNotificationManager";

    private NotificationManager notificationManager;

    public FCMNotificationManager(Context context) {
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void createNotification(String title, Context context, String msg, String url, String appName, Class classRef) {

        final int NOTIFY_ID = 0; // ID of notification
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
            if(!isAppIsInBackground(context)){
                intent.setComponent(new ComponentName(context, classRef));
            }else {
                intent.setClass(context, classRef);
            }
            intent.putExtra("WEB_VIEW_URL", url);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            builder.setContentTitle(appName)                            // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                    .setContentText(msg) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(title)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        }
        else {
            builder = new NotificationCompat.Builder(context,
                    context.getResources().getString(R.string.default_notification_channel_id));

            Intent intent = new Intent();
            if(!isAppIsInBackground(context)){
                intent.setComponent(new ComponentName(context, classRef));
            }else {
                intent.setClass(context, classRef);
            }
            intent.putExtra("WEB_VIEW_URL", url);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            builder.setContentTitle(appName)                            // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                    .setContentText(msg) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(title)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        Notification notification = builder.build();
        notificationManager.notify(NOTIFY_ID, notification);
    }

    public void createPictureTypeNotification(Context context, String title, String message, String imageUrl,
                                              String appName, String url, Class classRef){
        new generatePictureStyleNotification(context,title, message,
                imageUrl,appName,url,classRef).execute();
    }

    public class generatePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private String title, message, imageUrl,appName,url;
        private Class classRef;

        public generatePictureStyleNotification(Context context, String title, String message,
                                                String imageUrl, String appName, String url, Class classRef) {
            super();
            this.mContext = context;
            this.title = title;
            this.message = message;
            this.imageUrl = imageUrl;
            this.appName = appName;
            this.url = url;
            this.classRef = classRef;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
            try {
                URL url = new URL(this.imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            final int NOTIFY_ID = 0; // ID of notification
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
                if(!isAppIsInBackground(mContext)){
                    intent.setComponent(new ComponentName(mContext, classRef));
                }else {
                    intent.setClass(mContext, classRef);
                }
                intent.putExtra("WEB_VIEW_URL", url);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                builder.setContentTitle(appName)                            // required
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                        .setContentText(message) // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setTicker(title)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setLargeIcon(result)
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(result));
            }
            else {
                builder = new NotificationCompat.Builder(mContext,
                        mContext.getResources().getString(R.string.default_notification_channel_id));

                Intent intent = new Intent();
                if(!isAppIsInBackground(mContext)){
                    intent.setComponent(new ComponentName(mContext, classRef));
                }else {
                    intent.setClass(mContext, classRef);
                }

                intent.putExtra("WEB_VIEW_URL", url);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                builder.setContentTitle(appName)                            // required
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                        .setContentText(message) // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setTicker(title)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setLargeIcon(result)
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(result));
            }
            Notification notification = builder.build();
            notificationManager.notify(NOTIFY_ID, notification);
        }
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
}
