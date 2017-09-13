package com.yehyunryu.android.mywifi2.service;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Yehyun Ryu on 9/12/2017.
 */

public class CountdownTimerService extends Service {
    private static final String LOG_TAG = CountdownTimerService.class.getSimpleName();
    public static final String ACTION_COUNTDOWN = "com.yehyunryu.android.countdownbroadcast";
    private Intent mBroadcastIntent = new Intent(ACTION_COUNTDOWN);
    private CountDownTimer mCountdownTimer;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "Starting timer...");

        mCountdownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(LOG_TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                mBroadcastIntent.putExtra("countdown", millisUntilFinished);
                sendBroadcast(mBroadcastIntent);
            }

            @Override
            public void onFinish() {
                Log.d(LOG_TAG, "Timer finished");
            }
        };

        mCountdownTimer.start();
    }

    @Override
    public void onDestroy() {
        mCountdownTimer.cancel();
        Log.d(LOG_TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
