package com.baidu.iknow.imageloader.gif;

import java.io.File;

import android.util.Log;

/**
 * GifFactory
 * 
 * @author zhaoxuyang
 * @since 2015-4-7
 */
public class GifFactory {

    static {
        System.loadLibrary("nsgif");
    }

    /**
     * 通过文件解析
     * 
     * @param path
     * @return
     */
    public static synchronized Gif decodeFromFile(String path, GifFactory.Options opts) {
        File file = new File(path);
        if (!file.exists()) {
            Log.d("Giffactory", "file not exist");
            return null;
        }
        return decodeFromFileNative(path, opts);
    }

    /**
     * 通过文件解析
     * 
     * @param path
     * @return
     */
    public static native Gif decodeFromFileNative(String path, GifFactory.Options opts);

    /**
     * 通过bytearray解析
     * 
     * @param bytes
     * @return
     */
    public static synchronized Gif decodeFromByteArray(byte[] bytes, GifFactory.Options opts) {
        return decodeFromByteArrayNative(bytes, opts);
    }

    private static native Gif decodeFromByteArrayNative(byte[] bytes, GifFactory.Options opts);

    public static class Options {
        public boolean inJustDecodeBounds;
        public int inSampleSize = 1;
        public int outWidth;
        public int outHeight;
    }
}
