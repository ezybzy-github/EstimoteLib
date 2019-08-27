package com.estimotelib.fcm;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.estimotelib.EstimoLibUtil;
import com.estimotelib.R;
import com.estimotelib.interfaces.NotificationListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class CustomFirebaseMessagingService extends FirebaseMessagingService
{
    private static final String TAG = "FirebaseMessageService";

    NotificationManager notificationManager;

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a data payload.
        Map<String, String> data = remoteMessage.getData();
        try {
            JSONObject dataJSON = new JSONObject(data);
            Log.e("onMessageReceived-->","" + data);
            processNotification(dataJSON);
        } catch (Exception e) {
            Log.e("onMessageReceived-->","" + e.getMessage());
        }
    }

    private void processNotification(JSONObject data)
    {
        String Image = data.optString("Image");
        String url = data.optString("url");
        String title  = data.optString("title");
        String message   = data.optString("message");
        String appName   = data.optString("app_name");

        Log.e(TAG,"Image: "+Image);
        Log.e(TAG,"url: "+url);
        Log.e(TAG,"title: "+title);
        Log.e(TAG,"message: "+message);
        Log.e(TAG,"appName: "+appName);

        createNotification(title,getApplicationContext(),message,Image,url,appName);

//        handleDataMessage(title,Image,url,message,apName);
    }

    /*private void handleDataMessage(String title, String Image, String url, String message, String apName) {

        try {
            Intent intent = new Intent();
            intent.putExtra("url", url);
            intent.setAction(apName);

            createNotification(title,getApplicationContext(),message,intent,Image);
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }*/

    public void createNotification(String title, Context context, String msg, String image, String url, String appName) {

        final int NOTIFY_ID = 0; // ID of notification

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel contentChannel = new NotificationChannel(
                    "content_channel", "Things near you", NotificationManager.IMPORTANCE_HIGH);

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            contentChannel.setSound(alarmSound, attributes);
            notificationManager.createNotificationChannel(contentChannel);
        } else {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("url", url);
        intent.setAction(appName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NOTIFY_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"content_channel");

        builder.setSmallIcon(EstimoLibUtil.notificationIcon())
                .setContentTitle(title)
                .setContentText(msg)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(defaults)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


       /* if(!image.equalsIgnoreCase("null")) {
            Glide.with(context)
                    .asBitmap()
                    .load(image)
                    .into(new BaseTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            builder.setLargeIcon(resource);
                        }

                        @Override
                        public void getSize(@NonNull SizeReadyCallback cb) {

                        }

                        @Override
                        public void removeCallback(@NonNull SizeReadyCallback cb) {

                        }
                    });
        }*/

        Notification notification = builder.build();
        notificationManager.notify(NOTIFY_ID, notification);
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