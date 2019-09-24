package com.estimotelib;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class EstimoteNotificationManager {
    public static final String TAG = "EstimoNotifications";

    private NotificationManager notificationManager;

    private String key,value;

    private String mIMEINumber;

    private PropertyController mPropertyController;

    private boolean isFirstTime = false;

    private OnBeaconMessageListener mBeaconMessageListener;

    public EstimoteNotificationManager(Context mContext) {
        this.notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mPropertyController = new PropertyController(mContext);
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

    private void readAttachmentsAndShowNotifications(Activity mContext,final ProximityZoneContext proximityZoneContext,
                                                     Class classRef,boolean flag, String appName) throws ClassNotFoundException {
        final String beaconId = proximityZoneContext.getDeviceId();

        if(isBeaconNotificationReceivedInTwelveHours(mContext,beaconId)) {
            return;
        }

        visitedBeacon(mContext,beaconId);

        Map<String, String> attachments = proximityZoneContext.getAttachments();

        for(Map.Entry<String, String> attachment: attachments.entrySet()) {
            key = attachment.getKey();
            value = attachment.getValue();

            // Decide if the property is visited
            // if property is visited was 30 days ago, if yes we need to consider that property
            boolean isPropertyVisited = false;
            if(matchKeyFromMuteMap(mContext,value)) {
                isPropertyVisited = true;

                if(checkDate(mContext,value)) {
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

                sendPropertyEntryRequest(mContext,value,appName);
            }
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
                new ProximityObserverBuilder(mContext, EstimoLibUtil.cloudCredentials)
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
                        saveBeaconEnterDetail(mContext,proximityContext.getDeviceId());
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
                        saveBeaconExitDetail(mContext,proximityContext.getDeviceId());
                        readAttachment(mContext,proximityContext,appName);
                        return null;
                    }
                })
                .build();
        proximityObserver.startObserving(zone);
    }

    private void readAttachment(Context context,ProximityZoneContext proximityContext,String appName) {
        Map<String, String> attachments = proximityContext.getAttachments();

        Map.Entry<String,String> entry = attachments.entrySet().iterator().next();
        String value = entry.getValue();

        sendExitPropertyRequest(context,value,appName);
    }


    //****************************************************************
    public HashMap<String,String> getMap(SharedPreferences pref)
    {
        HashMap<String, String> map= (HashMap<String, String>) pref.getAll();
        return map;
    }

    //user mute any notification then store here
    public void storeMutedUrl(Context context, String url)
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
    public boolean matchKeyFromMuteMap(Context context, String value)
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
    public void deleteFromMutedUrl(Context context, String value){
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
    public boolean checkDate(Context context, String value)
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

            if (currentDate.after(newDate)) {
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
    public void visitedBeacon(Context context, String beaconId)
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

    public boolean isBeaconNotificationReceivedInTwelveHours(Context context, final String beaconId) {
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
    public void deleteFromVisitedBeacon(Context context, String value){
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

    public void saveBeaconEnterDetail(Context context, String deviceId){
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

    public void saveBeaconExitDetail(Context context,String deviceId){
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

    public SharedPreferences getPreference(Context context, String name){
        return context.getSharedPreferences(name,Context.MODE_PRIVATE);
    }

    public void getAppNameIMEINumber(String imeinumber){
        mIMEINumber = imeinumber;
    }

    public static void saveFCMToken(Context context, String token){
        SharedPreferences sp = context.getSharedPreferences("FCM_TOKEN",Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("TOKEN",token);
        spe.commit();
    }

    public String getFCMToken(Context context){
        SharedPreferences sp = context.getSharedPreferences("FCM_TOKEN",Context.MODE_PRIVATE);
        return sp.getString("TOKEN","");
    }

    private void saveUserId(Context context, String UserId){
        SharedPreferences sp = context.getSharedPreferences("USER_ID",Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("UserId",UserId);
        spe.commit();
    }

    private String getUserId(Context context){
        SharedPreferences sp = context.getSharedPreferences("USER_ID",Context.MODE_PRIVATE);
        return sp.getString("UserId","");
    }

    private void sendPropertyEntryRequest(final Context context, String url, String appName){
        mPropertyController.visitProperty(getFCMToken(context), url, appName, mIMEINumber, new ICallbackHandler<PropertyVisitResponse>() {
            @Override
            public void response(PropertyVisitResponse response) {
                saveUserId(context,String.valueOf(response.getUserId()));
                Log.e(TAG,"PROPERTY_ENTRY: "+new Gson().toJson(response));
            }

            @Override
            public void isError(String errorMsg) {

            }
        });
    }

    private void sendExitPropertyRequest(Context context,String url,String appName){
        mPropertyController.exitProperty(getUserId(context),url,getFCMToken(context), appName, mIMEINumber,
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

    public void sendAddUserRequest(Context context,String userName,String appName){
        Log.e(TAG,"mUserName: "+userName);
        Log.e(TAG,"getFCMToken(): "+getFCMToken(context));
        Log.e(TAG,"mAppName: "+appName);
        Log.e(TAG,"mIMEINumber: "+mIMEINumber);

        mPropertyController.addUser(userName,"Android",getFCMToken(context),appName, mIMEINumber,
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

    public void showNotificationDialog(final Activity mContext, String appName, final String key,
                                        final String value, final Class classRef)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(appName);
        builder.setMessage(key);
        builder.setCancelable(true);
        builder.setPositiveButton("More Details", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent webViewIntent = new Intent(mContext, classRef);
                webViewIntent.putExtra("WEB_VIEW_URL", value);
                mContext.startActivity(webViewIntent);
            }
        });

        builder.setNegativeButton("Mute", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                storeMutedUrl(mContext,value);
            }
        });

        builder.show();
    }
}
