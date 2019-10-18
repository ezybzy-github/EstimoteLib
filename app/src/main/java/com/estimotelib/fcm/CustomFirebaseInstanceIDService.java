package com.estimotelib.fcm;

import android.util.Log;

import com.estimotelib.EstimoteNotificationManager;
import com.estimotelib.PreferenceUtil;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class CustomFirebaseInstanceIDService extends FirebaseInstanceIdService
{
    private static final String TAG = CustomFirebaseInstanceIDService.class.getSimpleName();

    PreferenceUtil mPreferenceUtil;

    @Override
    public void onTokenRefresh() {
        mPreferenceUtil = new PreferenceUtil();

        String firebaseId = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG,"FIREBASE_ID: "+firebaseId);
        mPreferenceUtil.saveFCMToken(this,firebaseId);
    }
}