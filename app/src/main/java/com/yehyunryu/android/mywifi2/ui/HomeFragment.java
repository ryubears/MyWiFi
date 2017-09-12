package com.yehyunryu.android.mywifi2.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.yehyunryu.android.mywifi2.R;
import com.yehyunryu.android.mywifi2.utils.Geofencing;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeFragment extends Fragment {
    @BindView(R.id.home_onoff_iv) ImageView mOnOffIV;
    @BindView(R.id.home_onoff_button) Button mOnOffButton;

    //geofencing object and state of geofencing
    private Geofencing mGeofencing;
    private boolean mIsGeofencing;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate and bind views
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootView);

        //get geofencing object from MainActivity
        mGeofencing = ((MainActivity) getActivity()).mGeofencing;

        //get state of geofencing
        mIsGeofencing = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getContext().getString(R.string.geofencing_key), false);
        //set appropriate icon and text for on/off image view and button
        if(mIsGeofencing) {
            setGeofencingOn();
        } else {
            setGeofencingOff();
        }

        return rootView;
    }

    @OnClick(R.id.home_onoff_button)
    public void onButtonClick() {
        if(mIsGeofencing) {
            //set geofencing to false
            mIsGeofencing = false;
            setGeofencingOff();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(getString(R.string.geofencing_key), false).apply();
            //unregister all geofences
            mGeofencing.unregisterAllGeofences();
        } else {
            //set geofencing to true
            mIsGeofencing = true;
            setGeofencingOn();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(getString(R.string.geofencing_key), true).apply();
            //register all geofences
            mGeofencing.registerAllGeofences();
        }
    }

    private void setGeofencingOff() {
        //set appropriate icon and text to indicate that geofencing is off
        mOnOffIV.setImageResource(R.drawable.place_off);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mOnOffButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.geofencing_button_off));
        }
        mOnOffButton.setTextColor(ContextCompat.getColor(getContext(), R.color.textPrimary));
        mOnOffButton.setText(getString(R.string.geofencing_off));
    }

    private void setGeofencingOn() {
        //set appropriate icon and text to indicate that geofencing is on
        mOnOffIV.setImageResource(R.drawable.place_on_yellow);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mOnOffButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.geofencing_button_on));
        }
        mOnOffButton.setTextColor(ContextCompat.getColor(getContext(), R.color.textSecondary));
        mOnOffButton.setText(getString(R.string.geofencing_on));
    }
}
