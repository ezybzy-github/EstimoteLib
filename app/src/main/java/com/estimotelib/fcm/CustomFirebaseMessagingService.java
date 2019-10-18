package com.estimotelib.fcm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.estimotelib.FCMNotificationManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONObject;
import java.util.List;
import java.util.Map;

public class CustomFirebaseMessagingService extends FirebaseMessagingService
{
    private static final String TAG = "FirebaseMessageService";

    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.e(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
        }
        // Check if message contains a data payload.
        Map<String, String> data = remoteMessage.getData();
        try {
            JSONObject dataJSON = new JSONObject(data);
            Log.e("onMessageReceived-->","" + data);
            processNotification(dataJSON,remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        } catch (Exception e) {
            Log.e("onMessageReceived-->","" + e.getMessage());
        }
    }

    private void processNotification(JSONObject data,String title,String msg)
    {
        String Image = data.optString("image");
        String url = data.optString("url");
        String appName   = data.optString("app_name");

        Log.e(TAG,"Image: "+Image);
        Log.e(TAG,"url: "+url);
        Log.e(TAG,"title: "+title);
        Log.e(TAG,"message: "+msg);
        Log.e(TAG,"appName: "+appName);

        Intent intent = new Intent("NewNotification");
        intent.putExtra("image", Image);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        intent.putExtra("message", msg);
        intent.putExtra("app_name", appName);
        broadcaster.sendBroadcast(intent);
    }
}