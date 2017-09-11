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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static com.yehyunryu.android.mywifi2.data.PlacesContract.PlacesEntry;
import static com.yehyunryu.android.mywifi2.ui.MainActivity.PLACE_PICKER_REQUEST;

public class PlacesFragment extends Fragment {
    @BindView(R.id.places_rv) RecyclerView mPlaceRV;
    @BindView(R.id.places_fab) FloatingActionButton mPlacesFAB;

    private static final String LOG_TAG = PlacesFragment.class.getSimpleName();
    private static final int PLACES_LOADER_ID = 404;

    private GoogleApiClient mGoogleApiClient;
    private PlacesAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_places, container, false);
        ButterKnife.bind(this, rootView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mPlaceRV.setLayoutManager(layoutManager);
        mAdapter = new PlacesAdapter();
        mPlaceRV.setAdapter(mAdapter);

        mGoogleApiClient = ((MainActivity) getActivity()).mGoogleApiClient;
        refreshPlacesData();

        return rootView;
    }

    private void refreshPlacesData() {
        Cursor cursor = getContext().getContentResolver().query(
                PlacesEntry.PLACES_CONTENT_URI,
                null,
                null,
                null,
                null
        );
        if(cursor == null || cursor.getCount() == 0) return;
        List<String> places = new ArrayList<>();
        while(cursor.moveToNext()) {
            places.add(cursor.getString(cursor.getColumnIndex(PlacesEntry.COLUMN_PLACE_ID)));
        }
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, places.toArray(new String[places.size()]));
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                mAdapter.swapPlaces(places);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PLACE_PICKER_REQUEST:
                if(resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(getActivity(), data);
                    if(place == null) {
                        Log.d(LOG_TAG, "No Place Selected");
                        return;
                    }

                    String placeId = place.getId();

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(PlacesEntry.COLUMN_PLACE_ID, placeId);
                    getContext().getContentResolver().insert(
                            PlacesEntry.PLACES_CONTENT_URI,
                            contentValues
                    );
                    Log.d(LOG_TAG, "Place Picked");
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
            Toast.makeText(getContext(), getString(R.string.need_location_permission), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent placePickerIntent = new PlacePicker.IntentBuilder().build(getActivity());
            startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.d(LOG_TAG, "GooglePlayServicesRepairable");
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.d(LOG_TAG, "GooglePlayServicesNotAvailable");
        }
    }
}
