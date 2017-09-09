package com.yehyunryu.android.mywifi2.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yehyunryu.android.mywifi2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlacesFragment extends Fragment {
    @BindView(R.id.places_rv) RecyclerView mPlaceRV;
    @BindView(R.id.places_fab) FloatingActionButton mPlacesFAB;

    private PlacesAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_places, container, false);
        ButterKnife.bind(this, rootView);

        String[] placeNames = {
                "Sanford Hall",
                "44 North Apartments",
                "White House",
                "Area 51"
        };
        String[] placeAddresses = {
                "1122 University Ave SE, Minneapolis, 55455 MN",
                "2701 4th Street SE, Minneapolis, 55414 MN",
                "1600 Pennsylvania Ave NW, Washington, DC 20500",
                "Blah Blah Blah Blah"
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mPlaceRV.setLayoutManager(layoutManager);
        mAdapter = new PlacesAdapter(placeNames, placeAddresses);
        mPlaceRV.setAdapter(mAdapter);

        return rootView;
    }

    @OnClick(R.id.places_fab)
    public void onAddClick() {
        Toast.makeText(getContext(), "Add", Toast.LENGTH_SHORT).show();
    }
}
