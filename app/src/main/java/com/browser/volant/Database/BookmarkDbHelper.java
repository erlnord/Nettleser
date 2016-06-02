package com.browser.volant.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class contains creation of a database containing bookmarks
 * Created by larsmartin on 12/05/16.
 */
public class BookmarkDbHelper extends SQLiteOpenHelper {

    // Table name
    public static final String TABLE_NAME = "table_bookmarks";

    // Column name
    public static final String COLUMN_NAME_ENTRY_ID = "_id";
    public static final String COLUMN_NAME_TITLE = "bookmark_title";
    public static final String COLUMN_NAME_URL = "bookmark_url";
    public static final String COLUMN_NAME_FAVICON = "bookmark_favicon";


    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    // Table create statement
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
            TABLE_NAME + " (" + COLUMN_NAME_ENTRY_ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_URL + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_FAVICON + " BLOB);";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    // Database version
    private static final int DATABASE_VERSION = 1;
    // Database name
    public static final String DATABASE_NAME = "bookmark_db";


    public BookmarkDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public static String getColumnNameEntryId() {
        return COLUMN_NAME_ENTRY_ID;
    }

    public static String getColumnNameTitle() {
        return COLUMN_NAME_TITLE;
    }

    public static String getColumnNameUrl() {
        return COLUMN_NAME_URL;
    }

    public static String getColumnNameFavicon() {
        return COLUMN_NAME_FAVICON;
    }
}