package com.baidu.iknow.imageloader.decoder;

import android.backport.webp.WebPFactory;
import android.graphics.Bitmap;
import android.os.Build;

import com.baidu.iknow.imageloader.cache.ImageLoaderLog;
import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.drawable.BitmapDrawable;
import com.baidu.iknow.imageloader.drawable.BitmapDrawable.BitmapDrawableFactory;
import com.baidu.iknow.imageloader.drawable.CustomDrawable;
import com.baidu.iknow.imageloader.drawable.SizeDrawable;

public class WebPDecoder extends BaseDecoder {

    private static final String TAG = WebPDecoder.class.getSimpleName();

    @Override
    public boolean checkType(byte[] bytes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false;
        }
        boolean res = WebPFactory.nativeCheckType(bytes);
        ImageLoaderLog.d(TAG, "checkType:" + res);
        return res;
    }

    @Override
    public CustomDrawable doDecode(byte[] bytes, DecodeInfo decodeInfo, UrlSizeKey key, int from) {
        if (bytes == null) {
            return null;
        }

        Bitmap bm = WebPFactory.nativeDecodeByteArray(bytes, decodeInfo.mBitmapOptions);
        if (bm != null) {
            BitmapDrawable drawable = BitmapDrawableFactory.createBitmapDrawable(bm);
            return drawable;
        }
        return null;
    }

    @Override
    public SizeDrawable getSize(byte[] bytes, DecodeInfo decodeInfo) {
        if (bytes == null) {
            return null;
        }
        decodeInfo.mBitmapOptions.inJustDecodeBounds = true;
        decodeInfo.mBitmapOptions.inSampleSize = 1;
        WebPFactory.nativeDecodeByteArray(bytes, decodeInfo.mBitmapOptions);
        SizeDrawable sizeDrawable = new SizeDrawable(decodeInfo.mBitmapOptions.outWidth,
                decodeInfo.mBitmapOptions.outHeight);
        return sizeDrawable;
    }

}
