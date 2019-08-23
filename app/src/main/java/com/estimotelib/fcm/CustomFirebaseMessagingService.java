package com.estimotelib.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class CustomFirebaseMessagingService extends FirebaseMessagingService
{
    private static final String TAG = "FirebaseMessageService";

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
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            String clickAction = remoteMessage.getData().get("click_action");
            String fromTopic = remoteMessage.getData().get("fromTopic");

            handleDataMessage(title,message,clickAction,fromTopic);
        }

        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            String title = remoteMessage.getNotification().getTitle();
            String message = remoteMessage.getNotification().getBody();
            String clickAction = remoteMessage.getNotification().getClickAction();
            String fromTopic = remoteMessage.getNotification().getTag();

            Log.e("FROM_GET_NOTIFICATION",""+title);
            Log.e("FROM_GET_NOTIFICATION",""+message);
            Log.e("FROM_GET_NOTIFICATION",""+clickAction);
            Log.e("FROM_GET_NOTIFICATION",""+fromTopic);

//            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
//            notificationUtils.saveIsNew(true);

            handleDataMessage(title,message,clickAction,null);
        }
    }

    private void handleDataMessage(String title,String msg,String clickEvent,String fromTopic) {

        try {
            Log.e(TAG, "title: " + title);
            Log.e(TAG, "message: " + msg);
            Log.e(TAG, "click_action: " + clickEvent);
            Log.e(TAG, "fromTopic: " + fromTopic);
            Log.e(TAG, "NotificationUtils: " + fromTopic);

        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }
}