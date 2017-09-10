package com.yehyunryu.android.mywifi2.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.yehyunryu.android.mywifi2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeFragment extends Fragment {
    @BindView(R.id.home_onoff_iv) ImageView mOnOffIV;
    @BindView(R.id.home_onoff_button) Button mOnOffButton;

    private boolean mIsGeofencing;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootView);

        mIsGeofencing = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.geofencing_key), false);
        if(mIsGeofencing) {
            setGeofencingOn();
        } else {
            setGeofencingOff();
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    @OnClick(R.id.home_onoff_button)
    public void onButtonClick() {
        if(mIsGeofencing) {
            mIsGeofencing = false;
            setGeofencingOff();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(getString(R.string.geofencing_key), false).apply();
        } else {
            mIsGeofencing = true;
            setGeofencingOn();
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(getString(R.string.geofencing_key), true).apply();
        }
    }

    private void setGeofencingOff() {
        mOnOffIV.setImageResource(R.drawable.place_off);
        mOnOffButton.setText(getString(R.string.geofencing_off));
    }

    private void setGeofencingOn() {
        mOnOffIV.setImageResource(R.drawable.place_on_yellow);
        mOnOffButton.setText(getString(R.string.geofencing_on));
    }
}
