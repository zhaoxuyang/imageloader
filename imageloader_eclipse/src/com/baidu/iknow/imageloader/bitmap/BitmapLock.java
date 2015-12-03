package com.baidu.iknow.imageloader.bitmap;

import android.graphics.Bitmap;

/**
 * 5.0以下，匿名共享内存存储，为了渲染流畅，需要提前lock
 * @author zhaoxuyang
 * @since 2015-10-9
 */
public class BitmapLock {
    static{
        System.loadLibrary("bitmaplock");
    }

    public static native void lockBitmap(Bitmap bm);
    
    public static native void unlockBitmap(Bitmap bm);
}
