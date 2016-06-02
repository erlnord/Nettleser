package com.browser.volant.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import com.browser.volant.BitmapUtility;

import java.util.ArrayList;

/**
 * Created by larsmartin on 13/05/16.
 */
public class BookmarkDbAdapter {

    private final Context context;
    private SQLiteDatabase db;
    private BookmarkDbHelper bDbHelper;

    public BookmarkDbAdapter(Context ctx) {
        this.context = ctx;
        bDbHelper = new BookmarkDbHelper(ctx);
    }

    public BookmarkDbAdapter open(Context context) {
        db = bDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        bDbHelper.close();
    }

    public Cursor getAllRows() {
        String selectAllQuery = " SELECT * FROM " + BookmarkDbHelper.TABLE_NAME;
        SQLiteDatabase db = bDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(selectAllQuery, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public ArrayList<Bitmap> getAllImages() {
        ArrayList<Bitmap> bitList = new ArrayList<Bitmap>();

        Bitmap bitmap;

        SQLiteDatabase db = bDbHelper.getWritableDatabase();
        String selectQuery = " SELECT * FROM " + BookmarkDbHelper.TABLE_NAME;
        Cursor c = db.rawQuery(selectQuery,null);

        if (c!=null) {
            while (c.moveToNext()) {
                byte[] bitBytes = c.getBlob(c.getColumnIndex(BookmarkDbHelper.COLUMN_NAME_FAVICON));
                bitmap = BitmapUtility.getImage(bitBytes);
                bitList.add(bitmap);
            }
        }
        return bitList;
    }

}
