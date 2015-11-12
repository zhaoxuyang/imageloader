package com.baidu.iknow.imageloader.request;

import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.baidu.iknow.imageloader.cache.UrlSizeKey;

/**
 * 从网络拉取图片
 * 
 * @author zhaoxuyang
 * @since 2015-10-12
 */
public class HttpUrlFetcher implements DataFetcher<InputStream> {

    private static final String TAG = "HttpUrlFetcher";
    private static final int MAXIMUM_REDIRECTS = 5;
    private static final int DEFAULT_TIMEOUT_MS = 2500;
    private final UrlSizeKey mKey;
    private final int timeout;

    private HttpURLConnection urlConnection;
    private InputStream stream;
    private volatile boolean isCancelled;

    public HttpUrlFetcher(UrlSizeKey url) {
        this(url, DEFAULT_TIMEOUT_MS);
    }

    HttpUrlFetcher(UrlSizeKey url, int timeout) {
        this.mKey = url;
        this.timeout = timeout;
    }

    @Override
    public void loadData(DataCallback<? super InputStream> callback) {
        Uri uri = Uri.parse(mKey.mUrl.toString());
        String scheme = uri.getScheme();
        String uriString = mKey.mUrl.toString();
        int redirects = 0;
        try {
            while (redirects < MAXIMUM_REDIRECTS) {
                String nextUriString;
                String nextScheme;
                InputStream is;

                URL url = new URL(uriString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(timeout);
                urlConnection.setReadTimeout(timeout);
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.connect();
                if (isCancelled) {
                    return;
                }
                final int statusCode = urlConnection.getResponseCode();
                if (statusCode / 100 == 2) {
                    is = urlConnection.getInputStream();
                    callback.onDataReady(is);
                    break;
                } else if (statusCode / 100 == 3) {
                    String redirectUrlString = urlConnection.getHeaderField("Location");
                    if (TextUtils.isEmpty(redirectUrlString)) {
                        throw new Exception("Received empty or null redirect url");
                    }
                    nextUriString = urlConnection.getHeaderField("Location");
                    nextScheme = (nextUriString == null) ? null : Uri.parse(nextUriString).getScheme();
                    if (nextUriString == null || nextScheme.equals(scheme)) {
                        break;
                    }
                    redirects++;
                    uriString = nextUriString;
                    scheme = nextScheme;
                } else if (statusCode == -1) {
                    throw new Exception(statusCode + "");
                } else {
                    throw new Exception(urlConnection.getResponseMessage());
                }
            }
            if (redirects >= MAXIMUM_REDIRECTS) {
                throw new Exception("Too many (> " + MAXIMUM_REDIRECTS + ") redirects!");
            }
        } catch (Exception e) {
            // error
            callback.onLoadFailed(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override
    public void cleanup() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    @Override
    public void cancel() {
        isCancelled = true;
    }

    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }
}
