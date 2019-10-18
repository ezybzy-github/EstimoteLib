package com.estimotelib.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.estimotelib.EstimoteNotificationManager;
import com.estimotelib.PreferenceUtil;

public class ActionButtonReceiver extends BroadcastReceiver {
    EstimoteNotificationManager mNm;

    PreferenceUtil mPreferenceUtil;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equalsIgnoreCase("YES_MUTE"))
        {
            mPreferenceUtil = new PreferenceUtil();
            //For cancel the notification on click of mute action button
            int noti_id = intent.getIntExtra("notification_id", 0);
            String url = intent.getStringExtra("url");

            mPreferenceUtil.storeMutedUrl(context,url);

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.cancel(noti_id);
        }
    }
}
