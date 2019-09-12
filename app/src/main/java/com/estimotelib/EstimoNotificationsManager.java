package com.estimotelib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;
import com.estimotelib.controller.PropertyController;
import com.estimotelib.interfaces.ICallbackHandler;
import com.estimotelib.model.AddUserResponse;
import com.estimotelib.model.PropertyExitResponse;
import com.estimotelib.model.PropertyVisitResponse;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static android.content.ContentValues.TAG;

public class EstimoNotificationsManager {
    public static final String TAG = "EstimoNotifications";

    private static Context context;
    private NotificationManager notificationManager;
    private OnBeaconMessageListener mBeaconMessageListener;

    private String key,value;

    private String mIMEINumber;

    private PropertyController mPropertyController;

    public EstimoNotificationsManager(Context mContext) {
        this.context = mContext;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mPropertyController = new PropertyController(mContext);
    }

    public void setBeaconMessageListener(OnBeaconMessageListener beaconMessageListener) {
        this.mBeaconMessageListener = beaconMessageListener;
    }

    public void removeBeaconMessageListener() {
        this.mBeaconMessageListener = null;
    }

    public NotificationCompat.Builder buildNotification(final String title, String key, final String value, int notification_id,
                                                        Class refClass, Class receiver, boolean flag) {
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

        Intent webViewIntent = new Intent(context,refClass);
        webViewIntent.putExtra("WEB_VIEW_URL", value);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notification_id,
                webViewIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        //Pending intent for mute action
        Intent muteIntent = new Intent(context,receiver);
        muteIntent.putExtra("url",value);
        muteIntent.putExtra("notification_id", notification_id);
        muteIntent.setAction("YES_MUTE");
        PendingIntent pendingIntentMute = PendingIntent.getBroadcast(context, notification_id, muteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"content_channel");

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

    private void readAttachmentsAndShowNotifications(final ProximityZoneContext proximityZoneContext, Class refClass,
                                                     Class receiver, boolean flag, String appName) {
        final String beaconId = proximityZoneContext.getDeviceId();

        if(isBeaconNotificationReceivedInTwelveHours(beaconId)) {
            return;
        }

        visitedBeacon(beaconId);

        Map<String, String> attachments = proximityZoneContext.getAttachments();
        int notification_id = 0;

        for(Map.Entry<String, String> attachment: attachments.entrySet()) {
            key = attachment.getKey();
            value = attachment.getValue();
            notification_id = notification_id+1;

            // Decide if the property is visited
            // if property is visited was 30 days ago, if yes we need to consider that property
            boolean isPropertyVisited = false;
            if(matchKeyFromMuteMap(value)) {
                isPropertyVisited = true;

                if(checkDate(value)) {
                    isPropertyVisited = false;
                }
            }

            if(!isPropertyVisited) {
                NotificationCompat.Builder entryNotification = buildNotification(key,
                        key, value, notification_id,refClass,receiver,flag);

                if(notification_id == 1 && mBeaconMessageListener != null) {
                    mBeaconMessageListener.onMessageReceived(key, value);
                }else{
                    notificationManager.notify(notification_id, entryNotification.build());
                }

                sendPropertyEntryRequest(value,appName);
            }
        }
    }

    public void startMonitoring(final Class classRef, final Class receiver, final boolean flag,
                                final String appName) {
        EstimoLibUtil util = new EstimoLibUtil();
        ProximityObserver proximityObserver =
                new ProximityObserverBuilder(context, util.cloudCredentials)
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
                        saveBeaconEnterDetail(proximityContext.getDeviceId());
                        readAttachmentsAndShowNotifications(proximityContext,classRef,receiver,flag,appName);
                        return null;
                    }
                })
                .onExit(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityContext) {
                        saveBeaconExitDetail(proximityContext.getDeviceId());
                        readAttachment(proximityContext,appName);
                        return null;
                    }
                })
                .build();
        proximityObserver.startObserving(zone);
    }

    private void readAttachment(ProximityZoneContext proximityContext,String appName) {
        Map<String, String> attachments = proximityContext.getAttachments();

        Map.Entry<String,String> entry = attachments.entrySet().iterator().next();
        String value = entry.getValue();

        sendExitPropertyRequest(value,appName);

        /*for(Map.Entry<String, String> attachment: attachments.entrySet()) {
            value = attachment.getValue();
            sendExitPropertyRequest(value);
        }*/
    }


    //****************************************************************
    public HashMap<String,String> getMap(SharedPreferences pref)
    {
        HashMap<String, String> map= (HashMap<String, String>) pref.getAll();
        return map;
    }

    //user mute any notification then store here
    public void storeMutedUrl(String url)
    {
        try
        {
            SharedPreferences pref= context.getSharedPreferences("STORED_MUTED_URLS",Context.MODE_PRIVATE);
            HashMap<String, String> map = getMap(pref);
            map.put(url, getCurrentDateTime());

            //Use url as map key and current date as value
            SharedPreferences.Editor editor= pref.edit();

            for (String s : map.keySet()) {
                editor.putString(s, map.get(s));
            }

            editor.apply();
            editor.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Get muted urls
    public boolean matchKeyFromMuteMap(String value)
    {
        boolean urlMatch = false;
        SharedPreferences pref= context.getSharedPreferences("STORED_MUTED_URLS",Context.MODE_PRIVATE);
        HashMap<String, String> map= getMap(pref);

        if(map.containsKey(value))
        {
            urlMatch =  true;
        }
        return urlMatch;
    }

    //Delete from preference
    public void deleteFromMutedUrl(String value){
        SharedPreferences pref= context.getSharedPreferences("STORED_MUTED_URLS",Context.MODE_PRIVATE);
        HashMap<String, String> map= getMap(pref);

        SharedPreferences.Editor editor= pref.edit();
        editor.clear();

        if(map.containsKey(value))
        {
            map.remove(value);

            for (String s : map.keySet()) {
                editor.putString(s, map.get(s));
            }
        }

        editor.apply();
        editor.commit();
    }

    //user mute specific url then notification should not be come before 30 days
    public boolean checkDate(String value)
    {
        boolean dateMatch = false;

        try {
            SharedPreferences pref= context.getSharedPreferences("STORED_MUTED_URLS",Context.MODE_PRIVATE);
            HashMap<String, String> map= getMap(pref);
            String date = map.get(value);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            Date currentDate = new Date();

            // Add 30 days in to input date and calculate new date
            Calendar cDate = Calendar.getInstance();
            cDate.setTime(sdf.parse(date));
            cDate.add(Calendar.DATE, 30);

            String stringDate = sdf.format(cDate.getTime());
            Date newDate = sdf.parse(stringDate);


            if (currentDate.before(newDate)) {
                // for 30 days
                dateMatch = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return dateMatch;
    }

    public String getCurrentDateTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar calender = Calendar.getInstance(Locale.getDefault());
        Date currentTime = calender.getTime();
        String formattedDate = sdf.format(currentTime);

        return formattedDate;
    }

    //store beacon id if user visit first time
    public void visitedBeacon(String beaconId)
    {
        try
        {
            SharedPreferences pref= context.getSharedPreferences("VISITED_BEACON",Context.MODE_PRIVATE);
            HashMap<String, String> map = getMap(pref);
            if(!map.containsKey(beaconId))
            {
                map.put(beaconId, getCurrentDateTime());
            }

            //Use url as map key and current date as value
            SharedPreferences.Editor editor= pref.edit();

            for (String s : map.keySet()) {
                editor.putString(s, map.get(s));
            }

            editor.apply();
            editor.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean isBeaconNotificationReceivedInTwelveHours(final String beaconId) {
        try {
            SharedPreferences pref= context.getSharedPreferences("VISITED_BEACON",Context.MODE_PRIVATE);
            HashMap<String, String> map= getMap(pref);

            if(map.containsKey(beaconId))
            {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date beaconNotificationTime = sdf.parse(map.get(beaconId));
                Date now = Calendar.getInstance().getTime(); // Get time now
                long differenceInMillis = now.getTime() - beaconNotificationTime.getTime();
                long diffSeconds = differenceInMillis / 1000;

                // 12 hours == 43200 seconds
                if(diffSeconds <= 43200) {
                    return true;
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    //Delete from preference
    public void deleteFromVisitedBeacon(String value){
        SharedPreferences pref= context.getSharedPreferences("VISITED_BEACON",Context.MODE_PRIVATE);
        HashMap<String, String> map= getMap(pref);

        SharedPreferences.Editor editor= pref.edit();
        editor.clear();

        if(map.containsKey(value))
        {
            map.remove(value);

            for (String s : map.keySet()) {
                editor.putString(s, map.get(s));
            }
        }

        editor.apply();
        editor.commit();
    }

    public String getTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Calendar calender = Calendar.getInstance(Locale.getDefault());
        Date currentTime = calender.getTime();
        String formattedTime = sdf.format(currentTime);

        return formattedTime;
    }

    public void saveBeaconEnterDetail(String deviceId){
        SharedPreferences pref= context.getSharedPreferences("ENTERED_BEACON",Context.MODE_PRIVATE);
        HashMap<String, String> map= getMap(pref);

        if(!map.containsKey(deviceId))
        {
            map.put(deviceId, getTime());
        }

        SharedPreferences.Editor editor= pref.edit();

        for (String s : map.keySet()) {
            editor.putString(s, map.get(s));
        }

        editor.apply();
        editor.commit();
    }

    public void saveBeaconExitDetail(String deviceId){
        SharedPreferences pref= context.getSharedPreferences("EXIT_BEACON",Context.MODE_PRIVATE);
        HashMap<String, String> map= getMap(pref);

        if(!map.containsKey(deviceId))
        {
            map.put(deviceId, getTime());
        }

        SharedPreferences.Editor editor= pref.edit();

        for (String s : map.keySet()) {
            editor.putString(s, map.get(s));
        }

        editor.apply();
        editor.commit();
    }

    public SharedPreferences getPreference(String name){
        return context.getSharedPreferences(name,Context.MODE_PRIVATE);
    }

    public void getAppNameIMEINumber(String imeinumber){
        mIMEINumber = imeinumber;
    }

    public static void saveFCMToken(String token){
        SharedPreferences sp = context.getSharedPreferences("FCM_TOKEN",Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("TOKEN",token);
        spe.commit();
    }

    public String getFCMToken(){
        SharedPreferences sp = context.getSharedPreferences("FCM_TOKEN",Context.MODE_PRIVATE);
        return sp.getString("TOKEN","");
    }

    private void saveUserId(String UserId){
        SharedPreferences sp = context.getSharedPreferences("USER_ID",Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("UserId",UserId);
        spe.commit();
    }

    private String getUserId(){
        SharedPreferences sp = context.getSharedPreferences("USER_ID",Context.MODE_PRIVATE);
        return sp.getString("UserId","");
    }

    private void sendPropertyEntryRequest(String url,String appName){
        mPropertyController.visitProperty(getFCMToken(), url, appName, mIMEINumber, new ICallbackHandler<PropertyVisitResponse>() {
            @Override
            public void response(PropertyVisitResponse response) {
                saveUserId(String.valueOf(response.getUserId()));
                Log.e(TAG,"PROPERTY_ENTRY: "+new Gson().toJson(response));
            }

            @Override
            public void isError(String errorMsg) {

            }
        });
    }

    private void sendExitPropertyRequest(String url,String appName){
        mPropertyController.exitProperty(getUserId(),url,getFCMToken(), appName, mIMEINumber,
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

    public void sendAddUserRequest(String userName,String appName){
        Log.e(TAG,"mUserName: "+userName);
        Log.e(TAG,"getFCMToken(): "+getFCMToken());
        Log.e(TAG,"mAppName: "+appName);
        Log.e(TAG,"mIMEINumber: "+mIMEINumber);

        mPropertyController.addUser(userName,"Android",getFCMToken(),appName, mIMEINumber,
                new ICallbackHandler<AddUserResponse>() {
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
