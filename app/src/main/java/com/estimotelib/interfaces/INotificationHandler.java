package com.estimotelib.interfaces;

import com.estimotelib.model.Notification;

public interface INotificationHandler {

    void OnNotificationReceived(final Notification notificationData);

}
