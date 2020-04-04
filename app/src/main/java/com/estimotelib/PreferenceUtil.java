package com.estimotelib;

import android.content.Context;
import android.content.SharedPreferences;
import com.estimotelib.model.NotificationInfo;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceUtil implements Constant{
    public void PreferenceUtil(){

    }

        public HashMap<String, String> getMap(SharedPreferences pref)
        {
            HashMap<String, String> map= (HashMap<String, String>) pref.getAll();
            return map;
        }

    //user mute any notification then store here
    public void storeMutedUrl(Context context, String url)
    {
        try
        {
            SharedPreferences pref= context.getSharedPreferences(STORED_MUTED_URLS, MODE_PRIVATE);
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
        SharedPreferences pref= context.getSharedPreferences(STORED_MUTED_URLS, MODE_PRIVATE);
        HashMap<String, String> map= getMap(pref);

        if(map.containsKey(value))
        {
            urlMatch =  true;
        }
        return urlMatch;
    }

    //Delete from preference
    public void deleteFromMutedUrl(Context context, String value){
        SharedPreferences pref= context.getSharedPreferences(STORED_MUTED_URLS, MODE_PRIVATE);
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
            SharedPreferences pref= context.getSharedPreferences(STORED_MUTED_URLS, MODE_PRIVATE);
            HashMap<String, String> map= getMap(pref);
            String date = map.get(value);

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_WITH_TIME);

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
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_WITH_TIME);
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
            SharedPreferences pref= context.getSharedPreferences(VISITED_BEACON, MODE_PRIVATE);
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
            SharedPreferences pref= context.getSharedPreferences(VISITED_BEACON, MODE_PRIVATE);
            HashMap<String, String> map= getMap(pref);

            if(map.containsKey(beaconId))
            {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_WITH_TIME);
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

    public void deleteFromVisitedBeacon(Context context, String value){
        SharedPreferences pref= context.getSharedPreferences(VISITED_BEACON, MODE_PRIVATE);
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
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        Calendar calender = Calendar.getInstance(Locale.getDefault());
        Date currentTime = calender.getTime();
        String formattedTime = sdf.format(currentTime);

        return formattedTime;
    }

    public void saveBeaconEnterDetail(Context context, String deviceId){
        SharedPreferences pref= context.getSharedPreferences(ENTERED_BEACON, MODE_PRIVATE);
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

    public void saveBeaconExitDetail(Context context, String deviceId){
        SharedPreferences pref= context.getSharedPreferences(EXIT_BEACON, MODE_PRIVATE);
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
        return context.getSharedPreferences(name, MODE_PRIVATE);
    }

    public void saveUniqueID(Context context,String uniqueID,String appNameAsInt){
        SharedPreferences pref = context.getSharedPreferences(PREF_UNIQUE_ID, MODE_PRIVATE);
        HashMap<String, String> map = getMap(pref);

        if(!map.containsKey(appNameAsInt))
        {
            map.put(appNameAsInt, uniqueID);
        }

        SharedPreferences.Editor editor= pref.edit();

        for (String s : map.keySet()) {
            editor.putString(s, map.get(s));
        }

        editor.apply();
    }
    public synchronized String getUniqueID(Context context, String appNameAsInt) {
        String uniqueId = null;
        SharedPreferences pref = context.getSharedPreferences(PREF_UNIQUE_ID, MODE_PRIVATE);
        HashMap<String, String> map = getMap(pref);
        if(map.containsKey(appNameAsInt))
        {
            uniqueId = pref.getString(appNameAsInt,"");
        }

        return uniqueId;
    }

    public void saveFCMToken(Context context, String token){
        SharedPreferences sp = context.getSharedPreferences(FCM_TOKEN, MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(TOKEN,token);
        spe.apply();
    }

    public String getFCMToken(Context context){
        SharedPreferences sp = context.getSharedPreferences(FCM_TOKEN, MODE_PRIVATE);
        return sp.getString(TOKEN,"");
    }

    public void SaveNotificationInfo(Context context, String appNameAsString, NotificationInfo info){
        try{
            Gson gson = new Gson();
            String json = gson.toJson(info);

            SharedPreferences pref = context.getSharedPreferences(NOTIFICATION_INFO, MODE_PRIVATE);
            HashMap<String, String> map = getMap(pref);
            if(!map.containsKey(appNameAsString))
            {
                map.put(appNameAsString, json);
            }

            SharedPreferences.Editor editor= pref.edit();

            for (String s : map.keySet()) {
                editor.putString(s, map.get(s));
            }

            editor.apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public NotificationInfo getNotificationInfo(Context context,String appNameAsString){
        NotificationInfo info = null;
        try{
            SharedPreferences pref = context.getSharedPreferences(NOTIFICATION_INFO, MODE_PRIVATE);
            HashMap<String, String> map = getMap(pref);
            if(map.containsKey(appNameAsString))
            {
                Gson gson = new Gson();
                String json = pref.getString(appNameAsString, "");
                info = gson.fromJson(json, NotificationInfo.class);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return info;
    }

    public void isAppLunchFirstTime(Context context,String flag ,String appName){
        try {
            SharedPreferences pref = context.getSharedPreferences(APP_INSTALLED, MODE_PRIVATE);
            HashMap<String, String> map = getMap(pref);

            if(!map.containsKey(appName))
            {
                map.put(appName, flag);
            }

            SharedPreferences.Editor editor= pref.edit();

            for (String s : map.keySet()) {
                editor.putString(s, map.get(s));
            }

            editor.apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getIfAppLaunchFirst(Context context,String appName){
        String flag =   NO;
        try {
            SharedPreferences pref = context.getSharedPreferences(APP_INSTALLED, MODE_PRIVATE);
            HashMap<String, String> map = getMap(pref);
            if(map.containsKey(appName))
            {
                flag = pref.getString(appName,NO);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return flag;
    }

    public void saveUserId(Context context,String id ,String appName){
        try {
            SharedPreferences pref = context.getSharedPreferences(USER_ID, MODE_PRIVATE);
            HashMap<String, String> map = getMap(pref);

            if(!map.containsKey(appName))
            {
                map.put(appName, id);
            }

            SharedPreferences.Editor editor= pref.edit();

            for (String s : map.keySet()) {
                editor.putString(s, map.get(s));
            }

            editor.apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getUserId(Context context,String appName){
        String id = "";
        try {
            SharedPreferences pref = context.getSharedPreferences(USER_ID, MODE_PRIVATE);
            HashMap<String, String> map = getMap(pref);
            if(map.containsKey(appName))
            {
                id = pref.getString(appName,"");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return id;
    }

    public void saveSingleAppNameAsInteger(Context context,int appNameAsInteger) {
        SharedPreferences sp = context.getSharedPreferences(APP_NAME_AS_INTEGER, MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(SINGLE_APP_NAME, appNameAsInteger);
        spe.apply();
    }

    public int getSingleAppNameAsInteger(Context context) {
        SharedPreferences sp = context.getSharedPreferences(APP_NAME_AS_INTEGER, MODE_PRIVATE);
        int i = sp.getInt(SINGLE_APP_NAME, 0);
        return i;
    }
}
