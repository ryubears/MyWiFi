package com.yehyunryu.android.mywifi2.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yehyunryu.android.mywifi2.R;
import com.yehyunryu.android.mywifi2.data.PlacesContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlacesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.places_rv) RecyclerView mPlaceRV;
    @BindView(R.id.places_fab) FloatingActionButton mPlacesFAB;

    private static final int PLACES_LOADER_ID = 404;

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

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(PLACES_LOADER_ID, null, this);

        return rootView;
    }

    @OnClick(R.id.places_fab)
    public void onAddClick() {
        //TODO: Temporary
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlacesContract.PlacesEntry.COLUMN_PLACE_NAME, "Sanford Hall");
        contentValues.put(PlacesContract.PlacesEntry.COLUMN_PLACE_ADDRESS, "1122 University Ave SE, Minneapolis 55455, MN");
        getContext().getContentResolver().insert(
                PlacesContract.PlacesEntry.PLACES_CONTENT_URI,
                contentValues
        );
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id) {
            case PLACES_LOADER_ID:
                return new CursorLoader(
                        getContext(),
                        PlacesContract.PlacesEntry.PLACES_CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            default:
                throw new UnsupportedOperationException("This loader is not implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
