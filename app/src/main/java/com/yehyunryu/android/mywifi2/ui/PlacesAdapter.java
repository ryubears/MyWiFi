package com.yehyunryu.android.mywifi2.ui;

import android.content.ContentUris;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yehyunryu.android.mywifi2.R;
import com.yehyunryu.android.mywifi2.data.PlacesContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Yehyun Ryu on 9/9/2017.
 */

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder> {
    private Cursor mCursor;

    @Override
    public PlacesAdapter.PlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        int layoutId = R.layout.place_item;
        boolean attachToParentImmediately = false;

        View view = layoutInflater.inflate(layoutId, parent, attachToParentImmediately);
        return new PlacesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlacesAdapter.PlacesViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if(mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if(newCursor != null) {
            notifyDataSetChanged();
        }
        mCursor = newCursor;
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.place_name_tv) TextView mNameTV;
        @BindView(R.id.place_address_tv) TextView mAddressTV;
        @BindView(R.id.place_clear_button) ImageButton mClearButton;

        private int mPlaceId;
        private String mPlaceName;
        private String mPlaceAddress;


        public PlacesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(int position){
            mCursor.moveToPosition(position);

            int indexId = mCursor.getColumnIndex(PlacesContract.PlacesEntry._ID);
            int indexPlaceName = mCursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_PLACE_NAME);
            int indexPlaceAddress = mCursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_PLACE_ADDRESS);

            mPlaceId = mCursor.getInt(indexId);
            mPlaceName = mCursor.getString(indexPlaceName);
            mPlaceAddress = mCursor.getString(indexPlaceAddress);

            mNameTV.setText(mPlaceName);
            mAddressTV.setText(mPlaceAddress);
        }

        @OnClick(R.id.place_clear_button)
        public void onClearClick() {
            itemView.getContext().getContentResolver().delete(
                    ContentUris.withAppendedId(PlacesContract.PlacesEntry.PLACES_CONTENT_URI, mPlaceId),
                    null,
                    null
            );
        }
    }
}
