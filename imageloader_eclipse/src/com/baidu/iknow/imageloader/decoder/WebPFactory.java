package com.baidu.iknow.imageloader.decoder;

import android.graphics.Bitmap;

public class WebPFactory {

    static {
        System.loadLibrary("webp_evme");
    }

    public static native boolean checkTypeNative(byte[] bytes, long length);

    public static native byte[] decodeByteArrayNative(byte[] bytes, long length, Options options);

    public static class Options {
        public boolean inJustDecodeBounds;
        public int inSampleSize = 1;
        public int outWidth;
        public int outHeight;
    }
}
