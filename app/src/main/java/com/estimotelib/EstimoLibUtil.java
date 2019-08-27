package com.estimotelib;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimotelib.interfaces.NotificationListener;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class EstimoLibUtil {
    private static String TAG = "EstimoLibUtil";

    EstimoteCloudCredentials cloudCredentials;

    private EstimoNotificationsManager mNm;
    private boolean mIsMonitoringOn = false;

    private Context mContext;

    public static NotificationListener mListener;

    private static int mNotificationIcon;

    public EstimoLibUtil(String appId, String appToken, Context applicationContext) {

        Log.e(TAG,"ID: "+appId);
        Log.e(TAG,"TOKEN: "+appToken);
        mContext = applicationContext;
        cloudCredentials = new EstimoteCloudCredentials( appId, appToken);
    }

    public EstimoLibUtil(){

    }

    /*public static int notificationIcon(){
        return mNotificationIcon;
    }*/

    public void enableBeaconsNotification(int notifIcon, final int mute, final Class classRef, final Class receiver,boolean flag,
                                          String appName) {

        if(!mIsMonitoringOn) {
            mIsMonitoringOn = true;
            mNm = new EstimoNotificationsManager(mContext);
            mNm.startMonitoring(notifIcon,mute,classRef,receiver,flag,appName);
            mNotificationIcon = notifIcon;
        }
    }

    public void setBeaconMessageListener(OnBeaconMessageListener listener) {
        if(mNm != null) {
            mNm.setBeaconMessageListener(listener);
        }
    }

    public void removeBeaconMessageListener() {
        if(mNm != null) {
            mNm.removeBeaconMessageListener();
        }
    }

    public void startMonitoring(Activity context, final int notifIcon, final int muteIcon, final Class classRef,
                                final Class receiver, final boolean flag,final String appName) {
        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(context,
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                Log.d("app", "requirements fulfilled");
                                enableBeaconsNotification(notifIcon,muteIcon,classRef,receiver,flag,appName);
                                return null;
                            }
                        },
                        new Function1<List<? extends Requirement>, Unit>() {
                            @Override
                            public Unit invoke(List<? extends Requirement> requirements) {
                                Log.e("app", "requirements missing: " + requirements);
                                return null;
                            }
                        },
                        new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Log.e("app", "requirements error: " + throwable);
                                return null;
                            }
                        });

    }

    public static void bindListener(NotificationListener listener) {
        mListener = listener;
    }
}
