package com.baidu.iknow.imageloader.decoder;

import android.util.Log;

import com.baidu.iknow.imageloader.cache.ImageLoaderLog;
import com.baidu.iknow.imageloader.drawable.CustomDrawable;
import com.baidu.iknow.imageloader.drawable.GifDrawable.GifDrawableFactory;
import com.baidu.iknow.imageloader.gif.Gif;
import com.baidu.iknow.imageloader.gif.GifFactory;

public class GifDecoder extends BaseDecoder{
    
    private static final String TAG = GifDecoder.class.getSimpleName();

    @Override
    public boolean checkType(byte[] bytes) {
        String flag = "GIFVER";
        if (bytes.length >= flag.length()) {
            String str = new String(bytes, 0, flag.length());
            if ("GIF87a".equals(str) || "GIF89a".equals(str)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CustomDrawable doDecode(byte[] bytes, DecodeInfo decodeInfo, int viewWidth, int viewHeight) {
        decodeInfo.mGifOptions.inJustDecodeBounds = true;
        decodeInfo.mGifOptions.inSampleSize = 1;
        GifFactory.decodeFromByteArray(bytes, decodeInfo.mGifOptions);
        int bitmapWidth = decodeInfo.mGifOptions.outWidth;
        int bitmapHeight = decodeInfo.mGifOptions.outHeight;
        ImageLoaderLog.d(TAG, "gif width:" + bitmapWidth + ",height:" + bitmapHeight);
        int sampleSize = getSampleSize(decodeInfo, bitmapWidth, bitmapHeight, viewWidth, viewHeight);
        bitmapWidth /= sampleSize;
        bitmapHeight /= sampleSize;
        decodeInfo.mGifOptions.inJustDecodeBounds = false;
        decodeInfo.mGifOptions.inSampleSize = sampleSize;
        Gif gif = GifFactory.decodeFromByteArray(bytes, decodeInfo.mGifOptions);
        if(gif!=null){
            ImageLoaderLog.d(TAG, "after gif width:" + gif.mWidth + ",height:" + gif.mHeight);
        }
        CustomDrawable drawable = GifDrawableFactory.createGifDrawable(gif);
        return drawable;
    }

    
}
