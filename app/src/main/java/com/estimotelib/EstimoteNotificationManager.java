package com.estimotelib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;
import com.estimotelib.controller.PropertyController;
import com.estimotelib.interfaces.ICallbackHandler;
import com.estimotelib.interfaces.INotificationHandler;
import com.estimotelib.model.NotificationInfo;
import com.estimotelib.model.PropertyExitResponse;
import com.estimotelib.model.PropertyVisitResponse;
import com.estimotelib.receiver.ActionButtonReceiver;
import com.google.gson.Gson;
import java.util.Map;
import java.util.Random;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class EstimoteNotificationManager {
    private static final String TAG = "EstimoNotifications";
    private NotificationManager notificationManager;
    private String key,value;
    private PropertyController mPropertyController;

    private boolean isFirstTime = false;
    private INotificationHandler mInotificatioHandler;

    private PreferenceUtil mPreferenceUtil;
    private NotificationInfo info = null;

    public EstimoteNotificationManager(Context context, final INotificationHandler notificationHandler, String appName) {
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mPropertyController = new PropertyController(context);
        this.mInotificatioHandler = notificationHandler;

        mPreferenceUtil = new PreferenceUtil();
        info = mPreferenceUtil.getNotificationInfo(context,appName);

//        sendPropertyEntryRequest(context,"https://ankita_developer.com/",info.getAppNameAsInt());
//        sendExitPropertyRequest(context,"https://ankita_developer.com/",info.getAppNameAsInt());
    }

    public NotificationCompat.Builder buildNotification(Activity mContext, final String title, final String value,
                                                        int notification_id, boolean flag, String classReference) throws ClassNotFoundException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel contentChannel = new NotificationChannel(
                    "content_channel", "Things near you", NotificationManager.IMPORTANCE_HIGH);

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            contentChannel.setSound(alarmSound, attributes);
            notificationManager.createNotificationChannel(contentChannel);
        }

        Intent webViewIntent = new Intent();
        webViewIntent.setComponent(new ComponentName(mContext, classReference));
        webViewIntent.putExtra("WEB_VIEW_URL", value);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notification_id,
                webViewIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        //Pending intent for mute action
        Intent muteIntent = new Intent(mContext, ActionButtonReceiver.class);
        muteIntent.putExtra("url",value);
        muteIntent.putExtra("notification_id", notification_id);
        muteIntent.setAction("YES_MUTE");
        PendingIntent pendingIntentMute = PendingIntent.getBroadcast(mContext, notification_id, muteIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext,"content_channel");

        if(flag){
            builder.addAction(R.drawable.ic_mute,"Mute",pendingIntentMute);
        }
        builder.setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(value)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(defaults)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        return builder;
    }

    private void readAttachmentsAndShowNotifications(Activity mContext, final ProximityZoneContext proximityZoneContext,
                                                     boolean flag, String appName) throws ClassNotFoundException {

        if(this.mInotificatioHandler != null) {
            com.estimotelib.model.Notification n = new com.estimotelib.model.Notification();
            n.key = "Key";
            n.type = 2;
            this.mInotificatioHandler.OnNotificationReceived(n);
        }

        final String beaconId = proximityZoneContext.getDeviceId();

        mPreferenceUtil = new PreferenceUtil();
        info = mPreferenceUtil.getNotificationInfo(mContext,appName);

        if(mPreferenceUtil.isBeaconNotificationReceivedInTwelveHours(mContext,beaconId)) {
            return;
        }

        mPreferenceUtil.visitedBeacon(mContext,beaconId);

        Map<String, String> attachments = proximityZoneContext.getAttachments();

        for(Map.Entry<String, String> attachment: attachments.entrySet()) {
            key = attachment.getKey();
            value = attachment.getValue();

            // Decide if the property is visited
            // if property is visited was 30 days ago, if yes we need to consider that property
            boolean isPropertyVisited = false;
            if(mPreferenceUtil.matchKeyFromMuteMap(mContext,value)) {
                isPropertyVisited = true;

                if(mPreferenceUtil.checkDate(mContext,value)) {
                    isPropertyVisited = false;
                }
            }

            if(!isPropertyVisited) {
                if(!isFirstTime){
                    isFirstTime = true;
                    showNotificationDialog(mContext,key,value,appName,info.getClassReference());
                }else{

                    NotificationCompat.Builder entryNotification = buildNotification(mContext,key,
                            value, randomNotificationId(),flag,info.getClassReference());
                    notificationManager.notify(randomNotificationId(), entryNotification.build());
                }
            }

            sendPropertyEntryRequest(mContext,value,info.getAppNameAsInt());
        }
    }

    private int randomNotificationId(){
        int min = 20;
        int max = 80;
        int random = new Random().nextInt((max - min) + 1) + min;
        return random;
    }

    public void startMonitoring(final Activity mContext, final boolean flag, final String appName) {
        mPreferenceUtil = new PreferenceUtil();
        ProximityObserver proximityObserver =
                new ProximityObserverBuilder(mContext, EstimoteLibUtil.cloudCredentials)
                        .onError(new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Log.e("app", "proximity observer error: " + throwable);
                                return null;
                            }
                        })
                        .withBalancedPowerMode()
                        .build();

        ProximityZone zone = new ProximityZoneBuilder()
                .forTag("RealEstate")
                .inCustomRange(3.0)
                .onEnter(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityContext) {
                        mPreferenceUtil.saveBeaconEnterDetail(mContext,proximityContext.getDeviceId());
                        try {
                            readAttachmentsAndShowNotifications(mContext,proximityContext,flag,appName);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .onExit(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityContext) {
                        mPreferenceUtil.saveBeaconExitDetail(mContext,proximityContext.getDeviceId());
                        readAttachment(mContext,proximityContext,appName);
                        return null;
                    }
                })
                .build();
        proximityObserver.startObserving(zone);
    }

    private void readAttachment(Context context, ProximityZoneContext proximityContext, String appName) {
        mPreferenceUtil = new PreferenceUtil();
        info = mPreferenceUtil.getNotificationInfo(context,appName);

        Map<String, String> attachments = proximityContext.getAttachments();

        Map.Entry<String, String> entry = attachments.entrySet().iterator().next();
        String value = entry.getValue();

        sendExitPropertyRequest(context,value,info.getAppNameAsInt());
    }

    public void sendPropertyEntryRequest(final Context context, String url, final int appName){
        mPreferenceUtil = new PreferenceUtil();

//        Log.e(TAG,"PROPERTY_ENTRY getFCMToken: "+mPreferenceUtil.getFCMToken(context));
//        Log.e(TAG,"PROPERTY_ENTRY url: "+url);
//        Log.e(TAG,"PROPERTY_ENTRY appName: "+appName);
//        Log.e(TAG,"PROPERTY_ENTRY getIMEINumber: "+mPreferenceUtil.getIMEINumber(context));

        mPropertyController.visitProperty(mPreferenceUtil.getFCMToken(context), url, appName,
                mPreferenceUtil.getIMEINumber(context), new ICallbackHandler<PropertyVisitResponse>() {
            @Override
            public void response(PropertyVisitResponse response) {
                mPreferenceUtil.saveUserId(context, String.valueOf(response.getUserId()),String.valueOf(appName));
                Log.e(TAG,"PROPERTY_ENTRY: "+new Gson().toJson(response));
            }

            @Override
            public void isError(String errorMsg) {
                Log.e(TAG,"PROPERTY_ENTRY ERROR: "+errorMsg);
            }
        });
    }

    public void sendExitPropertyRequest(Context context, String url, int appName){
        mPreferenceUtil = new PreferenceUtil();

//        Log.e(TAG,"PROPERTY_EXIT getUserId: "+mPreferenceUtil.getUserId(context,String.valueOf(appName)));
//        Log.e(TAG,"PROPERTY_EXIT url: "+url);
//        Log.e(TAG,"PROPERTY_EXIT getFCMToken: "+mPreferenceUtil.getFCMToken(context));
//        Log.e(TAG,"PROPERTY_EXIT appName: "+appName);
//        Log.e(TAG,"PROPERTY_EXIT getIMEINumber: "+mPreferenceUtil.getIMEINumber(context));

        mPropertyController.exitProperty(mPreferenceUtil.getUserId(context,String.valueOf(appName)),
                url,mPreferenceUtil.getFCMToken(context),
                appName, mPreferenceUtil.getIMEINumber(context),
                new ICallbackHandler<PropertyExitResponse>() {
            @Override
            public void response(PropertyExitResponse response) {
                Log.e(TAG,"PROPERTY_EXIT: "+response.getMessage());
            }

            @Override
            public void isError(String errorMsg) {
                Log.e(TAG,"PROPERTY_EXIT: "+errorMsg);
            }
        });
    }

    public void showNotificationDialog(final Activity mContext, final String key, final String value, String appName,
                                       final String classReference)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(appName);
        builder.setMessage(key);
        builder.setCancelable(true);
        builder.setPositiveButton("More Details", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent webViewIntent = new Intent();
                webViewIntent.setComponent(new ComponentName(mContext, classReference));
                webViewIntent.putExtra("WEB_VIEW_URL", value);
                mContext.startActivity(webViewIntent);
            }
        });

        builder.setNegativeButton("Mute", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPreferenceUtil = new PreferenceUtil();
                mPreferenceUtil.storeMutedUrl(mContext,value);
            }
        });

        builder.show();
    }
}
