package com.yehyunryu.android.mywifi2.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;
import com.yehyunryu.android.mywifi2.R;

/**
 * Created by Yehyun Ryu on 9/11/2017.
 */

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive called");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.wifi_preference_key), false)) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        }

        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.notifications_preference_key), false)) {
            int notificationId = 1000;
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("WiFi Turned On")
                    .setContentText("MyWiFi turned on your wifi.");

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
