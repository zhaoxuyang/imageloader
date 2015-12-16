package com.baidu.iknow.imageloader.cache;

import com.baidu.iknow.imageloader.drawable.SizeDrawable;

/**
 * 
 * @author zhaoxuyang
 * @since 2015-10-9
 */
public class SizeLruCache extends LruCache<UrlSizeKey, SizeDrawable> {

    private static final String TAG = SizeLruCache.class.getSimpleName();

    public SizeLruCache(int size) {
        super(size);
    }

    @Override
    protected int sizeOf(UrlSizeKey key, SizeDrawable value) {
        return value.getSize();
    }

}
