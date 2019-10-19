package com.estimotelib;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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
import com.estimotelib.interfaces.OnBeaconMessageListener;
import com.estimotelib.model.AddUserResponse;
import com.estimotelib.model.PropertyExitResponse;
import com.estimotelib.model.PropertyVisitResponse;
import com.estimotelib.receiver.ActionButtonReceiver;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Random;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class EstimoteNotificationManager {
    public static final String TAG = "EstimoNotifications";

    private NotificationManager notificationManager;
    private String key,value;
    private PropertyController mPropertyController;
    private boolean isFirstTime = false;
    private OnBeaconMessageListener mBeaconMessageListener;

    private PreferenceUtil mPreferenceUtil;

    public EstimoteNotificationManager(Context context) {
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mPropertyController = new PropertyController(context);

        mPreferenceUtil = new PreferenceUtil();
    }

    public void setBeaconMessageListener(OnBeaconMessageListener beaconMessageListener) {
        this.mBeaconMessageListener = beaconMessageListener;
    }

    public void removeBeaconMessageListener() {
        this.mBeaconMessageListener = null;
    }

    public NotificationCompat.Builder buildNotification(Activity mContext, final String title, final String value, int notification_id,
                                                        Class classRef, boolean flag) throws ClassNotFoundException {
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

        Intent webViewIntent = new Intent(mContext,classRef);
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
                                                     Class classRef, boolean flag, String appName) throws ClassNotFoundException {
        final String beaconId = proximityZoneContext.getDeviceId();

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
                if(!isFirstTime && mBeaconMessageListener != null){
                    isFirstTime = true;
                    mBeaconMessageListener.onMessageReceived(key, value);
                }else{
                    NotificationCompat.Builder entryNotification = buildNotification(mContext,key,
                            value, randomNotificationId(),classRef,flag);
                    notificationManager.notify(randomNotificationId(), entryNotification.build());
                }
            }

            sendPropertyEntryRequest(mContext,value,appName);
        }
    }

    private int randomNotificationId(){
        int min = 20;
        int max = 80;
        int random = new Random().nextInt((max - min) + 1) + min;
        return random;
    }

    public void startMonitoring(final Activity mContext, final Class classRef, final boolean flag, final String appName) {
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
                            readAttachmentsAndShowNotifications(mContext,proximityContext,classRef,flag,appName);
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
        Map<String, String> attachments = proximityContext.getAttachments();

        Map.Entry<String, String> entry = attachments.entrySet().iterator().next();
        String value = entry.getValue();

        sendExitPropertyRequest(context,value,appName);
    }

    private void sendPropertyEntryRequest(final Context context, String url, String appName){
        Log.e(TAG,"FCMToken: "+mPreferenceUtil.getFCMToken(context));
        Log.e(TAG,"URL: "+url);
        Log.e(TAG,"App name: "+appName);
        Log.e(TAG,"IMEINumber: "+mPreferenceUtil.mIMEINumber);
        mPropertyController.visitProperty(mPreferenceUtil.getFCMToken(context), url, appName,
                mPreferenceUtil.mIMEINumber, new ICallbackHandler<PropertyVisitResponse>() {
            @Override
            public void response(PropertyVisitResponse response) {
                mPreferenceUtil.saveUserId(context, String.valueOf(response.getUserId()));
                Log.e(TAG,"PROPERTY_ENTRY: "+new Gson().toJson(response));
            }

            @Override
            public void isError(String errorMsg) {
                Log.e(TAG,"PROPERTY_ENTRY ERROR: "+errorMsg);
            }
        });
    }

    private void sendExitPropertyRequest(Context context, String url, String appName){
        mPropertyController.exitProperty(mPreferenceUtil.getUserId(context),url,mPreferenceUtil.getFCMToken(context),
                appName, mPreferenceUtil.mIMEINumber,
                new ICallbackHandler<PropertyExitResponse>() {
            @Override
            public void response(PropertyExitResponse response) {
                Log.e(TAG,"PROPERTY_EXIT: "+response.getMessage());
            }

            @Override
            public void isError(String errorMsg) {

            }
        });
    }

    public void sendAddUserRequest(Context context, String userName, String appName){
        Log.e(TAG,"mUserName: "+userName);
        Log.e(TAG,"getFCMToken(): "+mPreferenceUtil.getFCMToken(context));
        Log.e(TAG,"mAppName: "+appName);
        Log.e(TAG,"mIMEINumber: "+mPreferenceUtil.mIMEINumber);

        mPropertyController.addUser(userName,"Android",mPreferenceUtil.getFCMToken(context),appName,
                mPreferenceUtil.mIMEINumber,new ICallbackHandler<AddUserResponse>() {
                    @Override
                    public void response(AddUserResponse response) {
                        Log.e(TAG,"ADD_USER: "+new Gson().toJson(response));
                    }

                    @Override
                    public void isError(String errorMsg) {

                    }
                });
    }
}
