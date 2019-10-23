package com.estimotelib;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimotelib.controller.PropertyController;
import com.estimotelib.interfaces.ICallbackHandler;
import com.estimotelib.interfaces.INotificationHandler;
import com.estimotelib.model.AddUserResponse;
import com.estimotelib.model.NotificationInfo;
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
        cloudCredentials = new EstimoteCloudCredentials( appId, appToken);

        mPreferenceUtil = new PreferenceUtil();
        mPropertyController = new PropertyController(applicationContext);
    }

    public void enableBeaconsNotification(Activity mContext, boolean flag,final INotificationHandler notificationHandler,
                                          String appName) {

        if(!mIsMonitoringOn) {
            mIsMonitoringOn = true;
            mNm = new EstimoteNotificationManager(mContext, notificationHandler);
            mNm.startMonitoring(mContext,flag,appName);
        }
    }

    public void setNotificationInfo(Context ctx,Class reference,int AppNameAsInteger,String appNameAsString){
        NotificationInfo info = new NotificationInfo();
        info.setAppNameAsInt(AppNameAsInteger);
        info.setAppNameAsString(appNameAsString);
        info.setClassReference(reference.getName());
        mPreferenceUtil.SaveNotificationInfo(ctx,appNameAsString,info);
    }

    public void startMonitoring(final Activity context, final boolean flag,
                                final INotificationHandler notificationHandler, final String AppName) {

        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(context,
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                Log.d("app", "requirements fulfilled");
                                enableBeaconsNotification(context,flag, notificationHandler,AppName);
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

    public void sendAddUserRequest(final Context context, String userName, final int appName){

        mPropertyController.addUser(userName,1,mPreferenceUtil.getFCMToken(context),appName,
                mPreferenceUtil.getIMEINumber(context),new ICallbackHandler<AddUserResponse>() {
                    @Override
                    public void response(AddUserResponse response) {
                        Log.e(TAG,"ADD_USER: "+new Gson().toJson(response));

                        int id = response.getId();
                        mPreferenceUtil.saveUserId(context,String.valueOf(id),String.valueOf(appName));
                        mPreferenceUtil.isAppLunchFirstTime(context,"yes",String.valueOf(appName));
                    }

                    @Override
                    public void isError(String errorMsg) {
                        Log.e(TAG,"ADD_USER ERROR: "+errorMsg);
                    }
                });
    }

    public void SendTokenRefreshRequest(Context context,String userId) {

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
