package com.yehyunryu.android.mywifi2.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.yehyunryu.android.mywifi2.R;
import com.yehyunryu.android.mywifi2.utils.Geofencing;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static com.yehyunryu.android.mywifi2.data.PlacesContract.PlacesEntry;
import static com.yehyunryu.android.mywifi2.ui.MainActivity.PLACE_PICKER_REQUEST;

public class PlacesFragment extends Fragment implements PlacesAdapter.PlaceItemDeleteListener {
    @BindView(R.id.places_rv) RecyclerView mPlaceRV;
    @BindView(R.id.places_fab) FloatingActionButton mPlacesFAB;
    @BindView(R.id.places_empty_view) LinearLayout mEmptyView;

    private static final String LOG_TAG = PlacesFragment.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private Geofencing mGeofencing;
    private PlacesAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_places, container, false);
        ButterKnife.bind(this, rootView);

        //set layout manager and adapter to recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mPlaceRV.setLayoutManager(layoutManager);
        mAdapter = new PlacesAdapter(getContext(), this);
        mPlaceRV.setAdapter(mAdapter);

        //get google api client and geofencing object
        mGoogleApiClient = ((MainActivity) getActivity()).mGoogleApiClient;
        mGeofencing = ((MainActivity) getActivity()).mGeofencing;

        //refresh place data
        refreshPlacesData();

        return rootView;
    }

    private void refreshPlacesData() {
        //query places saved in database
        Cursor cursor = getContext().getContentResolver().query(
                PlacesEntry.PLACES_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if(cursor == null || cursor.getCount() == 0) {
            //return early if place is empty
            mEmptyView.setVisibility(View.VISIBLE);
            mAdapter.swapPlaces(null);
            return;
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
        //store place id in a array list
        List<String> places = new ArrayList<>();
        while(cursor.moveToNext()) {
            places.add(cursor.getString(cursor.getColumnIndex(PlacesEntry.COLUMN_PLACE_ID)));
        }

        //store GeoData in a PlaceBuffer using place id list
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, places.toArray(new String[places.size()]));
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                //swap place data for recycler view
                mAdapter.swapPlaces(places);

                //update geofence list
                mGeofencing.updateGeofencesList(places);

                //register geofences if geofence is enabled
                if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.geofencing_key), false)) {
                    mGeofencing.registerAllGeofences();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PLACE_PICKER_REQUEST:
                //when place picker is called
                if(resultCode == RESULT_OK) {
                    //when place is picked

                    //get place and check if place is empty
                    Place place = PlacePicker.getPlace(getActivity(), data);
                    if(place == null) {
                        return;
                    }

                    //get place id
                    String placeId = place.getId();

                    //insert place to database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(PlacesEntry.COLUMN_PLACE_ID, placeId);
                    getContext().getContentResolver().insert(
                            PlacesEntry.PLACES_CONTENT_URI,
                            contentValues
                    );

                    //refresh place data
                    refreshPlacesData();
                }
                return;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OnClick(R.id.places_fab)
    public void onAddClick() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: Automatically switch to settings fragment
            //show toast message
            Toast.makeText(getContext(), getString(R.string.need_location_permission), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            //build place picker intent and start it
            Intent placePickerIntent = new PlacePicker.IntentBuilder().build(getActivity());
            startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            //TODO: Help user to repair GooglePlayServices
            Log.d(LOG_TAG, "GooglePlayServicesRepairable");
        } catch (GooglePlayServicesNotAvailableException e) {
            //TODO: Help user install GooglePlayServices
            Log.d(LOG_TAG, "GooglePlayServicesNotAvailable");
        }
    }

    @OnClick(R.id.places_empty_view)
    public void onEmptyClick() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: Automatically switch to settings fragment
            //show toast message
            Toast.makeText(getContext(), getString(R.string.need_location_permission), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            //build place picker intent and start it
            Intent placePickerIntent = new PlacePicker.IntentBuilder().build(getActivity());
            startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            //TODO: Help user to repair GooglePlayServices
            Log.d(LOG_TAG, "GooglePlayServicesRepairable");
        } catch (GooglePlayServicesNotAvailableException e) {
            //TODO: Help user install GooglePlayServices
            Log.d(LOG_TAG, "GooglePlayServicesNotAvailable");
        }
    }

    @Override
    public void onPlaceDelete(int position) {
        //refresh place data when place item is deleted
        refreshPlacesData();
    }
}
