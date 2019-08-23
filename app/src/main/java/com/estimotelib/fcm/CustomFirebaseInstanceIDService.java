package com.estimotelib.fcm;

import android.util.Log;

import com.estimotelib.EstimoNotificationsManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class CustomFirebaseInstanceIDService extends FirebaseInstanceIdService
{
    private static final String TAG = CustomFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        String firebaseId = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG,"FIREBASE_ID: "+firebaseId);
        EstimoNotificationsManager.saveFCMToken(firebaseId);
    }
}