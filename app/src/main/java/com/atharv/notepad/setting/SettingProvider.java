package com.atharv.notepad.setting;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.atharv.notepad.setting.Setting_Contract.SettingEntry;


public class SettingProvider extends ContentProvider {
    public static final String LOG_TAG = com.atharv.notepad.data.NoteProvider.class.getSimpleName();

    private Settingdb mDbHelper;

    private static final int NOTES = 100;

    private static final int NOTE_ID = 101;

    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(SettingEntry.CONTENT_AUTHORITY, SettingEntry.PATH_NOTE, NOTES);
        sUriMatcher.addURI(SettingEntry.CONTENT_AUTHORITY, SettingEntry.PATH_NOTE + "/#", NOTE_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new Settingdb(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        Log.v("","Match:-"+match);
        switch (match) {
            case NOTES:
                cursor = database.query(SettingEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NOTE_ID:
                selection = SettingEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(SettingEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);

        Log.v("","LockScreen:-"+sUriMatcher+uri+match);
        switch (match) {
            case NOTES:
                return insertPASS(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    private Uri insertPASS(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(SettingEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return updatepass(uri, contentValues, selection, selectionArgs);
            case NOTE_ID:
                selection = SettingEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatepass(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatepass(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(SettingEntry.COLUMN_THEME)) {
            String name = values.getAsString(SettingEntry.COLUMN_THEME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated =   database.update(SettingEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                rowsDeleted = database.delete(SettingEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTE_ID:
                selection = SettingEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(SettingEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }


    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return SettingEntry.CONTENT_LIST_TYPE;
            case NOTE_ID:
                return SettingEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
