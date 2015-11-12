package com.baidu.iknow.imageloader.cache;

import android.content.Context;

import java.io.File;

/**
 * 
 * @author zhaoxuyang
 * @since 2015-10-13
 */
public final class ExternalCacheDiskCacheFactory extends DiskLruCacheFactory {

  public ExternalCacheDiskCacheFactory(Context context) {
    this(context, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR,
        DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
  }

  public ExternalCacheDiskCacheFactory(Context context, int diskCacheSize) {
    this(context, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR, diskCacheSize);
  }

  public ExternalCacheDiskCacheFactory(final Context context, final String diskCacheName,
      int diskCacheSize) {
    super(new CacheDirectoryGetter() {
      @Override
      public File getCacheDirectory() {
        File cacheDirectory = context.getExternalCacheDir();
        if (cacheDirectory == null) {
          return null;
        }
        if (diskCacheName != null) {
          return new File(cacheDirectory, diskCacheName);
        }
        return cacheDirectory;
      }
    }, diskCacheSize);
  }
}
