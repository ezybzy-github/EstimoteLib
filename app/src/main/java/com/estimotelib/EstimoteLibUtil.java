package com.estimotelib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimotelib.controller.PropertyController;
import com.estimotelib.interfaces.ICallbackHandler;
import com.estimotelib.interfaces.OnBeaconMessageListener;
import com.estimotelib.model.AddUserResponse;
import com.google.gson.Gson;

import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class EstimoteLibUtil {
    private static String TAG = "EstimoteLibUtil";

    public static EstimoteCloudCredentials cloudCredentials;

    private EstimoteNotificationManager mNm;
    private boolean mIsMonitoringOn = false;

    private PreferenceUtil mPreferenceUtil;

    private PropertyController mPropertyController;

    public EstimoteLibUtil(String appId, String appToken, Context applicationContext) {

        Log.e(TAG,"ID: "+appId);
        Log.e(TAG,"TOKEN: "+appToken);
        cloudCredentials = new EstimoteCloudCredentials( appId, appToken);

        mPreferenceUtil = new PreferenceUtil();
        mPropertyController = new PropertyController(applicationContext);

        mNm = new EstimoteNotificationManager(applicationContext);
    }

    public void enableBeaconsNotification(Activity mContext, final Class classRef, boolean flag, String appName) {

        if(!mIsMonitoringOn) {
            mIsMonitoringOn = true;
            mNm.startMonitoring(mContext,classRef,flag,appName);
        }
    }

    public void setBeaconMessageListener(OnBeaconMessageListener listener) {
        if(mNm != null) {
            Log.e(TAG,"EstimoteNotificationManager not null");
            mNm.setBeaconMessageListener(listener);
        }else{
            Log.e(TAG,"EstimoteNotificationManager is null");
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

    public void sendAddUserRequest(Context context, String userName, String appName){
        Log.e(TAG,"mUserName: "+userName);
        Log.e(TAG,"getFCMToken(): "+mPreferenceUtil.getFCMToken(context));
        Log.e(TAG,"mAppName: "+appName);
        Log.e(TAG,"mIMEINumber: "+mPreferenceUtil.mIMEINumber);

        mPropertyController.addUser(userName,"Android",mPreferenceUtil.getFCMToken(context),appName,
                mPreferenceUtil.mIMEINumber,new ICallbackHandler<AddUserResponse>() {
                    @Override
                    public void response(AddUserResponse response) {
                        Log.e(TAG,"ADD_USER: "+new Gson().toJson(response));
                    }

                    @Override
                    public void isError(String errorMsg) {

                    }
                });
    }
}
