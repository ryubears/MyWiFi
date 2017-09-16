package com.yehyunryu.android.mywifi2.service;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;

import com.yehyunryu.android.mywifi2.R;

import java.util.concurrent.TimeUnit;

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

//service that updates time left for countdown timer
public class CountdownTimerService extends Service {
    private static final String LOG_TAG = CountdownTimerService.class.getSimpleName();

    //for broadcast receiver filter
    public static final String ACTION_COUNTDOWN = "com.yehyunryu.android.countdownbroadcast";

    private static final long COUNTDOWN_DURATION = TimeUnit.DAYS.toMillis(1);
    private static final long COUNTDOWN_INTERVAL = 1000;

    //broadcast intent to notify broadcast receiver
    private Intent mBroadcastIntent = new Intent(ACTION_COUNTDOWN);

    //timer
    private CountDownTimer mCountdownTimer;

    @Override
    public void onCreate() {
        super.onCreate();

        //get geofence begin time and current time
        long durationTime;
        long beginTime = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getLong(getString(R.string.geofencing_time_key), -1);
        long currentTime = System.currentTimeMillis();

        if(beginTime <= 0) {
            //if geofence has not been turned on or has been turned off
            durationTime = COUNTDOWN_DURATION;
        } else if(currentTime - beginTime >= TimeUnit.DAYS.toMillis(1)) {
            //if geofence exceeded geofence duration
            durationTime = COUNTDOWN_DURATION;
        } else {
            //if it has been less than 24 hours since geofence has been enabled
            durationTime = COUNTDOWN_DURATION - (currentTime - beginTime);
        }

        mCountdownTimer = new CountDownTimer(durationTime, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                //send tick broadcast
                mBroadcastIntent.putExtra(getString(R.string.countdown_key), millisUntilFinished);
                sendBroadcast(mBroadcastIntent);
            }

            @Override
            public void onFinish() {
                //send broadcast indicating timer is finished
                mBroadcastIntent.putExtra(getString(R.string.countdown_key), Long.valueOf(-1));
                sendBroadcast(mBroadcastIntent);
            }
        };

        //start timer
        mCountdownTimer.start();
    }

    @Override
    public void onDestroy() {
        //stop timer
        mCountdownTimer.cancel();
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
