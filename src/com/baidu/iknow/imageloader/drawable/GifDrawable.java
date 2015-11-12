package com.baidu.iknow.imageloader.drawable;

import com.baidu.iknow.imageloader.gif.Gif;
import android.graphics.Bitmap;
import android.support.v4.util.Pools;
import android.support.v4.util.Pools.Pool;

public class GifDrawable extends CustomDrawable {

    private static final int MAX_POOL_SIZE = 40;

    private static final Pool<GifDrawable> sPool = new Pools.SynchronizedPool<GifDrawable>(MAX_POOL_SIZE);

    public Gif mGif;

    private GifDrawable() {

    }

    private void setGif(Gif gif) {
        mGif = gif;
    }

    private static GifDrawable obtain() {
        GifDrawable instance = sPool.acquire();
        return (instance != null) ? instance : new GifDrawable();
    }

    @Override
    public void recycle() {
        if (mGif != null && !mGif.isRecycled) {
            mGif.recycle();
            mGif = null;
        }
        sPool.release(this);
    }

    @Override
    public boolean checkLegal() {
        return mGif != null && !mGif.isRecycled;
    }

    public int updateFrame(int index, Bitmap bm) {
        if (mGif == null) {
            return -1;
        }
        int delay = mGif.getFrame(index, bm);
        return delay;
    }

    public int getFrameCount() {
        if (mGif == null) {
            return -1;
        }
        return mGif.mFrameCount;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getIntrinsicWidth() {
        return mGif.mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mGif.mHeight;
    }

    public static class GifDrawableFactory {
        public static GifDrawable createGifDrawable(Gif gif) {
            if (gif == null || gif.isRecycled) {
                return null;
            }
            GifDrawable drawable = GifDrawable.obtain();
            drawable.setGif(gif);
            return drawable;
        }
    }
}
