package com.browser.volant.Activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.browser.volant.BitmapUtility;
import com.browser.volant.Database.BookmarkDbHelper;
import com.browser.volant.R;

/**
 * Created by zteff1 on 5/4/2016.
 */
public class newLayoutActivity extends Activity {
    private static final String TAG = "Stringtest";
    private WebView mWebView;
    private EditText addWebsite_text;
    private ProgressBar progress;
    private String handledURL;
    private ImageButton ib;

    private BookmarkDbHelper bDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newmain);

        /**
         * Recieve the current URL if the activity was changed.
         */
        Bundle bundle = getIntent().getExtras();
        String currentURL = bundle.getString("currentURL");

        String webURL ="http://www.google.com";

        /**
         * Popup menu. Should probably be changed to an options menu.
         */
        final Button popupButton = (Button)findViewById(R.id.set_button);
        popupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View z) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(),z);

                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.show();

                popupButton.setBackground(getResources().getDrawable(R.mipmap.settingsclicked));
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId()==R.id.one) {
                            Toast.makeText(getApplicationContext(),"Created new tab",Toast.LENGTH_SHORT).show();

                            return true;
                        }
                        if (item.getItemId()==R.id.two){
                            Toast.makeText(getApplicationContext(),"Changed layout",Toast.LENGTH_SHORT).show();
                            changeActivity();
                        }
                        if (item.getItemId()==R.id.three){
                            Toast.makeText(getApplicationContext(),"Added bookmark",Toast.LENGTH_SHORT).show();
                            byte [] favicon;
                            favicon = BitmapUtility.getBytes(mWebView.getFavicon());

                            if (favicon == null) {
                                System.out.println("no favicon found");
                            }

                            addEntry(mWebView.getTitle(), mWebView.getUrl(), favicon);
                            return true;
                        }
                        if (item.getItemId()==R.id.four) {
                            // つ ◕_◕ ༽つ ALLIANCE TAKE MY ENERGY つ ◕_◕ ༽つ
                            Intent startBookmarkActivity = new Intent(newLayoutActivity.this, BookmarkActivity.class);
                            startActivityForResult(startBookmarkActivity, 1);
                            //startBookmarkActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //startActivity(startBookmarkActivity);
                        }
                        return false;
                    }
                });
            }
        });



        Log.i(TAG, "onCreate");
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true); //XSS issues must be resolved, eventually...
        mWebView.getSettings().setDisplayZoomControls(false); // remove on-display zoom controls
        mWebView.getSettings().setBuiltInZoomControls(false); // remove on-display zoom controls
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setVerticalScrollBarEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebChromeClient(new MyWebChromeClient());

        progress = (ProgressBar) findViewById(R.id.progressBar);

        /**
         * Enter key opens url in url-bar
         */
        addWebsite_text = (EditText) findViewById(R.id.website_text);
        addWebsite_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String URL = addWebsite_text.getText().toString();

                    // Checking if the URL is valid or not
                    boolean isURL = Patterns.WEB_URL.matcher(addWebsite_text.getText().toString()).matches();

                    // If the URL is valid, we open the webpage
                    if (isURL) {
                        if (!URL.startsWith("http://")) {
                            handledURL = "http://" + URL;
                        } else {
                            handledURL = URL;
                        }
                    }
                    // If the URL is not valid, we do a google search for the string
                    else if (!isURL) {
                        handledURL = "https://www.google.com/search?q="+URL;
                    }

                    mWebView.loadUrl(handledURL);
                    mWebView.requestFocus();

                    newLayoutActivity.this.progress.setProgress(0);
                }
                return handled;
            }
        });

        /**
         * Show and hide the cancel button on URL-bar focus.
         * Also specifically summons and dismisses the keyboard.
         *
         ib = (ImageButton) findViewById(R.id.imageButton);
         addWebsite_text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
        addWebsite_text.setPadding(20,0,70,0);
        ib.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(addWebsite_text, InputMethodManager.SHOW_IMPLICIT);
        } else {
        addWebsite_text.setPadding(20,0,20,0);
        ib.setVisibility(View.INVISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(addWebsite_text.getWindowToken(), 0);
        }
        }
        });

         */


        mWebView.setWebViewClient(new ThisWebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.v(TAG, "onPageStarted url: " + url);
                addWebsite_text.setText(mWebView.getUrl());
            }

            /**
             * Litt usikker på hva denne gjør, men uten den vil
             * ikke linker fra Google åpnes.
             */
            @Override
            public void onPageFinished(WebView view, String url)
            {
                System.out.println("onPageFinished: " + url);
                if ("about:blank".equals(url) && view.getTag() != null)
                {
                    view.loadUrl(view.getTag().toString());
                }
                else
                {
                    view.setTag(url);
                }
                addWebsite_text.setText(view.getUrl());
            }
        });

        /**
         * If there is no saved instance state, then load the default webpage(webURL)
         * This is needed because without it, the app will reload to the default webpage
         * every time the phone is tilted.
         */
        if (savedInstanceState == null)
        {
            mWebView.loadUrl(currentURL);
        }

    }

    // Insert into bookmark database
    public void addEntry( String title, String url, byte[] favicon) throws SQLiteException {
        bDbHelper = new BookmarkDbHelper(newLayoutActivity.this);
        SQLiteDatabase db = bDbHelper.getWritableDatabase();
        ContentValues cv = new  ContentValues();
        cv.put(BookmarkDbHelper.COLUMN_NAME_TITLE,     title);
        cv.put(BookmarkDbHelper.COLUMN_NAME_URL,       url);
        cv.put(BookmarkDbHelper.COLUMN_NAME_FAVICON,   favicon);
        db.insert(BookmarkDbHelper.TABLE_NAME, null, cv );
    }

    /**
     * Displays a progressbar when a page is loading. The progressbar
     * gets hidden when the page has finished loading.
     * Also updates the URL-bar with the current URL.
     */
    public class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            newLayoutActivity.this.setValue(newProgress);
            super.onProgressChanged(view, newProgress);
            if (progress.getProgress() < 100) {
                progress.setVisibility(View.VISIBLE);
            } else if (progress.getProgress() == 100) {
                progress.setVisibility(View.INVISIBLE);
            }
        }
    }
    public void setValue(int progress) {
        this.progress.setProgress(progress);
    }

    /**
     * Switches layout. Puts the URL-bar on the bottom. Starts a new activity
     */
    public void changeActivity() {
        Intent startNewActivity = new Intent(this, MainActivity.class);
        startNewActivity.putExtra("currentURL", mWebView.getUrl().toString());
        startNewActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startNewActivity);
    }

    /**
     * Makes the application handle the web call for itself, instead of the default app
     * being called
     */
    private class ThisWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url) {
            webview.loadUrl(url);
            return true;
        }
    }

    // Enables the browser to return
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack())
        {
            mWebView.goBack();
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

    // Call before activity is destroyed
    // Get info about activity before user starts it again
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        // Save state, to let us flip and stuff
        mWebView.saveState(outState);
        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the state saved in the method above
        mWebView.restoreState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");
    }

}

