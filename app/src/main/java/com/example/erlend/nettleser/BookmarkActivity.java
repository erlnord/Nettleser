package com.example.erlend.nettleser;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookmarkActivity extends Activity {

    private static final String TAG = "Bookmark tag";

    private BookmarkDbHelper dbHelper;
    private BookmarkDbAdapter dbAdapter;
    private ImageView imageView;

    private byte[] bytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmarkview);

        dbHelper = new BookmarkDbHelper(this);
        dbAdapter = new BookmarkDbAdapter(this);

        populateListView();
    }
    /*

    public ArrayList<Bitmap> getAllImages() {
        BookmarkDbHelper bDbHelper = new BookmarkDbHelper(this);
        ArrayList<Bitmap> bitList = new ArrayList<Bitmap>();

        Bitmap bitmap;

        SQLiteDatabase db = bDbHelper.getWritableDatabase();
        db.beginTransaction();
        String selectQuery = " SELECT * FROM " + BookmarkDbHelper.TABLE_NAME;
        Cursor c = db.rawQuery(selectQuery,null);

        if (c != null) {

            c.moveToFirst();

            while (c.moveToNext()) {

                byte[] bitBytes = c.getBlob(c.getColumnIndex(BookmarkDbHelper.COLUMN_NAME_FAVICON));
                System.out.println(Arrays.toString(bitBytes));
                bitmap = DbBitmapUtility.getImage(bitBytes);
                bitList.add(bitmap);
                System.out.println("ADDED BITMAP");
            }
        }
        else {
            System.out.println("Cursor is empty.");
        }
        return bitList;
    }*/

    /**
     * This class populates the bookmarks view with the bookmarks that have been
     * saved to the database.
     */
    public void populateListView() {

        /**
        ArrayList bitmapList = getAllImages();

        ListView lv = (ListView) findViewById(R.id.listView);

        ArrayAdapter<Bitmap> arrayAdapter = new ArrayAdapter<Bitmap>(this,R.layout.item_layout, bitmapList);

        lv.setAdapter(arrayAdapter);
        */

        final Cursor cursor = dbAdapter.getAllRows();

        String[] fromFieldNames = new String[] {BookmarkDbHelper.COLUMN_NAME_TITLE};//, BookmarkDbHelper.COLUMN_NAME_FAVICON};
        int[] toViewIDs = new int[] {R.id.textViewItemTitle};//, R.id.imageView};

        SimpleCursorAdapter myCursorAdapter;

        myCursorAdapter = new SimpleCursorAdapter(this,R.layout.item_layout,cursor,fromFieldNames,toViewIDs,0);

        ListView myList = (ListView) findViewById(R.id.listView);

        myList.setAdapter(myCursorAdapter);


        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = cursor.getString(cursor.getColumnIndex(BookmarkDbHelper.COLUMN_NAME_URL));

                Intent returnIntent = new Intent();
                returnIntent.putExtra("currentURL", url);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

    }

    // Enables the browser to return
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Log.i(TAG, "onStop");
    }
}
