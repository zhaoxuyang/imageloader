package com.baidu.iknow.imageloader.cache;

import android.support.v4.util.Pools;
import android.support.v4.util.Pools.Pool;

/**
 * 
 * @author zhaoxuyang
 * @since 2015-10-13
 */
public class SizeKey {

    private static final int MAX_POOL_SIZE = 40;

    private static final Pool<SizeKey> sPool = new Pools.SynchronizedPool<SizeKey>(MAX_POOL_SIZE);

    public int mWidth;

    public int mHeight;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SizeKey)) {
            return false;
        }
        SizeKey key = (SizeKey) o;
        return key.mWidth == mWidth && key.mHeight == mHeight;
    }

    public boolean isLagerThan(SizeKey key){
        if(key==null){
            return false;
        }
        return mWidth>key.mWidth&&mHeight>key.mHeight;
    }

    @Override
    public int hashCode() {
        return mWidth * mHeight;
    }

    public static SizeKey obtain() {
        SizeKey instance = sPool.acquire();
        return (instance != null) ? instance : new SizeKey();
    }

    public void recycle() {
        sPool.release(this);
        mWidth = 0;
        mHeight = 0;
    }
}
