package com.example.erlend.nettleser;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String TAG = "Stringtest";
    private WebView mWebView;
    private EditText addWebsite_text;
    private ProgressBar progress;
    private String currentURL;
    private String handledURL;
    private ImageButton ib;


    private BookmarkDbHelper bDbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Allow app to recieve favicons from urls
        WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());

        /**
         * Recieve the current URL if the activity was changed. Does nothing
         * if there was no previous activity, i.e. we have not yet changed activity.
         */
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            currentURL = bundle.getString("currentURL");
        }

        String defaultURL ="http://www.google.com";

        /**
         * Popup menu. Should probably be changed to an options menu?
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
                            Toast.makeText(getApplicationContext(),"Changed color",Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        Log.i(TAG, "onCreate");
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true); //XSS issues must be resolved, eventually...
        mWebView.getSettings().setBuiltInZoomControls(true); // enable zoom
        mWebView.getSettings().setDisplayZoomControls(false); // remove on-display zoom controls
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setVerticalScrollBarEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebChromeClient(new MyWebChromeClient() {
            /**
             * Enable receiving favicons
             */
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);

            }
        });

        mWebView.setWebViewClient(new ThisWebViewClient());

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

                    addWebsite_text.setText(handledURL);
                    mWebView.loadUrl(handledURL);
                    mWebView.requestFocus();

                    MainActivity.this.progress.setProgress(0);
                }
                return handled;
            }
        });

        /**
         * Show and hide the cancel button on URL-bar focus.
         * Also explicitly summons and dismisses the keyboard.
         */
        ib = (ImageButton) findViewById(R.id.imageButton);
        addWebsite_text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    addWebsite_text.selectAll();
                    addWebsite_text.setPadding(20,0,70,0);
                    ib.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(addWebsite_text, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    addWebsite_text.setPadding(20,0,20,0);
                    addWebsite_text.setText(mWebView.getUrl());
                    ib.setVisibility(View.INVISIBLE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(addWebsite_text.getWindowToken(), 0);
                }
            }
        });

        /**
         * The cancel button removes the text from the url-bar and
         * replaces it with the current opened URL. The url-bar then
         * loses focus to webview.
         */
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWebsite_text.setText("");


                byte [] favicon;
                favicon = DbBitmapUtility.getBytes(mWebView.getFavicon());

                if (favicon == null) {
                    System.out.println("no favicon found");
                }

                addEntry(mWebView.getTitle(), mWebView.getUrl(), favicon);
            }
        });


        /**
         * If there is no saved instance state, then load the default webpage(webURL)
         * This is needed because without it, the app will reload to the default webpage
         * every time the phone is tilted.
         */
        if (savedInstanceState == null)
        {
            if (currentURL == null) {
                mWebView.loadUrl(defaultURL);
            } else
                mWebView.loadUrl(currentURL);
        }

    }

    // Insert into bookmark database
    public void addEntry( String title, String url, byte[] favicon) throws SQLiteException{
        bDbHelper = new BookmarkDbHelper(MainActivity.this);
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
                if (!addWebsite_text.hasFocus()) {
                    addWebsite_text.setText(mWebView.getUrl());
                }
                MainActivity.this.setValue(newProgress);
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
     * Switches layout. Puts the URL-bar on the bottom. Starts a new activity.
     */
    public void changeActivity() {
        Intent startNewActivity = new Intent(this, newMainActivity.class);
        startNewActivity.putExtra("currentURL", mWebView.getUrl().toString());
        startNewActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
