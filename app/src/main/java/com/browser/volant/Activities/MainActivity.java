package com.browser.volant.Activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextMenu;
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

import com.browser.volant.AdBlocker;
import com.browser.volant.BitmapUtility;
import com.browser.volant.Database.BookmarkDbHelper;
import com.browser.volant.R;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private static final String TAG = "Main tag";

    private WebView mWebView;
    private EditText addWebsiteText;
    private ProgressBar progress;
    private ImageButton ib;

    // Variable to determine if incognito is enabled. It is disabled by default.
    private boolean isIncognito = false;
    // Variable to determnie if AdBlock is enabled. This is enabled by default.
    private boolean adblockEnabled = true;
    private String currentURL; // global variable for the current url
    private String defaultURL = "http://www.google.com";

    private String isAdblockEnabled;
    private String getTempUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // We check if the adblock function is enabled or disabled before we enable the view.
        SharedPreferences adblockprefs = getSharedPreferences(isAdblockEnabled, MODE_PRIVATE);
        adblockEnabled = adblockprefs.getBoolean("adblockstatus", true);

        // We get the temporary url (if there is one) and use it. Then we remove the temp url.
        SharedPreferences urlprefs = getSharedPreferences(getTempUrl, MODE_PRIVATE);
        currentURL = urlprefs.getString("tempurl", null);
        saveTempUrl(null);

        /**
         * Check if incognito is enabled before we enable the view layout.
         */
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) { isIncognito = bundle.getBoolean("getIncognito");}

        /**
         * When incognito is enabled this is shown on the app's title. The titlebar is hidden
         * if incognito is disabled.
         */
        if (isIncognito) {
            this.setTitle("Incognito mode");
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        super.onCreate(savedInstanceState);
        AdBlocker.init(this);
        setContentView(R.layout.main);

        // Allow app to recieve favicons from urls
        WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());

        /**
         * Popup menu. Should probably be changed to an options menu?
         */
        final Button popupButton = (Button) findViewById(R.id.set_button);
        popupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);

                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.one) {
                            // just showing a "Created new tab" text
                            Toast.makeText(getApplicationContext(),
                                    "Created new tab", Toast.LENGTH_SHORT).show();

                            return true;
                        }
                        if (item.getItemId() == R.id.two) {

                            // swapping layout with changeActivity method
                            Toast.makeText(getApplicationContext(),
                                    "Changed layout", Toast.LENGTH_SHORT).show();
                            changeActivity();
                        }
                        if (item.getItemId() == R.id.three) {
                            // saves the current URL into the bookmark database
                            Toast.makeText(getApplicationContext(),
                                    "Added bookmark", Toast.LENGTH_SHORT).show();

                            byte[] favicon;
                            favicon = BitmapUtility.getBytes(mWebView.getFavicon());
                            if (favicon == null) {
                                System.out.println("no favicon found");
                            }

                            addEntry(mWebView.getTitle(), mWebView.getUrl(), favicon);
                        }
                        if (item.getItemId() == R.id.four) {
                            // Show the bookmark view
                            Intent startBookmarkActivity = new Intent(MainActivity.this,
                                    BookmarkActivity.class);
                            startActivityForResult(startBookmarkActivity, 1);

                        }
                        /**
                         * Enable or disable incognito mode.
                         */
                        if (item.getItemId() == R.id.five) {
                            System.out.println("Incognito was: " + isIncognito);
                            if (!isIncognito) {
                                isIncognito = true;
                                Intent intent = getIntent();
                                intent.putExtra("getIncognito", isIncognito);
                                intent.putExtra("currentURL", defaultURL);
                                finish();
                                // starts a new activity so all history is purged
                                startActivity(intent);
                                System.out.println("Incognito is now enabled.");
                            } else if (isIncognito) {
                                isIncognito = false;
                                Intent intent = getIntent();
                                intent.putExtra("getIncognito", isIncognito);
                                intent.putExtra("currentURL", defaultURL);
                                finish();
                                // starts a new activity so all history is purged
                                startActivity(intent);
                                System.out.println("Incognito is no disabled.");
                            }
                        }
                        if (item.getItemId() == R.id.six) {
                            System.out.println("Adblock was: " + adblockEnabled);
                            if (isadblockEnabled()) {
                                saveAdblockStatus(false);
                                saveTempUrl(mWebView.getUrl().toString());
                                Toast.makeText(getApplicationContext(),
                                        "Disabled adblock.", Toast.LENGTH_SHORT).show();
                                MainActivity.this.recreate();
                                System.out.println("Adblock has been disabled.");
                            } else if (!isadblockEnabled()) {
                                saveAdblockStatus(true);
                                saveTempUrl(mWebView.getUrl().toString());
                                Toast.makeText(getApplicationContext(),
                                        "Enabled adblock.", Toast.LENGTH_SHORT).show();
                                MainActivity.this.recreate();
                                System.out.println("Adblock has been enabled.");
                            }
                        }
                        return false;
                    }
                });
            }
        });

        Log.i(TAG, "onCreate");
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true); //Enable javascript
        mWebView.getSettings().setBuiltInZoomControls(true); // enable zoom
        mWebView.getSettings().setDisplayZoomControls(false); // remove on-display zoom controls
        mWebView.getSettings().setDomStorageEnabled(true);
        registerForContextMenu(mWebView);
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
        addWebsiteText = (EditText) findViewById(R.id.website_text);
        addWebsiteText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String url = addWebsiteText.getText().toString();

                    // Checking if the URL is valid or not
                    boolean isURL = Patterns.WEB_URL.matcher(addWebsiteText.getText()
                            .toString()).matches();

                    String handledURL = "";

                    // If the URL is valid, we open the webpage
                    if (isURL) {
                        // Make sure that a https or http tag exists at the start of the url
                        handledURL = urlHandler(url);
                    }
                    // If the URL is not valid, we do a google search for the string
                    else if (!isURL) {
                        handledURL = "https://www.google.com/search?q=" + url;
                    }

                    addWebsiteText.setText(handledURL);
                    mWebView.loadUrl(handledURL);
                    mWebView.requestFocus();

                    MainActivity.this.progress.setProgress(0);
                }
                return handled;
            }
        });

        /**
         * Show and hide the cancel button on URL-bar focus.
         * Also implicitly summons and dismisses the keyboard.
         */
        ib = (ImageButton) findViewById(R.id.imageButton);
        addWebsiteText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                addWebsiteText.selectAll();

                addWebsiteText.setPadding(dpToPx(10), 0, dpToPx(35), 0);
                ib.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(addWebsiteText, InputMethodManager.SHOW_IMPLICIT);
            } else {
                addWebsiteText.setPadding(dpToPx(10), 0, dpToPx(10), 0);
                addWebsiteText.setText(mWebView.getUrl());
                ib.setVisibility(View.INVISIBLE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(addWebsiteText.getWindowToken(), 0);
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
                addWebsiteText.setText("");
            }
        });

        /**
         * If there is no saved instance state, then load the default webpage(webURL)
         * This is needed because without it, the app will reload to the default webpage
         * every time the phone is tilted.
         */
        if (savedInstanceState == null) {
            if (currentURL != null) {
                mWebView.loadUrl(currentURL);
            } else {
                mWebView.loadUrl(defaultURL);
            }
        }
    }

    /**
     * Convert display-independant pixels to pixels. This is used for display-independant
     * scaling of sizes.
     */
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Checking if the URL starts with https or http. If it does not, we add it at the start
     * of the URL string. This is a fix for some pages requiring the http or https tag
     * at the start of the url.
     */
    public String urlHandler(String url) {
        if (url.startsWith("https://")) {
            return url;
        } else if (url.startsWith("http://")) {
            return url;
        } else {
            return "http://" + url;
        }
    }

    // Insert into bookmark database. Requires a title, an url and a bitmap image
    public final void addEntry(String title, String url, byte[] favicon) {
        BookmarkDbHelper bDbHelper = new BookmarkDbHelper(MainActivity.this);
        SQLiteDatabase db = bDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(BookmarkDbHelper.COLUMN_NAME_TITLE, title);
        cv.put(BookmarkDbHelper.COLUMN_NAME_URL, url);
        cv.put(BookmarkDbHelper.COLUMN_NAME_FAVICON, favicon);
        db.insert(BookmarkDbHelper.TABLE_NAME, null, cv);
    }

    /**
     * Method to add current adblock status (enabled/disabled) to the user settings.
     */
    public void saveAdblockStatus(boolean adblockstatus) {
        SharedPreferences.Editor editor = getSharedPreferences(isAdblockEnabled, MODE_PRIVATE).edit();
        editor.putBoolean("adblockstatus", adblockstatus);
        editor.apply();
    }

    /**
     * Adds a temporary url to the user settings.
     */
    public void saveTempUrl(String tempurl) {
        SharedPreferences.Editor editor = getSharedPreferences(isAdblockEnabled, MODE_PRIVATE).edit();
        editor.putString("tempurl", tempurl);
        editor.apply();
    }

    public boolean isadblockEnabled() {
        return adblockEnabled;
    }

    public void setValue(int progress) {
        this.progress.setProgress(progress);
    }

    /**
     * Switches layout. Puts the URL-bar on the bottom. Starts a new activity.
     */
    public void changeActivity() {
        Intent startNewActivity = new Intent(this, newLayoutActivity.class);
        startNewActivity.putExtra("currentURL", mWebView.getUrl().toString());
        startNewActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startNewActivity);
    }


    /**
     *
     * Create ContextMenu
     * Creates a contextmenu when a long click on pictures and links are detected.
     * Pictures can be viewed, saved, opened in another tab or the link for the picture can be copied to the clipboard.
     * Links can be copied, viewed or opened in another tab.
     *
     * PSA: The context menu does not yet have any functionality except for showing and hiding
     * the menu. The description above is meant to illustrate how it supposed by implemented.
     */

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        WebView.HitTestResult result = mWebView.getHitTestResult();

        // Adds a listener, so that the menu items can actually do something
        MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                // do the menu action
                return true;

            }
        };

        // This checks if the longclick is an image
        if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            // Menu options for an image.
            //set the header title to the image url
            menu.setHeaderTitle(result.getExtra());
            menu.add("Save Image").setOnMenuItemClickListener(handler);
            menu.add("View Image").setOnMenuItemClickListener(handler);
            getMenuInflater().inflate(R.menu.contextual, menu);

            // This checks if the long click is a link
        } else if (result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
            // Menu options for a hyperlink.
            //set the header title to the link url
            menu.setHeaderTitle(result.getExtra());
            menu.add("Copy Link").setOnMenuItemClickListener(handler);
            menu.add("Share Link").setOnMenuItemClickListener(handler);
            getMenuInflater().inflate(R.menu.contextual, menu);
        }
    }

    /**
     * Displays a progressbar when a page is loading. The progressbar
     * is hidden when the page has finished loading.
     * Also updates the URL-bar with the current URL.
     */
    public class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (!addWebsiteText.hasFocus()) {
                addWebsiteText.setText(mWebView.getUrl());
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


    /**
     * Makes the application handle the web call for itself, instead of the default app
     * being called
     */
    public class ThisWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url) {
            webview.loadUrl(url);
            return true;
        }

        /**
         * For some reason links on Google-search won't open without this
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            System.out.println("onPageFinished: " + url);
            if ("about:blank".equals(url) && view.getTag() != null) {
                view.loadUrl(view.getTag().toString());
            }
            else {
                view.setTag(url);
            }
        }


        // Adblocker

        private Map<String, Boolean> loadedUrls = new HashMap<>();   // Caching the checked urls

        /**
         * Check if the urls that are being requested to load are ads. If they are ads we
         * hide them and try to create an empty HTML container to replace them.
         *
         * The urls that have already been checked are cached so we won't spend time
         * and resources checking them again.
         */
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            boolean ad;
            if (isadblockEnabled()) {
                if (!loadedUrls.containsKey(url)) {
                    ad = AdBlocker.isAd(url);
                    loadedUrls.put(url, ad);
                } else {
                    ad = loadedUrls.get(url);
                }
                return ad ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, url);
            }
            return null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("currentURL");
                mWebView.loadUrl(result);
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    public boolean isIncognito() {
        return isIncognito;
    }

    // Enables the browser to return
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else if ((keyCode == KeyEvent.KEYCODE_BACK) && !mWebView.canGoBack()) {
            currentURL = defaultURL;
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    // Call before activity is destroyed
    // Get info about activity before user starts it again
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save state, to let us flip and stuff
        mWebView.saveState(outState);
        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the state saved in the method above
        mWebView.restoreState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");
    }
}
