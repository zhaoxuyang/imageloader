package com.baidu.iknow.imageloader.drawable;

import com.baidu.iknow.imageloader.bitmap.BitmapLock;
import com.baidu.iknow.imageloader.cache.Pools;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Shader;
import android.os.Build;

public class BitmapDrawable extends CustomDrawable {

    private static final int MAX_POOL_SIZE = 40;

    private static final Pools.Pool<BitmapDrawable> sPool = new Pools.SynchronizedPool<BitmapDrawable>(MAX_POOL_SIZE);

    public BitmapShader mShader;

    public Bitmap mBitmap;

    public int mScaledWidth;

    public int mScaledHeight;

    private BitmapDrawable() {

    }

    private void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mScaledWidth = mBitmap.getScaledWidth(CustomDrawable.sTargetDensity);
        mScaledHeight = mBitmap.getScaledHeight(CustomDrawable.sTargetDensity);
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
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return mBitmap.getAllocationByteCount();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                return mBitmap.getByteCount();
            }
        }catch(Exception e){

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

    @Override
    public Bitmap asBitmap() {
        return mBitmap;
    }
}
