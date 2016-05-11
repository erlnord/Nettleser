package com.example.erlend.nettleser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zteff1 on 5/4/2016.
 */
public class newMainActivity extends Activity {
    private static final String TAG = "Stringtest";
    private WebView mWebView;
    private EditText addWebsite_text;
    private ProgressBar progress;
    //private Boolean layout;
    private ImageButton ib;

    public String urlHandler(String string) {
        WebView webView = (WebView)findViewById(R.id.webview);

        String URL = string;

        String Shttp = "http://";
        String Swww = "www.";

        List<String> httpTest = new ArrayList<String>();
        httpTest.add(Shttp);

        List<String> wwwTest = new ArrayList<String>();
        wwwTest.add(Swww);

        Pattern pattern1 = Pattern.compile(String.valueOf(httpTest));
        Matcher matcher1 = pattern1.matcher(URL);

        Pattern pattern2 = Pattern.compile(String.valueOf(wwwTest));
        Matcher matcher2 = pattern2.matcher(URL);

        if (matcher1.find() && matcher2.find()){
            //webView.loadUrl(URL);
            return URL;
        }else if (!matcher1.find() && !matcher2.find()){
            return Shttp+Swww+URL;
            //webView.loadUrl(Shttp+Swww+URL);
        }else if (!matcher1.find()) {
            return Shttp+URL;
            //webView.loadUrl(Shttp+URL);
        }
        else if (!matcher2.find()){
            return Shttp+URL;
            //webView.loadUrl(Shttp+URL);
        }
        Log.d(TAG, "url ="+ URL);
        return URL;
    };

    /**
     * Switches layout. Puts the URL-bar on the bottom. Starts a new activity
     */
    public void changeActivity() {
        Intent startNewActivity = new Intent(this, MainActivity.class);
        startNewActivity.putExtra("currentURL", mWebView.getUrl().toString());
        startNewActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startNewActivity);

    }

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
                    String handledURL = urlHandler(addWebsite_text.getText().toString());
                    mWebView.loadUrl(handledURL);
                    mWebView.requestFocus();

                    newMainActivity.this.progress.setProgress(0);
                }
                return handled;
            }
        });

        /**
         * Show and hide the cancel button on URL-bar focus.
         * Also specifically summons and dismisses the keyboard.
         */
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


        mWebView.setWebViewClient(new ThisWebViewClient() {

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

    /**
     * Displays a progressbar when a page is loading. The progressbar
     * gets hidden when the page has finished loading.
     * Also updates the URL-bar with the current URL.
     */
    public class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            addWebsite_text.setText(view.getUrl());
            newMainActivity.this.setValue(newProgress);
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

