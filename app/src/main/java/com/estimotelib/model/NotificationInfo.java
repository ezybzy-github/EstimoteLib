package com.estimotelib.model;

public class NotificationInfo {
    private int appNameAsInt;
    private String appNameAsString;
    private String classReference;

    public int getAppNameAsInt() {
        return appNameAsInt;
    }

    public void setAppNameAsInt(int appNameAsInt) {
        this.appNameAsInt = appNameAsInt;
    }

    public String getAppNameAsString() {
        return appNameAsString;
    }

    public void setAppNameAsString(String appNameAsString) {
        this.appNameAsString = appNameAsString;
    }

    public String getClassReference() {
        return classReference;
    }

    public void setClassReference(String classReference) {
        this.classReference = classReference;
    }
}
