package com.yehyunryu.android.mywifi2.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.PlaceBuffer;
import com.yehyunryu.android.mywifi2.R;
import com.yehyunryu.android.mywifi2.data.PlacesContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Yehyun Ryu on 9/9/2017.
 */

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder> {
    private Context mContext;
    private PlaceItemDeleteListener mPlaceItemDeleteListener;
    private PlaceBuffer mPlaces;

    //place item delete listener interface
    //used for callback
    public interface PlaceItemDeleteListener {
        void onPlaceDelete(int position);
    }

    public PlacesAdapter(Context context, PlaceItemDeleteListener listener) {
        mContext = context;
        mPlaceItemDeleteListener = listener;
    }

    @Override
    public PlacesAdapter.PlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate view and return view holder
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        int layoutId = R.layout.place_item;
        boolean attachToParentImmediately = false;

        View view = layoutInflater.inflate(layoutId, parent, attachToParentImmediately);
        return new PlacesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlacesAdapter.PlacesViewHolder holder, int position) {
        //bind views with place name/address
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if(mPlaces == null) {
            return 0;
        }
        return mPlaces.getCount();
    }

    //used to change PlaceBuffer data
    public void swapPlaces(PlaceBuffer places) {
        notifyDataSetChanged();
        mPlaces = places;
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.place_name_tv) TextView mNameTV;
        @BindView(R.id.place_address_tv) TextView mAddressTV;
        @BindView(R.id.place_delete_button) ImageButton mDeleteButton;

        private Toast mToast;
        private int mPosition;
        private String mPlaceId;

        public PlacesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(int position){
            //get position and place id
            mPosition = position;
            mPlaceId = mPlaces.get(position).getId();

            //set place info
            mNameTV.setText(mPlaces.get(position).getName().toString());
            mAddressTV.setText(mPlaces.get(position).getAddress().toString());
        }

        @OnClick(R.id.place_delete_button)
        public void onDelete() {
            //display toast
            if(mToast != null) mToast.cancel();
            mToast = Toast.makeText(itemView.getContext(), mContext.getString(R.string.toast_delete), Toast.LENGTH_SHORT);
            mToast.show();

            //TODO: Use AsyncTask
            //delete place
            mContext.getContentResolver().delete(
                    PlacesContract.PlacesEntry.PLACES_CONTENT_URI,
                    PlacesContract.PlacesEntry.COLUMN_PLACE_ID + "=?",
                    new String[] {mPlaceId}
            );

            //call onPlaceDelete for callback
            mPlaceItemDeleteListener.onPlaceDelete(mPosition);
        }
    }
}
