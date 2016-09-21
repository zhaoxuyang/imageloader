package com.baidu.iknow.imageloader.cache;

import android.support.annotation.Nullable;

import java.io.File;
import java.io.InputStream;

/**
 * @author zhaoxuyang
 * @since 2015-10-13
 */
public interface DiskCache {

    interface Factory {

        int DEFAULT_DISK_CACHE_SIZE = 50 * 1024 * 1024;
        String DEFAULT_DISK_CACHE_DIR = "image_loader_disk_cache";

        @Nullable
        DiskCache build();
    }

    @Nullable
    DiskLruCache.Snapshot get(String key);

    void put(String key, InputStream is);

    void delete(String key);

    File getFile(String key, int i);

    void clear();
}
