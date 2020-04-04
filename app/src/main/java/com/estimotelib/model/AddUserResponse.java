package com.estimotelib.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddUserResponse {
    @SerializedName("user_name")
    @Expose
    private Object userName;
    @SerializedName("device_type")
    @Expose
    private String deviceType;
    @SerializedName("device_notification_id")
    @Expose
    private String deviceNotificationId;
    @SerializedName("app_name")
    @Expose
    private String appName;
    @SerializedName("device_imei")
    @Expose
    private String deviceImei;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("id")
    @Expose
    private Integer id;

    public Object getUserName() {
        return userName;
    }

    public void setUserName(Object userName) {
        this.userName = userName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceNotificationId() {
        return deviceNotificationId;
    }

    public void setDeviceNotificationId(String deviceNotificationId) {
        this.deviceNotificationId = deviceNotificationId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDeviceImei() {
        return deviceImei;
    }

    public void setDeviceImei(String deviceImei) {
        this.deviceImei = deviceImei;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
