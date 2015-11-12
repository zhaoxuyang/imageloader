/*
 * Copyright (c) 2013. Bump Technologies Inc. All Rights Reserved.
 */

package com.baidu.iknow.imageloader.cache;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * 
 * @author zhaoxuyang
 * @since 2015-10-13
 */
public class DiskLruCacheWrapper implements DiskCache {
    private static final String TAG = "DiskLruCacheWrapper";

    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    private static DiskLruCacheWrapper wrapper = null;

    private final SafeKeyGenerator safeKeyGenerator;
    private final File directory;
    private final int maxSize;
    private final DiskCacheWriteLocker writeLocker = new DiskCacheWriteLocker();
    private DiskLruCache diskLruCache;

    public static synchronized DiskCache get(File directory, int maxSize) {
        if (wrapper == null) {
            wrapper = new DiskLruCacheWrapper(directory, maxSize);
        }
        return wrapper;
    }

    protected DiskLruCacheWrapper(File directory, int maxSize) {
        this.directory = directory;
        this.maxSize = maxSize;
        this.safeKeyGenerator = new SafeKeyGenerator();
    }

    private synchronized DiskLruCache getDiskCache() throws IOException {
        if (diskLruCache == null) {
            diskLruCache = DiskLruCache.open(directory, APP_VERSION, VALUE_COUNT, maxSize);
        }
        return diskLruCache;
    }

    @Override
    public DiskLruCache.Snapshot get(String key) {
        String safeKey = safeKeyGenerator.getSafeKey(key);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Get: Obtained: " + safeKey + " for for Key: " + key);
        }
        DiskLruCache.Snapshot result = null;
        try {
            final DiskLruCache.Snapshot value = getDiskCache().get(safeKey);
            if (value != null) {
                result = value;
            }
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unable to get from disk cache", e);
            }
        }
        return result;
    }

    @Override
    public void put(String key, InputStream is) {
        writeLocker.acquire(key);
        try {
            String safeKey = safeKeyGenerator.getSafeKey(key);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Put: Obtained: " + safeKey + " for for Key: " + key);
            }
            try {
                DiskLruCache diskCache = getDiskCache();
                DiskLruCache.Snapshot current = diskCache.get(safeKey);
                if (current != null) {
                    return;
                }

                DiskLruCache.Editor editor = diskCache.edit(safeKey);
                if (editor == null) {
                    throw new IllegalStateException("Had two simultaneous puts for: " + safeKey);
                }
                OutputStream os = null;
                try {
                    os = editor.newOutputStream(0);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = is.read(buffer)) > 0) {
                        os.write(buffer, 0, len);
                    }
                    os.flush();
                    editor.commit();
                } finally {
                    editor.abortUnlessCommitted();
                    if (os != null) {
                        os.close();
                        os = null;
                    }
                }
            } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unable to put to disk cache", e);
                }
            }
        } finally {
            writeLocker.release(key);
        }
    }

    @Override
    public void delete(String key) {
        String safeKey = safeKeyGenerator.getSafeKey(key);
        try {
            getDiskCache().remove(safeKey);
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unable to delete from disk cache", e);
            }
        }
    }

    @Override
    public synchronized void clear() {
        try {
            getDiskCache().delete();
            resetDiskCache();
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Unable to clear disk cache", e);
            }
        }
    }

    private synchronized void resetDiskCache() {
        diskLruCache = null;
    }

}
