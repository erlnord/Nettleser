package com.browser.volant;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import okhttp3.HttpUrl;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by larsmartin on 29/05/16.
 * This class was created from this guide @May 29 2016:
 * http://www.hidroh.com/2016/05/19/hacking-up-ad-blocker-android/
 *
 * For documentation see the guide.
 */
public class AdBlocker {
    private static final String AD_HOSTS_FILE = "hosts_block.txt";
    private static final Set<String> AD_HOSTS = new HashSet<>();

    public static void init(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    loadFromAssets(context);
                } catch (IOException e) {
                    Log.e(AdBlocker.class.getSimpleName(), e.toString());
                }
                return null;
            }
        }.execute();
    }

    public static boolean isAd(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (isAdHost((httpUrl != null) ? httpUrl.host() : "")) {
            return true;
        }
        else {
            return false;
        }
    }

    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }

    @WorkerThread
    private static void loadFromAssets(Context context) throws IOException {
        InputStream stream = context.getAssets().open(AD_HOSTS_FILE);
        BufferedSource buffer = Okio.buffer(Okio.source(stream));
        String line;
        while ((line = buffer.readUtf8Line()) != null) {
            AD_HOSTS.add(line);
        }
        buffer.close();
        stream.close();
    }

    /**
     * Recursively walking up sub domain chain until we exhaust or find a match,
     * effectively doing a longest substring matching here
     */
    private static boolean isAdHost(String host) {
        if (TextUtils.isEmpty(host)) {
            return false;
        }
        int index = host.indexOf(".");
        return index >= 0 && (AD_HOSTS.contains(host) ||
                index + 1 < host.length() && isAdHost(host.substring(index + 1)));
    }
}
