package com.estimotelib.fcm;

import android.util.Log;

import com.estimotelib.EstimoteNotificationManager;
import com.estimotelib.PreferenceUtil;
import com.estimotelib.controller.PropertyController;
import com.estimotelib.interfaces.ICallbackHandler;
import com.estimotelib.model.PropertyVisitResponse;
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
        mPropertyController = new PropertyController(getApplicationContext());

        String firebaseId = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG,"FIREBASE_ID: "+firebaseId);

        mPreferenceUtil.saveFCMToken(this,firebaseId);
    }
}