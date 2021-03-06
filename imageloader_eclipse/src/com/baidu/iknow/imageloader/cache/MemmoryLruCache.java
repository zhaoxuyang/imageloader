package com.baidu.iknow.imageloader.cache;

import android.os.Build;

import com.baidu.iknow.imageloader.drawable.BitmapDrawable;
import com.baidu.iknow.imageloader.drawable.CustomDrawable;
import com.baidu.iknow.imageloader.request.ImageLoader;

/**
 * 
 * @author zhaoxuyang
 * @since 2015-10-9
 */
public class MemmoryLruCache extends LruCache<UrlSizeKey, CustomDrawable> {

    private static final String TAG = MemmoryLruCache.class.getSimpleName();
    
    public MemmoryLruCache(int size) {
        super(size);
    }



    @Override
    protected void entryRemoved(boolean evicted, UrlSizeKey key, CustomDrawable oldValue, CustomDrawable newValue) {

        ImageLoaderLog.d(TAG, "entry remove");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(oldValue instanceof BitmapDrawable && oldValue.needRecycleUse){
                BitmapDrawable bd = (BitmapDrawable) oldValue;
                ImageLoader.getInstance().mBitmapPool.put(bd.mBitmap);
                ImageLoaderLog.d(TAG, "reuse");
            }
        } else {
            ImageLoaderLog.d(TAG, "recycle");
            oldValue.recycle();
        }
        
    }

    @Override
    protected int sizeOf(UrlSizeKey key, CustomDrawable value) {
        return value.getSize();
    }

}
