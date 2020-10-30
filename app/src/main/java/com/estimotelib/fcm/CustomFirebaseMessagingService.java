package com.estimotelib.fcm;

import android.content.Context;
import android.util.Log;
import com.estimotelib.FCMNotificationManager;
import com.estimotelib.PreferenceUtil;
import com.estimotelib.controller.PropertyController;
import com.estimotelib.interfaces.ICallbackHandler;
import com.estimotelib.model.NotificationInfo;
import com.estimotelib.model.UpdateUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.json.JSONObject;
import java.util.Map;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessageService";

    PreferenceUtil mPreferenceUtil;
    FCMNotificationManager mFCMNotificationManager;
    private PropertyController mPropertyController;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mPreferenceUtil = new PreferenceUtil();
        mPropertyController = new PropertyController(this);
        mFCMNotificationManager = new FCMNotificationManager(getApplicationContext());
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

    private void processNotification(JSONObject data) {
        String Image = data.optString("image");
        String url = data.optString("url");
        String title   = data.optString("title");
        String msg   = data.optString("message");
        String appNameAsString = data.optString("app_name");
        Log.e("onMessageReceived-->","data: " + data);

        NotificationInfo info = mPreferenceUtil.getNotificationInfo(getApplicationContext(),appNameAsString);
        Log.e("onMessageReceived-->","info: " + new Gson().toJson(info));
        showFCMNotification(getApplicationContext(),title,msg,Image,url,info.getClassReference());
    }

    private void showFCMNotification(Context ctx, String title, String message, String image, String url,
                                    String className){
        if(!image.equalsIgnoreCase("")){
            mFCMNotificationManager.createPictureTypeNotification(ctx,title,message,image,url, className);
        }else{
            mFCMNotificationManager.createNotification(title,ctx,message,url,className);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        String mAppNameAsInteger = String.valueOf(mPreferenceUtil.getSingleAppNameAsInteger(this));
        String userId = mPreferenceUtil.getUserId(this,mAppNameAsInteger);
        if(!userId.isEmpty()){
            SendTokenRefreshRequest(this,userId);
        }
    }

    public void SendTokenRefreshRequest(Context context, String userId) {
        mPropertyController.updateToken(userId, mPreferenceUtil.getFCMToken(context),
                new ICallbackHandler<UpdateUser>() {
                    @Override
                    public void response(UpdateUser response) {
                        Log.e(TAG,"TOKEN_UPDATE: "+new Gson().toJson(response));
                    }

                    @Override
                    public void isError(String errorMsg) {
                        Log.e(TAG,"TOKEN_UPDATE ERROR: "+errorMsg);
                    }
                });
    }
}