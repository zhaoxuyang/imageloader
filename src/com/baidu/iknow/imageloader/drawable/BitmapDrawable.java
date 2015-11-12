package com.baidu.iknow.imageloader.drawable;

import com.baidu.iknow.imageloader.bitmap.BitmapLock;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.os.Build;
import android.support.v4.util.Pools;
import android.support.v4.util.Pools.Pool;

public class BitmapDrawable extends CustomDrawable {

    private static final int MAX_POOL_SIZE = 40;

    private static final Pool<BitmapDrawable> sPool = new Pools.SynchronizedPool<BitmapDrawable>(MAX_POOL_SIZE);

    public BitmapShader mShader;

    public Bitmap mBitmap;

    public int mScaledWidth;

    public int mScaledHeight;

    private BitmapDrawable() {

    }

    private void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mScaledWidth = mBitmap.getWidth();
        mScaledHeight = mBitmap.getHeight();
        mShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }

    @Override
    public int getIntrinsicWidth() {
        return mScaledWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mScaledHeight;
    }

    private static BitmapDrawable obtain() {
        BitmapDrawable instance = sPool.acquire();
        return (instance != null) ? instance : new BitmapDrawable();
    }

    @Override
    public void recycle() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                BitmapLock.unlockBitmap(mBitmap);
            }
            mBitmap.recycle();
            mBitmap = null;
            mShader = null;
            mScaledWidth = 0;
            mScaledHeight = 0;
        }
        sPool.release(this);
    }

    @Override
    public boolean checkLegal() {
        return mBitmap != null && !mBitmap.isRecycled();
    }

    @SuppressLint("NewApi")
    @Override
    public int getSize() {
        if (mBitmap == null || mBitmap.isRecycled()) {
            return 0;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return mBitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return mBitmap.getByteCount();
        }
        return mBitmap.getRowBytes() * mBitmap.getHeight();
    }

    public static class BitmapDrawableFactory {
        public static BitmapDrawable createBitmapDrawable(Bitmap bitmap) {
            if (bitmap == null || bitmap.isRecycled()) {
                return null;
            }
            BitmapDrawable drawable = BitmapDrawable.obtain();
            drawable.setBitmap(bitmap);
            return drawable;
        }
    }

}
