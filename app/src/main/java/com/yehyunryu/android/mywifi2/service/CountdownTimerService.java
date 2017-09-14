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
 * Created by Yehyun Ryu on 9/12/2017.
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
