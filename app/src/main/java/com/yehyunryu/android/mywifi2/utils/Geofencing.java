package com.yehyunryu.android.mywifi2.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.yehyunryu.android.mywifi2.R;
import com.yehyunryu.android.mywifi2.receivers.GeofenceBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;
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

public class Geofencing implements ResultCallback {
    private static final String LOG_TAG = Geofencing.class.getSimpleName();
    private static final long GEOFENCE_DURATION = TimeUnit.DAYS.toMillis(1);
    private static final float GEOFENCE_RADIUS = 125f; //125 meters

    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public Geofencing(Context context, GoogleApiClient client) {
        mContext = context;
        mGoogleApiClient = client;
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    public void registerAllGeofences() {
        //if google api client is not connected or if geofence list is empty return early
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected() ||
                mGeofenceList == null || mGeofenceList.size() == 0) {
            Log.d(LOG_TAG, "Empty Geofences");
            return;
        }

        //add geofences using Google Location Services
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        Log.d(LOG_TAG, "Geofences Registered");
    }

    public void unregisterAllGeofences() {
        //check if google api client is connected and return early if so
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()) return;

        //remove geofences
        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch(SecurityException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        Log.d(LOG_TAG, "Geofences Unregistered");
    }

    public void updateGeofencesList(PlaceBuffer places) {
        //loop through each place and build a geofence object and set its specifications
        mGeofenceList = new ArrayList<>();
        if(places == null || places.getCount() == 0) return;
        for(Place place: places) {
            String placeId = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLng = place.getLatLng().longitude;

            long durationTime;
            long beginTime = PreferenceManager.getDefaultSharedPreferences(mContext).getLong(mContext.getString(R.string.geofencing_time_key), -1);
            long currentTime = System.currentTimeMillis();

            if(beginTime <= 0) {
                durationTime = GEOFENCE_DURATION;
            } else if(currentTime - beginTime > TimeUnit.DAYS.toMillis(1)) {
                durationTime = GEOFENCE_DURATION;
            } else {
                durationTime = GEOFENCE_DURATION - (currentTime - beginTime);
            }

            Log.d(LOG_TAG, "Duration Time: " + durationTime);

            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeId)
                    .setExpirationDuration(durationTime)
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build();

            //add geofence object to list
            mGeofenceList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        //create geofencing request and set initial trigger for all listed geofences
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        //return early if pending intent already exists
        if(mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        //create pending intent that triggers geofence broadcast receiver
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
        //error logging for geofencing
        Log.d(LOG_TAG, String.format("Geofence Result: %s", result.getStatus().toString()));
    }
}
