package com.yehyunryu.android.mywifi2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yehyunryu.android.mywifi2.data.PlacesContract.PlacesEntry;

/**
 * Created by Yehyun Ryu on 9/9/2017.
 */

public class PlacesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "places.db";

    public static final int DATABASE_VERSION = 1;

    public PlacesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_PLACES_TABLE =
                "CREATE TABLE " + PlacesEntry.PLACES_TABLE_NAME + " (" +
                        PlacesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PlacesEntry.COLUMN_PLACE_ID + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_PLACES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //empty for now since there is only one database
    }
}
