package com.estimotelib;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimotelib.controller.PropertyController;
import com.estimotelib.interfaces.ICallbackHandler;
import com.estimotelib.model.AddUserResponse;
import com.estimotelib.model.PropertyVisitResponse;
import com.estimotelib.model.UpdateUser;
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

    public void enableBeaconsNotification(Activity mContext, boolean flag) {

        if(!mIsMonitoringOn) {
            mIsMonitoringOn = true;
            mNm.startMonitoring(mContext,flag);
        }
    }

    public void setClassReferenceForNotification(Context ctx,Class reference,int AppName){
        mPreferenceUtil.saveApplicationName(ctx,AppName);
        mPreferenceUtil.SaveClassReferenceForNotification(ctx,AppName,reference);
    }

    public void startMonitoring(final Activity context, final boolean flag) {

        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(context,
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                Log.d("app", "requirements fulfilled");
                                enableBeaconsNotification(context,flag);
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

    public void sendAddUserRequest(final Context context, String userName, int appName){
        Log.e(TAG,"mUserName: "+userName);
        Log.e(TAG,"getFCMToken(): "+mPreferenceUtil.getFCMToken(context));
        Log.e(TAG,"mAppName: "+appName);
        Log.e(TAG,"mIMEINumber: "+mPreferenceUtil.getIMEINumber(context));

        mPropertyController.addUser(userName,1,mPreferenceUtil.getFCMToken(context),appName,
                mPreferenceUtil.getIMEINumber(context),new ICallbackHandler<AddUserResponse>() {
                    @Override
                    public void response(AddUserResponse response) {
                        mPreferenceUtil.saveIdFromServer(context, String.valueOf(response.getId()));
                        Log.e(TAG,"ADD_USER: "+new Gson().toJson(response));

                        if(response.getId() != null){
                            SendTokenRefreshRequest(context,String.valueOf(response.getId()));
                        }
                    }

                    @Override
                    public void isError(String errorMsg) {
                        Log.e(TAG,"ADD_USER ERROR: "+errorMsg);
                    }
                });
    }

    private void SendTokenRefreshRequest(Context context,String userId) {
        Log.e(TAG,"TOKEN_UPDATE: ");
        Log.e(TAG,"fireBaseId: "+mPreferenceUtil.getFCMToken(context));
        Log.e(TAG,"getUserId: "+userId);

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
