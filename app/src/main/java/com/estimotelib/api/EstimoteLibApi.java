package com.estimotelib.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import static com.estimotelib.api.API.ADD_USER;
import static com.estimotelib.api.API.EXIT_PROPERTY;
import static com.estimotelib.api.API.VISIT_PROPERTY;

public interface EstimoteLibApi {
    @FormUrlEncoded
    @POST(VISIT_PROPERTY)
    Call<ResponseBody> visitProperty(@Field("device_notification_id") String deviceNotificationId,
                                     @Field("property_url") String url, @Field("app_name") String appName,
                                     @Field("device_imei") String deviceImei);

    @FormUrlEncoded
    @POST(EXIT_PROPERTY)
    Call<ResponseBody> exitProperty(@Field("user_id") String userId,
                                    @Field("device_notification_id") String deviceNotificationId,
                                    @Field("app_name") String appName,
                                    @Field("property_url") String url,
                                    @Field("device_imei") String deviceIMEI);

    @FormUrlEncoded
    @POST(ADD_USER)
    Call<ResponseBody> addUser(@Field("user_name") String userName,
                               @Field("device_type") String deviceType,
                               @Field("device_notification_id") String firebaseId,
                               @Field("app_name") String appName,
                               @Field("device_imei") String deviceIMEI);
}
