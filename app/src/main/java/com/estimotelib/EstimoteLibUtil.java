package com.estimotelib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimotelib.interfaces.OnBeaconMessageListener;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class EstimoteLibUtil {
    private static String TAG = "EstimoteLibUtil";

    public static EstimoteCloudCredentials cloudCredentials;

    private EstimoteNotificationManager mNm;
    private boolean mIsMonitoringOn = false;

    private NotificationManager notificationManager;

    public static FCMNotificationManager mFCMNotificationManager;

    private PreferenceUtil mPreferenceUtil;

    public EstimoteLibUtil(String appId, String appToken, Context applicationContext) {

        Log.e(TAG,"ID: "+appId);
        Log.e(TAG,"TOKEN: "+appToken);
        cloudCredentials = new EstimoteCloudCredentials( appId, appToken);
        notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mPreferenceUtil = new PreferenceUtil();
        mFCMNotificationManager = new FCMNotificationManager(applicationContext);
    }

    public EstimoteLibUtil(){

    }

    public void enableBeaconsNotification(Activity mContext, final Class classRef, boolean flag, String appName) {

        if(!mIsMonitoringOn) {
            mIsMonitoringOn = true;
            mNm = new EstimoteNotificationManager(mContext);
            mNm.startMonitoring(mContext,classRef,flag,appName);
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

    public void setClassReferenceForNotification(Context ctx,Class reference,String AppName){
        mPreferenceUtil.saveApplicationName(ctx,AppName);
        mPreferenceUtil.SaveClassReferenceForNotification(ctx,AppName,reference);

        Log.e(TAG,"className: "+mPreferenceUtil.getClassReferenceName(ctx,AppName));
        Log.e(TAG,"appName: "+mPreferenceUtil.getApplicationName(ctx));
    }

    public String returnClassReferenceForNotification(Context ctx,String AppName){
        return mPreferenceUtil.getClassReferenceName(ctx,AppName);
    }

    public String returnAppNameForNotification(Context ctx){
        return mPreferenceUtil.getApplicationName(ctx);
    }

    public void showFCMNotification(Context ctx,String title, String message, String image, String appName, String url,
                                    String className){
        if(!image.equalsIgnoreCase("")){
            mFCMNotificationManager.
                    createPictureTypeNotification(ctx,title,message,image,appName,url, className);
        }else{
            mFCMNotificationManager.createNotification(title,ctx,message,url,appName,className);
        }
    }

    public void startMonitoring(final Activity context, final Class classRef,
                                final boolean flag, final String appName) {

        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(context,
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                Log.d("app", "requirements fulfilled");
                                enableBeaconsNotification(context,classRef,flag,appName);
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
                mPreferenceUtil.storeMutedUrl(mContext,value);
            }
        });

        builder.show();
    }
}
