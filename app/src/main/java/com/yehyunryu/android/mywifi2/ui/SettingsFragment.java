package com.yehyunryu.android.mywifi2.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

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

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //load settings preference
        addPreferencesFromResource(R.xml.pref_settings);

        //check and disable location permission by checking its state
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            CheckBoxPreference locationPreference = (CheckBoxPreference) findPreference(getString(R.string.location_preference_key));
            locationPreference.setChecked(true);
            locationPreference.setEnabled(false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //trigger permission dialog when location checkbox is clicked
        if(key.equals(getString(R.string.location_preference_key))) {
            if(sharedPreferences.getBoolean(key, false)) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                //TODO: Handle 'never ask again' dialog
                CheckBoxPreference locationPreference = (CheckBoxPreference) findPreference(getString(R.string.location_preference_key));
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    locationPreference.setEnabled(false);
                } else {
                    //denied
                    locationPreference.setChecked(false);
                }
                break;
            default:
                Log.e(LOG_TAG, "Permission request not yet implemented");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //register shared preference change listener
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister shared preference change listener
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
