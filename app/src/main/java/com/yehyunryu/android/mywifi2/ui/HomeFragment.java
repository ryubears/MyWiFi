package com.yehyunryu.android.mywifi2.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yehyunryu.android.mywifi2.R;
import com.yehyunryu.android.mywifi2.service.CountdownTimerService;
import com.yehyunryu.android.mywifi2.utils.Geofencing;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeFragment extends Fragment {
    @BindView(R.id.home_onoff_iv) ImageView mOnOffIV;
    @BindView(R.id.home_onoff_button) Button mOnOffButton;
    @BindView(R.id.home_timer) TextView mTimerTV;

    private static final String LOG_TAG = HomeFragment.class.getSimpleName();

    //geofencing object and state of geofencing
    private Geofencing mGeofencing;
    private boolean mIsGeofencing;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //start timer service

        //initialize shared preferences and its editor
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEditor = mSharedPreferences.edit();

        //check geofencing time and see if it exceeded geofence duration
        long beginTime = mSharedPreferences.getLong(getString(R.string.geofencing_time_key), -1);
        long currentTime = System.currentTimeMillis();
        if(!(beginTime <= 0)) {
            if(currentTime - beginTime > TimeUnit.DAYS.toMillis(1)) {
                mEditor.putBoolean(getString(R.string.geofencing_key), false).apply();
                mEditor.putLong(getString(R.string.geofencing_time_key), -1).apply();
            }
        }

        //get state of geofencing
        mIsGeofencing = mSharedPreferences.getBoolean(getContext().getString(R.string.geofencing_key), false);
        if(mIsGeofencing) {
            getActivity().startService(new Intent(getContext(), CountdownTimerService.class));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
        //inflate and bind views
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootView);

        //get geofencing object from MainActivity
        mGeofencing = ((MainActivity) getActivity()).mGeofencing;

        //set appropriate icon and text for on/off image view and button
        if(mIsGeofencing) {
            //ON

            //update ui to reflect that geofencing is on
            mTimerTV.setVisibility(View.VISIBLE);
            mOnOffIV.setImageResource(R.drawable.place_on_yellow);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mOnOffButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.geofencing_button_on));
            }
            mOnOffButton.setTextColor(ContextCompat.getColor(getContext(), R.color.textSecondary));
            mOnOffButton.setText(getString(R.string.geofencing_on));
        } else {
            //OFF

            //update ui to reflect that geofencing is on
            mTimerTV.setVisibility(View.GONE);
            mOnOffIV.setImageResource(R.drawable.place_off);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mOnOffButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.geofencing_button_off));
            }
            mOnOffButton.setTextColor(ContextCompat.getColor(getContext(), R.color.textPrimary));
            mOnOffButton.setText(getString(R.string.geofencing_off));
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stop timer service
        if(mIsGeofencing) getActivity().stopService(new Intent(getContext(), CountdownTimerService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        //register broadcast receiver for timer service
        getActivity().registerReceiver(mBroadcastReceiver, new IntentFilter(CountdownTimerService.ACTION_COUNTDOWN));
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister broadcast receiver for timer service
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    @OnClick(R.id.home_onoff_button)
    public void onButtonClick() {
        if(mIsGeofencing) {
            //TURNING OFF

            //update ui to reflect that geofencing is off
            mTimerTV.setVisibility(View.GONE);
            mOnOffIV.setImageResource(R.drawable.place_off);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mOnOffButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.geofencing_button_off));
            }
            mOnOffButton.setTextColor(ContextCompat.getColor(getContext(), R.color.textPrimary));
            mOnOffButton.setText(getString(R.string.geofencing_off));

            //stop timer service
            getActivity().stopService(new Intent(getContext(), CountdownTimerService.class));

            //set geofencing to false and reset geofencing time
            mIsGeofencing = false;
            mEditor.putBoolean(getString(R.string.geofencing_key), false).apply();
            mEditor.putLong(getString(R.string.geofencing_time_key), -1).apply();

            //unregister all geofences
            mGeofencing.unregisterAllGeofences();


        } else {
            //TURNING ON

            //update ui to reflect that geofencing is on
            mTimerTV.setVisibility(View.VISIBLE);
            mOnOffIV.setImageResource(R.drawable.place_on_yellow);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mOnOffButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.geofencing_button_on));
            }
            mOnOffButton.setTextColor(ContextCompat.getColor(getContext(), R.color.textSecondary));
            mOnOffButton.setText(getString(R.string.geofencing_on));

            //start timer service
            getActivity().startService(new Intent(getContext(), CountdownTimerService.class));

            //set geofencing to true
            mIsGeofencing = true;
            mEditor.putBoolean(getString(R.string.geofencing_key), true).apply();
            mEditor.putLong(getString(R.string.geofencing_time_key), System.currentTimeMillis()).apply();

            //register all geofences
            mGeofencing.registerAllGeofences();
        }
    }

    //broadcast receiver that listens for timer service ticks
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //update timer text view
            updateTimer(intent);
        }
    };

    //update timer to display correct time left
    private void updateTimer(Intent intent) {
        if(intent.getExtras() != null) {
            long millisUntilFinished = intent.getLongExtra(getString(R.string.countdown_key), -1);
            if(millisUntilFinished != -1) {
                //NORMAL TICKING

                //format time remaining into hh:mm:ss and set timer text
                String formattedTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1));
                mTimerTV.setText(formattedTime);
            } else {
                //TIMER OFF
                
                //update ui to reflect that geofencing is off
                mTimerTV.setVisibility(View.GONE);
                mOnOffIV.setImageResource(R.drawable.place_off);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mOnOffButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.geofencing_button_off));
                }
                mOnOffButton.setTextColor(ContextCompat.getColor(getContext(), R.color.textPrimary));
                mOnOffButton.setText(getString(R.string.geofencing_off));

                //stop timer service
                getActivity().stopService(new Intent(getContext(), CountdownTimerService.class));

                //set geofencing to false and reset geofencing time
                mIsGeofencing = false;
                mEditor.putBoolean(getString(R.string.geofencing_key), false).apply();
                mEditor.putLong(getString(R.string.geofencing_time_key), -1).apply();
            }
        }
    }
}
