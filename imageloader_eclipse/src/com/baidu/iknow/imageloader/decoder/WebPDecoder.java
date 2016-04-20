package com.baidu.iknow.imageloader.decoder;

import android.graphics.Bitmap;
import android.os.Build;

import java.nio.ByteBuffer;

import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.drawable.BitmapDrawable;
import com.baidu.iknow.imageloader.drawable.BitmapDrawable.BitmapDrawableFactory;
import com.baidu.iknow.imageloader.drawable.CustomDrawable;
import com.baidu.iknow.imageloader.drawable.SizeDrawable;

public class WebPDecoder extends BaseDecoder {

    @Override
    public boolean checkType(byte[] bytes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false;
        }
        return WebPFactory.checkTypeNative(bytes, bytes.length);
    }

    @Override
    public CustomDrawable doDecode(byte[] bytes, DecodeInfo decodeInfo, UrlSizeKey key, int from) {
        if (bytes == null) {
            return null;
        }
        decodeInfo.mWebPOptions.inJustDecodeBounds = false;
        decodeInfo.mWebPOptions.inSampleSize = 1;
        byte[] buffer = WebPFactory.decodeByteArrayNative(bytes, bytes.length, decodeInfo.mWebPOptions);
        if (buffer != null) {
            int[] pixels = new int[buffer.length / 4];
            ByteBuffer.wrap(buffer).asIntBuffer().get(pixels);
            Bitmap bitmap = Bitmap.createBitmap(pixels, decodeInfo.mWebPOptions.outWidth,
                    decodeInfo.mWebPOptions.outHeight, Bitmap.Config.ARGB_8888);
            BitmapDrawable drawable = BitmapDrawableFactory.createBitmapDrawable(bitmap);
            return drawable;
        }
        return null;
    }

    @Override
    public SizeDrawable getSize(byte[] bytes, DecodeInfo decodeInfo) {
        if (bytes == null) {
            return null;
        }
        decodeInfo.mWebPOptions.inJustDecodeBounds = true;
        decodeInfo.mWebPOptions.inSampleSize = 1;
        WebPFactory.decodeByteArrayNative(bytes, bytes.length, decodeInfo.mWebPOptions);
        SizeDrawable sizeDrawable = new SizeDrawable(decodeInfo.mWebPOptions.outWidth,
                decodeInfo.mWebPOptions.outHeight);
        return sizeDrawable;
    }

}
