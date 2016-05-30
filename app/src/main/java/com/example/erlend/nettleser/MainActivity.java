package com.example.erlend.nettleser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Dette er bare en comment for å kunne pushe v1.
//Forresten Steffen, KOMMENTER KODEN DIN PLS :) 
public class MainActivity extends Activity {

    private static final String TAG = "Stringtest";
    private WebView mWebView;
    AutoCompleteTextView acTextView;
    String[] urls = {"https://www.youtube.com", //The list of known URLS that will auto complete.
                     "https://www.google.com",
                     "https://www.facebook.com",
                     "https://www.gmail.com",
                     "https://www.hotmail.com"
                     "https://www.uia.no/student"};

    public void clickedButton(View view) {
        WebView webView = (WebView)findViewById(R.id.webview);
        TextView textView = (TextView)findViewById(R.id.autoCompleteTextView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);

        String URL = textView.getText().toString();

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
            webView.loadUrl(URL);
        }else if (!matcher1.find()){
            webView.loadUrl(Shttp+URL);
        }else if (!matcher2.find()){
            webView.loadUrl(Shttp+URL);
        }
        Log.d(TAG, "url ="+ URL);
    }

    public void changeActivity() {
        Intent startNewActivity = new Intent(this, newMainActivity.class);
        startNewActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startNewActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String webURL ="http://www.google.com";
        acTextView = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_item, urls);
        acTextView.setThreshold(13);
        acTextView.setAdapter(adapter);
        acTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_ENTER:
                        case KeyEvent.KEYCODE_NUMPAD_ENTER:
                            clickedButton(view);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        final Button popupButton = (Button)findViewById(R.id.set_button) ;
        popupButton.setOnClickListener(new View.OnClickListener(){
            @Override
                public void onClick(View z) {
                    PopupMenu popupMenu = new PopupMenu(getApplicationContext(),z);

                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.show();
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
