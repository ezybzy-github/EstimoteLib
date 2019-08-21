package com.estimotelib.controller;

import android.content.Context;
import com.estimotelib.interfaces.ICallbackHandler;
import com.estimotelib.model.AddUserResponse;
import com.estimotelib.model.PropertyExitResponse;
import com.estimotelib.model.PropertyVisitResponse;
import com.estimotelib.module.PropertyModule;


public class PropertyController extends BaseController {
    PropertyModule mPropertyModule;
    private Context mContext;

    public PropertyController(Context context) {
        if (mPropertyModule == null) {
            mContext = context;
            mPropertyModule = new PropertyModule(context);
        }
    }

    public void visitProperty(String fcmToken,String propertyUrl,String appName,String imeiNumber,
                              final ICallbackHandler iCallbackHandler) {
        mPropertyModule.visitProperty(fcmToken,propertyUrl,appName,imeiNumber,PropertyVisitResponse.class,
                new ICallbackHandler<PropertyVisitResponse>() {
                    @Override
                    public void response(PropertyVisitResponse responseContest) {
                        iCallbackHandler.response(responseContest);
                    }

                    @Override
                    public void isError(String errorMsg) {
                        iCallbackHandler.isError(errorMsg);
                    }
                });
    }

    public void exitProperty(String userId,String propertyUrl,String firebaseId,String appName,String deviceIMEI,
                              final ICallbackHandler iCallbackHandler) {
        mPropertyModule.exitProperty(userId,propertyUrl,firebaseId,appName,deviceIMEI,PropertyExitResponse.class,
                new ICallbackHandler<PropertyExitResponse>() {
                    @Override
                    public void response(PropertyExitResponse responseContest) {
                        iCallbackHandler.response(responseContest);
                    }

                    @Override
                    public void isError(String errorMsg) {
                        iCallbackHandler.isError(errorMsg);
                    }
                });
    }

    public void addUser(String userName,String deviceType,String firebaseId
            ,String appName,String deviceIMEI,final ICallbackHandler iCallbackHandler) {
        mPropertyModule.addUser(userName,deviceType,firebaseId,appName,deviceIMEI,AddUserResponse.class,
                new ICallbackHandler<AddUserResponse>() {
                    @Override
                    public void response(AddUserResponse responseContest) {
                        iCallbackHandler.response(responseContest);
                    }

                    @Override
                    public void isError(String errorMsg) {
                        iCallbackHandler.isError(errorMsg);
                    }
                });
    }
}
