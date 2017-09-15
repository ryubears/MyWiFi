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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yehyunryu.android.mywifi2.R;
import com.yehyunryu.android.mywifi2.service.CountdownTimerService;
import com.yehyunryu.android.mywifi2.utils.Geofencing;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.yehyunryu.android.mywifi2.data.PlacesContract.PlacesEntry;

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

        //get geofencing object from MainActivity
        mGeofencing = ((MainActivity) getActivity()).mGeofencing;

        //get toast from main activity
        mToast = ((MainActivity) getActivity()).mToast;

        //set appropriate icon and text for on/off image view and button
        if(mIsGeofencing) {
            //ON

            //fade in timer and move the icon and the button apart
            mTimerTV.animate()
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            mOnOffIV.animate()
                                    .translationY(-52)
                                    .setDuration(500);
                            mOnOffButton.animate()
                                    .translationY(53)
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

            //update ui to reflect that geofencing is on
            mOnOffIV.setImageResource(R.drawable.place_on_yellow);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mOnOffButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.geofencing_button_on));
            }
            mOnOffButton.setTextColor(ContextCompat.getColor(getContext(), R.color.textSecondary));
            mOnOffButton.setText(getString(R.string.geofencing_on));
        } else {
            //OFF

            //fade out timer and move the icon and the button back together
            mTimerTV.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            mOnOffIV.animate()
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

            //update ui to reflect that geofencing is on
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

            //fade out timer and move icon and button together
            mTimerTV.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            mOnOffIV.animate()
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

            //update ui to reflect that geofencing is off
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

            //query places saved in database
            Cursor cursor = getContext().getContentResolver().query(
                    PlacesEntry.PLACES_CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );

            if(cursor == null || cursor.getCount() == 0) {
                //show toast if there are no place to register
                if(mToast != null) mToast.cancel();
                mToast = Toast.makeText(getContext(), getString(R.string.need_places_toast), Toast.LENGTH_SHORT);
                mToast.show();
            } else {

                //fade in timer and move icon and button apart
                mTimerTV.animate()
                        .alpha(1.0f)
                        .setDuration(500)
                        .setListener(new Animator.AnimatorListener() {
                             @Override
                             public void onAnimationStart(Animator animator) {
                                 mOnOffIV.animate()
                                         .translationY(-52)
                                         .setDuration(500);
                                 mOnOffButton.animate()
                                         .translationY(53)
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

                //update ui to reflect that geofencing is on
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
