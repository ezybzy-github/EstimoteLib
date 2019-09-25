package com.estimotelib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimotelib.interfaces.FcmNotificationListener;
import com.estimotelib.interfaces.OnBeaconMessageListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class EstimoteLibUtil {
    private static String TAG = "EstimoteLibUtil";

    public static EstimoteCloudCredentials cloudCredentials;

    private EstimoteNotificationManager mNm;
    private boolean mIsMonitoringOn = false;

    public FcmNotificationListener mNotificationListener;

    public NotificationManager notificationManager;

    public EstimoteLibUtil(String appId, String appToken, Context applicationContext) {

        Log.e(TAG,"ID: "+appId);
        Log.e(TAG,"TOKEN: "+appToken);
        cloudCredentials = new EstimoteCloudCredentials( appId, appToken);
        notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public EstimoteLibUtil(){

    }

    public void enableBeaconsNotification(Activity mContext,final Class classRef, boolean flag,String appName) {

        if(!mIsMonitoringOn) {
            mIsMonitoringOn = true;
            mNm = new EstimoteNotificationManager(mContext);
            mNm.startMonitoring(mContext,classRef,flag,appName);
        }
    }

    public void setBeaconMessageListener(OnBeaconMessageListener listener) {
        if(mNm != null) {
            mNm.setBeaconMessageListener(listener);
        }
    }

    public void removeBeaconMessageListener() {
        if(mNm != null) {
            mNm.removeBeaconMessageListener();
        }
    }

    public void setFcmNotificationListener(FcmNotificationListener listener){
        mNotificationListener = listener;
    }

    public void removeFcmNotificationListener(){
        mNotificationListener = null;
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
                mNm.storeMutedUrl(mContext,value);
            }
        });

        builder.show();
    }

    public void createNotification(String title, Context context, String msg, String url, String appName,Class classRef) {

        final int NOTIFY_ID = 0; // ID of notification
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;
        if (notificationManager == null) {
            notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = notificationManager.getNotificationChannel("content_channel");
            if (mChannel == null) {
                mChannel = new NotificationChannel("content_channel", "Things near you", importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, "content_channel");

            Intent intent = new Intent(context,classRef);
            intent.putExtra("WEB_VIEW_URL", url);
            intent.setAction(appName);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            builder.setContentTitle(appName)                            // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                    .setContentText(msg) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(title)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        }
        else {
            builder = new NotificationCompat.Builder(context, "content_channel");

            Intent intent = new Intent(context,classRef);
            intent.putExtra("WEB_VIEW_URL", url);
            intent.setAction(appName);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            builder.setContentTitle(appName)                            // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                    .setContentText(msg) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(title)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        Notification notification = builder.build();
        notificationManager.notify(NOTIFY_ID, notification);
    }

    public void createPictureTypeNotification(Context context, String title, String message, String imageUrl,
                                              String appName,String url,Class classRef){
        new generatePictureStyleNotification(context,title, message,
                imageUrl,appName,url,classRef).execute();
    }

    public class generatePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private String title, message, imageUrl,appName,url;
        private Class classRef;

        public generatePictureStyleNotification(Context context, String title, String message,
                                                String imageUrl,String appName,String url,Class classRef) {
            super();
            this.mContext = context;
            this.title = title;
            this.message = message;
            this.imageUrl = imageUrl;
            this.appName = appName;
            this.url = url;
            this.classRef = classRef;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
            try {
                URL url = new URL(this.imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            final int NOTIFY_ID = 0; // ID of notification
            PendingIntent pendingIntent;
            NotificationCompat.Builder builder;
            if (notificationManager == null) {
                notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel mChannel = notificationManager.getNotificationChannel("content_channel");
                if (mChannel == null) {
                    mChannel = new NotificationChannel("content_channel", "Things near you", importance);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notificationManager.createNotificationChannel(mChannel);
                }
                builder = new NotificationCompat.Builder(mContext, "content_channel");

                Intent intent = new Intent(mContext,classRef);
                intent.putExtra("WEB_VIEW_URL", url);
                intent.setAction(appName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                builder.setContentTitle(appName)                            // required
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                        .setContentText(message) // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setTicker(title)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setLargeIcon(result)
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(result));
            }
            else {
                builder = new NotificationCompat.Builder(mContext, "content_channel");

                Intent intent = new Intent(mContext,classRef);
                intent.putExtra("WEB_VIEW_URL", url);
                intent.setAction(appName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                builder.setContentTitle(appName)                            // required
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                        .setContentText(message) // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setTicker(title)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setLargeIcon(result)
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(result));
            }
            Notification notification = builder.build();
            notificationManager.notify(NOTIFY_ID, notification);
        }
    }
}
