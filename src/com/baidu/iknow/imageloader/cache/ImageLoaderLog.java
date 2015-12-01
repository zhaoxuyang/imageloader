package com.baidu.iknow.imageloader.cache;

import android.util.Log;

/**
 * Created by zhaoxuyang on 15/11/27.
 */
public class ImageLoaderLog {

    public static boolean DEBUG = false;

    private ImageLoaderLog() {

    }

    public static int d(String tag, String msg) {
        if (!DEBUG) {
            return 0;
        }
        return Log.d(tag, msg);
    }

    public static int v(String tag, String msg) {
        if (!DEBUG) {
            return 0;
        }
        return Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (!DEBUG) {
            return 0;
        }
        return Log.v(tag, msg, tr);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (!DEBUG) {
            return 0;
        }
        return Log.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        if (!DEBUG) {
            return 0;
        }
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (!DEBUG) {
            return 0;
        }
        return Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        if (!DEBUG) {
            return 0;
        }
        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (!DEBUG) {
            return 0;
        }
        return Log.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        if (!DEBUG) {
            return 0;
        }
        return Log.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        if (!DEBUG) {
            return 0;
        }
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (!DEBUG) {
            return 0;
        }
        return Log.e(tag, msg, tr);
    }
}
