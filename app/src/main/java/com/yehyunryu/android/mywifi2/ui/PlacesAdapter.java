package com.yehyunryu.android.mywifi2.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.PlaceBuffer;
import com.yehyunryu.android.mywifi2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Yehyun Ryu on 9/9/2017.
 */

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder> {
    private PlaceBuffer mPlaces;

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
        if(mPlaces == null) {
            return 0;
        }
        return mPlaces.getCount();
    }

    public void swapPlaces(PlaceBuffer places) {
        if(places != null) {
            notifyDataSetChanged();
        }
        mPlaces = places;
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.place_name_tv) TextView mNameTV;
        @BindView(R.id.place_address_tv) TextView mAddressTV;
        @BindView(R.id.place_clear_button) ImageButton mClearButton;

        public PlacesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(int position){
            String placeId = mPlaces.get(position).getId();
            String placeName = mPlaces.get(position).getName().toString();
            String placeAddress = mPlaces.get(position).getAddress().toString();

            mNameTV.setText(placeName);
            mAddressTV.setText(placeAddress);
        }

        @OnClick(R.id.place_clear_button)
        public void onClearClick() {
            Toast.makeText(itemView.getContext(), "Delete", Toast.LENGTH_SHORT).show();
        }
    }
}
