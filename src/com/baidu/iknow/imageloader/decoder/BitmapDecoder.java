package com.baidu.iknow.imageloader.decoder;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.util.Log;

import com.baidu.iknow.imageloader.bitmap.BitmapLock;
import com.baidu.iknow.imageloader.cache.ImageLoaderLog;
import com.baidu.iknow.imageloader.drawable.BitmapDrawable;
import com.baidu.iknow.imageloader.drawable.CustomDrawable;
import com.baidu.iknow.imageloader.drawable.BitmapDrawable.BitmapDrawableFactory;
import com.baidu.iknow.imageloader.request.ImageLoader;

public class BitmapDecoder extends BaseDecoder {

    private static final String TAG = BitmapDecoder.class.getSimpleName();

    @Override
    public boolean checkType(byte[] bytes) {
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public CustomDrawable doDecode(byte[] bytes, DecodeInfo decodeInfo, int viewWidth, int viewHeight) {
        Options opts = decodeInfo.mBitmapOptions;
        opts.inJustDecodeBounds = true;
        opts.inSampleSize = 1;
        opts.outWidth = -1;
        opts.outHeight = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            opts.inBitmap = null;
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        int bitmapWidth = opts.outWidth;
        int bitmapHeight = opts.outHeight;
        ImageLoaderLog.d(TAG, "bitmap width:" + bitmapWidth + ",height:" + bitmapHeight);
        if (bitmapWidth <= 0 || bitmapHeight <= 0) {
            return null;
        }
        int sampleSize = 1;
        if (decodeInfo.mScaleToFitView) {
            while (bitmapWidth / 2 >= viewWidth || bitmapHeight / 2 >= viewHeight) { // ||
                bitmapWidth /= 2;
                bitmapHeight /= 2;
                sampleSize *= 2;
            }
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = sampleSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            opts.inMutable = true;
            opts.inBitmap = ImageLoader.getInstance().mBitmapPool.get(bitmapWidth, bitmapHeight, false, false);
        }

        Bitmap bm = null;
        try{
            bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        }catch(Exception e){
            ImageLoaderLog.d(TAG, "bitmap decode error");
            e.printStackTrace();
            return null;
        }

        if (bm != null) {
            ImageLoaderLog.d(TAG, "after bitmap width:" + bm.getWidth() + ",height:" + bm.getHeight());
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                BitmapLock.lockBitmap(bm);
            }
        }
        BitmapDrawable bd = BitmapDrawableFactory.createBitmapDrawable(bm);
        return bd;
    }

}
