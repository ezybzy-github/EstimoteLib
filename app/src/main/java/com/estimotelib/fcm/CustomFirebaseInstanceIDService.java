package com.estimotelib.fcm;

import android.content.Context;
import android.util.Log;

import com.estimotelib.EstimoteNotificationManager;
import com.estimotelib.PreferenceUtil;
import com.estimotelib.controller.PropertyController;
import com.estimotelib.interfaces.ICallbackHandler;
import com.estimotelib.model.PropertyVisitResponse;
import com.estimotelib.model.UpdateUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.Gson;

public class CustomFirebaseInstanceIDService extends FirebaseInstanceIdService
{
    private static final String TAG = CustomFirebaseInstanceIDService.class.getSimpleName();

    PreferenceUtil mPreferenceUtil;

    private PropertyController mPropertyController;

    @Override
    public void onTokenRefresh() {
        mPreferenceUtil = new PreferenceUtil();
        mPropertyController = new PropertyController(this);

        String firebaseId = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG,"FIREBASE_ID: "+firebaseId);

        mPreferenceUtil.saveFCMToken(this,firebaseId);

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