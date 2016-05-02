package com.example.erlend.nettleser;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private static final String TAG = "Stringtest";
    private WebView mWebView;

    public void clickedButton(View view) {
        WebView webView = (WebView)findViewById(R.id.webview);
        TextView textView = (TextView)findViewById(R.id.website_text);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);

        String URL = textView.getText().toString();

        List<String> urlTest = new ArrayList<String>();
        urlTest.add("http://");

        Pattern pattern = Pattern.compile(String.valueOf(urlTest));
        Matcher matcher = pattern.matcher(URL);

        if (matcher.find()){
            webView.loadUrl(URL);
        }
        else {
            webView.loadUrl("http://"+URL);
        }

        Log.d(TAG, "url ="+ URL);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String webURL ="http://www.google.com";

        Log.i(TAG, "onCreate");
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true); //XSS issues must be resolved, eventually...
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.setVerticalScrollBarEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new ThisWebViewClient());

        /**
         * If there is no saved instance state, then load the default webpage(webURL)
         * This is needed because without it, the app will reload to the default webpage
         * every time the phone is tilted.
         */
        if (savedInstanceState == null)
        {
            mWebView.loadUrl(webURL);
        }

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
