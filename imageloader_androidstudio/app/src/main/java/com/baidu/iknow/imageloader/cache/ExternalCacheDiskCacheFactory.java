package com.baidu.iknow.imageloader.cache;

import static android.os.Environment.MEDIA_MOUNTED;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * @author zhaoxuyang
 * @since 2015-10-13
 */
public final class ExternalCacheDiskCacheFactory extends DiskLruCacheFactory {

    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    public ExternalCacheDiskCacheFactory(Context context) {
        this(context, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR,
                DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
    }

    public ExternalCacheDiskCacheFactory(Context context, int diskCacheSize) {
        this(context, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR, diskCacheSize);
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
            }
        }
        return appCacheDir;
    }

    public ExternalCacheDiskCacheFactory(final Context context, final String diskCacheName,
                                         int diskCacheSize) {
        super(new CacheDirectoryGetter() {
            @Override
            public File getCacheDirectory() {
                File appCacheDir = null;
                String externalStorageState;
                try {
                    externalStorageState = Environment.getExternalStorageState();
                } catch (NullPointerException e) { // (sh)it happens (Issue #660)
                    externalStorageState = "";
                } catch (IncompatibleClassChangeError e) { // (sh)it happens too (Issue #989)
                    externalStorageState = "";
                }
                if (MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
                    appCacheDir = getExternalCacheDir(context);
                }
                if (appCacheDir == null) {
                    return null;
                }
                if (diskCacheName != null) {
                    return new File(appCacheDir, diskCacheName);
                }
                return appCacheDir;
            }
        }, diskCacheSize);
    }
}
