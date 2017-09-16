package com.yehyunryu.android.mywifi2.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;

import com.google.android.gms.location.GeofencingEvent;
import com.yehyunryu.android.mywifi2.R;

/**
 * Copyright 2017 Yehyun Ryu

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    //notification channel id and notificaton id
    private static final String NOTIFICATION_CHANNEL = "geofencing_notifications_channel";
    private static final int WIFI_ENABLED_ID = 600;
    private static final int WIFI_DENIED_ID = 601;

    @Override
    public void onReceive(Context context, Intent intent) {
        //get geofencing event
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.wifi_preference_key), false)) {
            //wifi access enabled

            //turn wifi on
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            //send notification telling wifi was turned on
            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.notifications_preference_key), false)) {
                //builder bitmap for large icon
                Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.wifi_on);

                //build notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.wifi_enabled_notification_title))
                        .setContentText(context.getString(R.string.wifi_enabled_notification_text))
                        .setAutoCancel(true);

                //display notification
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(WIFI_ENABLED_ID, builder.build());
            }
        } else {
            //wifi access denied

            //send notification telling wifi should be turned on
            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.notifications_preference_key), false)) {
                //build bitmap for large icon
                Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.wifi_off);

                //build notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.wifi_denied_notification_title))
                        .setContentText(context.getString(R.string.wifi_denied_notification_text))
                        .setAutoCancel(true);

                //display notification
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(WIFI_DENIED_ID, builder.build());
            }
        }


    }
}
