package com.yehyunryu.android.mywifi2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yehyunryu.android.mywifi2.data.PlacesContract.PlacesEntry;

/**
 * Created by Yehyun Ryu on 9/9/2017.
 */

public class PlacesProvider extends ContentProvider {

    private static final int CODE_ALL_PLACES = 300;
    private static final int CODE_SINGLE_PLACE = 301;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private PlacesDbHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String contentAuthority = PlacesContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(contentAuthority, PlacesContract.PATH_PLACES, CODE_ALL_PLACES);
        uriMatcher.addURI(contentAuthority, PlacesContract.PATH_PLACES + "/#", CODE_SINGLE_PLACE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PlacesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch(match) {
            case CODE_ALL_PLACES:
                cursor = mDbHelper.getReadableDatabase().query(
                        PlacesEntry.PLACES_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Invalid uri: " + uri);
        }

        if(cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        Uri returnUri;
        int match = sUriMatcher.match(uri);
        switch(match) {
            case CODE_ALL_PLACES:
                long id = mDbHelper.getWritableDatabase().insert(
                        PlacesEntry.PLACES_TABLE_NAME,
                        null,
                        contentValues
                );
                if(id != -1) {
                    returnUri = ContentUris.withAppendedId(PlacesEntry.PLACES_CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert: " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Invalid uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(PlacesEntry.PLACES_CONTENT_URI, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsDeleted;
        int match = sUriMatcher.match(uri);
        switch(match) {
            case CODE_SINGLE_PLACE:
                String rowId = uri.getLastPathSegment();
                selectionArgs = new String[] {rowId};
                rowsDeleted = mDbHelper.getWritableDatabase().delete(
                        PlacesEntry.PLACES_TABLE_NAME,
                        PlacesEntry._ID + "=?",
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Invalid uri: " + uri);
        }

        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(PlacesEntry.PLACES_CONTENT_URI, null);
        }
        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("MyWiFi does not support getType()");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new RuntimeException("MyWiFi does not support update()");
    }
}
