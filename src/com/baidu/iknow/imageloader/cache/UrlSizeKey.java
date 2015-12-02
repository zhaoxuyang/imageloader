package com.baidu.iknow.imageloader.cache;

import android.support.v4.util.Pools;
import android.support.v4.util.Pools.Pool;
import android.text.TextUtils;

public class UrlSizeKey {

    private static final int MAX_POOL_SIZE = 40;

    private static final Pool<UrlSizeKey> sPool = new Pools.SynchronizedPool<UrlSizeKey>(MAX_POOL_SIZE);

    public String mUrl;

    public int mViewWidth;

    public int mViewHeight;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UrlSizeKey)) {
            return false;
        }
        UrlSizeKey key = (UrlSizeKey) o;
        boolean same = false;
        if (mUrl == null) {
            same = (mUrl == key.mUrl);
        } else {
            same = mUrl.equals(key.mUrl);
        }

        same = same && (mViewWidth == key.mViewWidth);
        same = same && (mViewHeight == key.mViewHeight);
        return same;
    }

    @Override
    public int hashCode() {
        int h = (mUrl == null ? 0 : mUrl.hashCode());
        return h;
    }

    public boolean checkLegal() {
        return !TextUtils.isEmpty(mUrl);
    }

    public static UrlSizeKey obtain() {
        UrlSizeKey instance = sPool.acquire();
        instance = (instance != null) ? instance : new UrlSizeKey();
        return instance;
    }

    public static UrlSizeKey obtain(UrlSizeKey key) {
        UrlSizeKey instance = sPool.acquire();
        instance = (instance != null) ? instance : new UrlSizeKey();
        if(key!=null){
            instance.mUrl = key.mUrl;
            instance.mViewHeight = key.mViewHeight;
            instance.mViewWidth = key.mViewWidth;
        }
        return instance;
    }

    public void recycle() {
        sPool.release(this);
        mUrl = null;
        mViewWidth = 0;
        mViewHeight = 0;
    }

}
