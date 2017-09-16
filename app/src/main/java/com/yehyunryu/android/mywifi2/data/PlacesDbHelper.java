package com.yehyunryu.android.mywifi2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yehyunryu.android.mywifi2.data.PlacesContract.PlacesEntry;

/**
 * Copyright 2017 Yehyun Ryu

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class PlacesDbHelper extends SQLiteOpenHelper {

    //Name of database
    public static final String DATABASE_NAME = "places.db";

    //Database version
    public static final int DATABASE_VERSION = 1;

    public PlacesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //SQLite statement that creates a SQLite database
        final String SQL_CREATE_PLACES_TABLE = "CREATE TABLE " + PlacesEntry.PLACES_TABLE_NAME + " (" +
                PlacesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PlacesEntry.COLUMN_PLACE_ID + " TEXT NOT NULL, " +
                "UNIQUE (" + PlacesEntry.COLUMN_PLACE_ID + ") ON CONFLICT REPLACE);";

        //executes above SQLite statement
        sqLiteDatabase.execSQL(SQL_CREATE_PLACES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //empty for now since there is only one database
    }
}
