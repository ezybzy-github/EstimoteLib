package com.estimotelib.interfaces;

public interface OnFCMNotificationListener {
    public void onFcmMessageReceived(String title, String message, String image, String appName, String url);
}
