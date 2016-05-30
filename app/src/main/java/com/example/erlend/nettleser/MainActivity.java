package com.example.erlend.nettleser;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private static final String TAG = "Main tag";
    private WebView mWebView;
    private EditText addWebsite_text;
    private ProgressBar progress;
    private String currentURL;
    private String handledURL;
    private ImageButton ib;


    private BookmarkDbHelper bDbHelper;

    // Variable to determine if incognito is enabled.
    private boolean isIncognito = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /**
         * Receive the current URL if the activity was changed. Does nothing
         * if there was no previous activity, i.e. we have not yet changed activity.
         */
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            currentURL = bundle.getString("currentURL");
            isIncognito = bundle.getBoolean("getIncognito");
        }

        if (isIncognito == true) {
            this.setTitle("Incognito mode");
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        super.onCreate(savedInstanceState);
        AdBlocker.init(this);
        setContentView(R.layout.main);

        // Allow app to recieve favicons from urls
        WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());



        final String defaultURL = "http://www.google.com";

        /**
         * Popup menu. Should probably be changed to an options menu?
         */
        final Button popupButton = (Button)findViewById(R.id.set_button);
        popupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(),view);

                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.show();

                popupButton.setBackground(getResources().getDrawable(R.mipmap.settingsclicked));

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId()==R.id.one) {
                            // just showing a "Created new tab" text
                            Toast.makeText(getApplicationContext(),"Created new tab",Toast.LENGTH_SHORT).show();

                            return true;
                        }
                        if (item.getItemId()==R.id.two) {

                            // swapping layout with changeActivity method
                            Toast.makeText(getApplicationContext(),"Changed layout",Toast.LENGTH_SHORT).show();
                            changeActivity();
                        }
                        if (item.getItemId()==R.id.three) {
                            // saves the current URL into the bookmark database
                            Toast.makeText(getApplicationContext(),"Added bookmark",Toast.LENGTH_SHORT).show();

                            byte [] favicon;
                            favicon = DbBitmapUtility.getBytes(mWebView.getFavicon());
                            if (favicon == null) {
                                System.out.println("no favicon found");
                            }

                            addEntry(mWebView.getTitle(), mWebView.getUrl(), favicon);
                        }
                        if (item.getItemId()==R.id.four) {
                            // Show the bookmark view
                            Intent startBookmarkActivity = new Intent(MainActivity.this, BookmarkActivity.class);
                            startActivityForResult(startBookmarkActivity, 1);

                        }

                        /**
                         * Enable or disable incognito mode
                         */
                        if (item.getItemId()==R.id.five) {
                            System.out.println("Incognito is now: " + isIncognito);
                            if (isIncognito == false) {
                                isIncognito = true;
                                Intent intent = getIntent();
                                intent.putExtra("getIncognito", isIncognito);
                                intent.putExtra("currentURL", defaultURL);
                                finish();
                                startActivity(intent);
                                System.out.println("Incognito is now enabled");
                            }
                            else if (isIncognito == true) {
                                isIncognito = false;
                                Intent intent = getIntent();
                                intent.putExtra("getIncognito", isIncognito);
                                intent.putExtra("currentURL", defaultURL);
                                finish();
                                startActivity(intent);
                                System.out.println("Incognito is no disabled");
                            }
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
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new ThisWebViewClient());
        mWebView.getSettings().setUseWideViewPort(true); // fix scaling on some pages
        mWebView.getSettings().setLoadWithOverviewMode(true); // fix scaling on some pages

        progress = (ProgressBar) findViewById(R.id.progressBar);


        /**
         * Incognito/private browsing mode.
         * Using a global variable to set incognito to enabled or disabled.
         * When enabled no cookies are stored, no forms are stored and no passwords are stored.
         */
        if (isIncognito()) {
            //Make sure no cookies are created
            if (Build.VERSION.SDK_INT >= 21) {
                CookieManager.getInstance().removeAllCookies(null);
            } else {
                CookieManager.getInstance().setAcceptCookie(false);
            }
            //Make sure no caching is done
            mWebView.getSettings().setCacheMode(mWebView.getSettings().LOAD_NO_CACHE);
            mWebView.getSettings().setAppCacheEnabled(false);
            mWebView.clearHistory();
            mWebView.clearCache(true);
            //Make sure no autofill for Forms/ user-name password happens for the app
            WebViewDatabase.getInstance(this).clearFormData();
            mWebView.clearFormData();
            mWebView.getSettings().setSavePassword(false);
            mWebView.getSettings().setSaveFormData(false);
        }

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

                        // Checking if the URL starts with https or http. If not then we
                        // add it at the start of the URL string. This is a fix for some pages
                        // requiring the http or https tag at the start.
                        if (URL.startsWith("https://")) {
                            handledURL = URL;
                        } else if (URL.startsWith("http://")) {
                            handledURL = URL;
                        } else {
                            handledURL = "http://" + URL;
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

    // Insert into bookmark database. Requires a title, an url and a bitmap image
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
     * is hidden when the page has finished loading.
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

        /**
         * Enable receiving favicons
         */
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra("currentURL");
                mWebView.loadUrl(result);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

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
         * For some reason links on Google-search won't open without this
         */
        @Override
        public void onPageFinished(WebView view, String url)
        {
            System.out.println("onPageFinished: " + url);
            if ("about:blank".equals(url) && view.getTag() != null)
                view.loadUrl(view.getTag().toString());
            else
                view.setTag(url);
        }

        // Caching the checked urls
        private Map<String, Boolean> loadedUrls = new HashMap<>();

        /**
         * Check if the urls that are being requested to load are ads. If they are ads we
         * remove hide them and try to create an empty HTML container to replace them.
         *
         * The urls that have already been checked are cached so we
         * won't spend time and resources checking them again.
         */
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            boolean ad;
            if (!loadedUrls.containsKey(url)) {
                ad = AdBlocker.isAd(url);
                loadedUrls.put(url, ad);
            } else {
                ad = loadedUrls.get(url);
            }
            return ad ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, url);
        }

    }

    public boolean isIncognito() {
        return isIncognito;
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
