package com.yehyunryu.android.mywifi2.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Yehyun Ryu on 9/9/2017.
 */

public class PlacesContract {

    //Content authority is like an Android internal name
    //Needed to differentiate between oter content providers
    public static final String CONTENT_AUTHORITY = "com.yehyunryu.android.mywifi2";

    //Content Uri is a URI that identifies data in a provider
    //Base Content Uri provides the basic Uri structure for constructing a Content Uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Path points to a table in a provider
    public static final String PATH_PLACES = "places";

    //Specifics of data structure
    public static class PlacesEntry implements BaseColumns {

        //Content Uri for places table
        public static final Uri PLACES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build();

        //table name for places data
        public static final String PLACES_TABLE_NAME = "places";

        //id of places data
        public static final String _ID = BaseColumns._ID;

        //place id
        public static final String COLUMN_PLACE_ID = "place_id";
    }
}
