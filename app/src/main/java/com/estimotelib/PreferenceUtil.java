package com.estimotelib;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.estimotelib.model.NotificationInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PreferenceUtil {

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
            SharedPreferences pref= context.getSharedPreferences("STORED_MUTED_URLS", Context.MODE_PRIVATE);
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
        SharedPreferences pref= context.getSharedPreferences("STORED_MUTED_URLS", Context.MODE_PRIVATE);
        HashMap<String, String> map= getMap(pref);

        if(map.containsKey(value))
        {
            urlMatch =  true;
        }
        return urlMatch;
    }

    //Delete from preference
    public void deleteFromMutedUrl(Context context, String value){
        SharedPreferences pref= context.getSharedPreferences("STORED_MUTED_URLS", Context.MODE_PRIVATE);
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
            SharedPreferences pref= context.getSharedPreferences("STORED_MUTED_URLS", Context.MODE_PRIVATE);
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
            SharedPreferences pref= context.getSharedPreferences("VISITED_BEACON", Context.MODE_PRIVATE);
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
            SharedPreferences pref= context.getSharedPreferences("VISITED_BEACON", Context.MODE_PRIVATE);
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

    public void deleteFromVisitedBeacon(Context context, String value){
        SharedPreferences pref= context.getSharedPreferences("VISITED_BEACON", Context.MODE_PRIVATE);
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
        SharedPreferences pref= context.getSharedPreferences("ENTERED_BEACON", Context.MODE_PRIVATE);
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
        SharedPreferences pref= context.getSharedPreferences("EXIT_BEACON", Context.MODE_PRIVATE);
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
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void SaveIMEINumber(Context context,String imeinumber){
        SharedPreferences sp = context.getSharedPreferences("IMEI_NUMBER", Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("IMEINumber",imeinumber);
        spe.apply();
        spe.commit();
    }

    public String getIMEINumber(Context context){
        SharedPreferences sp = context.getSharedPreferences("IMEI_NUMBER", Context.MODE_PRIVATE);
        return sp.getString("IMEINumber","");
    }

    public void saveFCMToken(Context context, String token){
        SharedPreferences sp = context.getSharedPreferences("FCM_TOKEN", Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("TOKEN",token);
        spe.apply();
        spe.commit();
    }

    public String getFCMToken(Context context){
        SharedPreferences sp = context.getSharedPreferences("FCM_TOKEN", Context.MODE_PRIVATE);
        return sp.getString("TOKEN","");
    }

    public void saveUserId(Context context, String UserId){
        SharedPreferences sp = context.getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("UserId",UserId);
        spe.apply();
        spe.commit();
    }

    public String getUserId(Context context){
        SharedPreferences sp = context.getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        return sp.getString("UserId","");
    }

    public void SaveNotificationInfo(Context context, String appNameAsString, NotificationInfo info){
        try{
            Gson gson = new Gson();
            String json = gson.toJson(info);

            SharedPreferences pref = context.getSharedPreferences("NOTIFICATION_INFO", Context.MODE_PRIVATE);
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
            SharedPreferences pref = context.getSharedPreferences("NOTIFICATION_INFO", Context.MODE_PRIVATE);
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
            SharedPreferences pref = context.getSharedPreferences("APP_INSTALLED", Context.MODE_PRIVATE);
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
        String flag = "no";
        try {
            SharedPreferences pref = context.getSharedPreferences("APP_INSTALLED", Context.MODE_PRIVATE);
            HashMap<String, String> map = getMap(pref);
            if(map.containsKey(appName))
            {
                flag = pref.getString(appName,"no");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return flag;
    }

    public void saveUserId(Context context,String id ,int appName){
        try {
            SharedPreferences pref = context.getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
            HashMap<String, String> map = getMap(pref);

            if(!map.containsKey(String.valueOf(appName)))
            {
                map.put(String.valueOf(appName), id);
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

    public String getUserId(Context context,int appName){
        String id = "";
        try {
            SharedPreferences pref = context.getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
            HashMap<String, String> map = getMap(pref);
            if(map.containsKey(String.valueOf(appName)))
            {
                id = pref.getString(String.valueOf(appName),"no");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return id;
    }
}
