package com.baidu.iknow.imageloader.cache;

import java.io.File;

/**
 * 
 * @author zhaoxuyang
 * @since 2015-10-13
 */
public class DiskLruCacheFactory implements DiskCache.Factory {
  private final int diskCacheSize;
  private final CacheDirectoryGetter cacheDirectoryGetter;

  public interface CacheDirectoryGetter {
    File getCacheDirectory();
  }

  public DiskLruCacheFactory(final String diskCacheFolder, int diskCacheSize) {
    this(new CacheDirectoryGetter() {
      @Override
      public File getCacheDirectory() {
        return new File(diskCacheFolder);
      }
    }, diskCacheSize);
  }

  public DiskLruCacheFactory(final String diskCacheFolder, final String diskCacheName,
      int diskCacheSize) {
    this(new CacheDirectoryGetter() {
      @Override
      public File getCacheDirectory() {
        return new File(diskCacheFolder, diskCacheName);
      }
    }, diskCacheSize);
  }

  public DiskLruCacheFactory(CacheDirectoryGetter cacheDirectoryGetter, int diskCacheSize) {
    this.diskCacheSize = diskCacheSize;
    this.cacheDirectoryGetter = cacheDirectoryGetter;
  }

  @Override
  public DiskCache build() {
    File cacheDir = cacheDirectoryGetter.getCacheDirectory();

    if (cacheDir == null) {
      return null;
    }

    if (!cacheDir.mkdirs() && (!cacheDir.exists() || !cacheDir.isDirectory())) {
      return null;
    }

    return DiskLruCacheWrapper.get(cacheDir, diskCacheSize);
  }
}
