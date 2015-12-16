package com.baidu.iknow.imageloader.cache;


import com.baidu.iknow.imageloader.drawable.FileDrawable;

/**
 * 
 * @author zhaoxuyang
 * @since 2015-10-9
 */
public class FileLruCache extends LruCache<UrlSizeKey, FileDrawable> {

    private static final String TAG = FileLruCache.class.getSimpleName();

    public FileLruCache(int size) {
        super(size);
    }

    @Override
    protected int sizeOf(UrlSizeKey key, FileDrawable value) {
        return value.getSize();
    }

}
