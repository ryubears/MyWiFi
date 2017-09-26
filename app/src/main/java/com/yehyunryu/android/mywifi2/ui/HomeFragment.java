package com.yehyunryu.android.mywifi2.ui;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.yehyunryu.android.mywifi2.R;
import com.yehyunryu.android.mywifi2.service.CountdownTimerService;
import com.yehyunryu.android.mywifi2.utils.Geofencing;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.yehyunryu.android.mywifi2.data.PlacesContract.PlacesEntry;

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

public class HomeFragment extends Fragment {
    @BindView(R.id.home_off_iv) ImageView mOffIV;
    @BindView(R.id.home_on_iv) ImageView mOnIV;
    @BindView(R.id.home_onoff_button) Button mOnOffButton;
    @BindView(R.id.home_timer) TextView mTimerTV;
    @BindView(R.id.home_banner_ad) AdView mBannerAd;

    private static final String LOG_TAG = HomeFragment.class.getSimpleName();
    private static final boolean ON_START = true;
    private static final boolean NOT_ON_START = false;

    //geofencing object and state of geofencing
    private GoogleApiClient mGoogleApiClient;
    private Geofencing mGeofencing;
    private boolean mIsGeofencing;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private Toast mToast;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            //start timer service
            getActivity().startService(new Intent(getContext(), CountdownTimerService.class));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate and bind views
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootView);

        //get google api client and geofencing object from MainActivity
        mGoogleApiClient = ((MainActivity) getActivity()).mGoogleApiClient;
        mGeofencing = ((MainActivity) getActivity()).mGeofencing;

        //get toast from main activity
        mToast = ((MainActivity) getActivity()).mToast;

        //set appropriate icon and text for on/off image view and button
        if(mIsGeofencing) {
            //ON

            //update views to show current state
            showGeofencingOn(ON_START);
        } else {
            //OFF

            //update views to show current state
            showGeofencingOff(ON_START);
        }

        mBannerAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.e("Ads", "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.e("Ads", "onAdFailedToLoad: " + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.e("Ads", "onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.e("Ads", "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                Log.e("Ads", "onAdClosed");
            }
        });

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("6254ECF70C924212D6323B9DE3BF409F")
                .build();
        mBannerAd.loadAd(adRequest);

        return rootView;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stop timer service
        if(mIsGeofencing) getActivity().stopService(new Intent(getContext(), CountdownTimerService.class));
    }

    @OnClick(R.id.home_onoff_button)
    public void onButtonClick() {
        if(mIsGeofencing) {
            //TURNING OFF

            //update views to show current state
            showGeofencingOff(NOT_ON_START);

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

            //query places saved in database
            Cursor cursor = getContext().getContentResolver().query(
                    PlacesEntry.PLACES_CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );

            if(cursor == null || cursor.getCount() == 0) {
                //NO PLACES STORED

                //show toast if there are no place to register
                if(mToast != null) mToast.cancel();
                mToast = Toast.makeText(getContext(), getString(R.string.need_places_toast), Toast.LENGTH_SHORT);
                mToast.show();
            } else {
                //AT LEAST ONE PLACE STORED

                //update views to represent that geofences are on
                showGeofencingOn(NOT_ON_START);

                //start timer service
                getActivity().startService(new Intent(getContext(), CountdownTimerService.class));

                //store place id in a array list
                List<String> places = new ArrayList<>();
                while(cursor.moveToNext()) {
                    places.add(cursor.getString(cursor.getColumnIndex(PlacesEntry.COLUMN_PLACE_ID)));
                }

                //store GeoData in a PlaceBuffer using place id list
                final PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, places.toArray(new String[places.size()]));
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        //set geofencing to true
                        mIsGeofencing = true;
                        mEditor.putBoolean(getString(R.string.geofencing_key), true).apply();
                        mEditor.putLong(getString(R.string.geofencing_time_key), System.currentTimeMillis()).apply();

                        //update geofence and register geofences
                        mGeofencing.updateGeofencesList(places);
                        mGeofencing.registerAllGeofences();
                    }
                });
            }
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
                //TIMER ONGOING

                //format time remaining into hh:mm:ss and set timer text
                String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1));
                mTimerTV.setText(formattedTime);
            } else {
                //TIMER FINISHED

                //update views to show current state
                showGeofencingOff(NOT_ON_START);

                //stop timer service
                getActivity().stopService(new Intent(getContext(), CountdownTimerService.class));

                //set geofencing to false and reset geofencing time
                mIsGeofencing = false;
                mEditor.putBoolean(getString(R.string.geofencing_key), false).apply();
                mEditor.putLong(getString(R.string.geofencing_time_key), -1).apply();
            }
        }
    }

    //update views to indicate that geofencing is ON
    private void showGeofencingOn(boolean isStart) {
        if(isStart) {
            //update views to show that geofencing is on
            mTimerTV.animate()
                    .alpha(1.0f)
                    .setDuration(0)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            mOffIV.animate()
                                    .alpha(0f)
                                    .translationY(-80)
                                    .setDuration(0);
                            mOnIV.animate()
                                    .alpha(1f)
                                    .translationY(-80)
                                    .setDuration(0);
                            mOnOffButton.animate()
                                    .translationY(80)
                                    .setDuration(0);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            mTimerTV.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                            //nothing
                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {
                            //nothing
                        }
                    });
        } else {
            //fade in timer and move icon and button apart
            mTimerTV.animate()
                    .alpha(1.0f)
                    .setDuration(400)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            mOffIV.animate()
                                    .alpha(0f)
                                    .translationY(-80)
                                    .setDuration(500);
                            mOnIV.animate()
                                    .alpha(1f)
                                    .translationY(-80)
                                    .setDuration(500);
                            mOnOffButton.animate()
                                    .translationY(80)
                                    .setDuration(500);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            mTimerTV.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                            //nothing
                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {
                            //nothing
                        }
                    });
        }

        //update ui to reflect that geofencing is on
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mOnOffButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.geofencing_button_on));
        }
        mOnOffButton.setTextColor(ContextCompat.getColor(getContext(), R.color.textSecondary));
        mOnOffButton.setText(getString(R.string.geofencing_on));
    }

    //update views to indicate that geofencing is OFF
    private void showGeofencingOff(boolean isStart) {
        if(isStart) {
            //update views to show that geofencing is off
            mTimerTV.animate()
                    .alpha(0f)
                    .setDuration(0)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            mOffIV.animate()
                                    .alpha(1f)
                                    .translationY(0)
                                    .setDuration(0);
                            mOnIV.animate()
                                    .alpha(0f)
                                    .translationY(0)
                                    .setDuration(0);
                            mOnOffButton.animate()
                                    .translationY(0)
                                    .setDuration(0);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            mTimerTV.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                            //nothing
                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {
                            //nothing
                        }
                    });
        } else {
            //fade out timer and move icon and button together
            mTimerTV.animate()
                    .alpha(0f)
                    .setDuration(400)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            mOffIV.animate()
                                    .alpha(1f)
                                    .translationY(0)
                                    .setDuration(500);
                            mOnIV.animate()
                                    .alpha(0f)
                                    .translationY(0)
                                    .setDuration(500);
                            mOnOffButton.animate()
                                    .translationY(0)
                                    .setDuration(500);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            mTimerTV.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                            //nothing
                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {
                            //nothing
                        }
                    });
        }

        //update ui to reflect that geofencing is on
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mOnOffButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.geofencing_button_off));
        }
        mOnOffButton.setTextColor(ContextCompat.getColor(getContext(), R.color.textPrimary));
        mOnOffButton.setText(getString(R.string.geofencing_off));
    }
}
