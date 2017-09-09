package com.yehyunryu.android.mywifi2.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Yehyun Ryu on 9/9/2017.
 */

public class PlacesContract {

    public static final String CONTENT_AUTHORITY = "com.yehyunryu.android.mywifi2";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PLACES = "places";

    public static class PlacesEntry implements BaseColumns {

        public static final Uri PLACES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build();

        public static final String PLACES_TABLE_NAME = "places";

        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_PLACE_NAME = "place_name";

        public static final String COLUMN_PLACE_ADDRESS = "place_address";
    }
}
