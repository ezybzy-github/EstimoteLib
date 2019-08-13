package com.estimotelib;

public interface OnBeaconMessageListener {
    void onMessageReceived(final String key, final String value);
}
