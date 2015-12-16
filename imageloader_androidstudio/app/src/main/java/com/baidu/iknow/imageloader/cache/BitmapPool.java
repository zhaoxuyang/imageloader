package com.baidu.iknow.imageloader.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Build;

/**
 * bitmap pool，根据所需大小从池中取能够重用的bitmap gif和5.0以上需要
 * 
 * @author zhaoxuyang
 * @since 2015-10-9
 */
public class BitmapPool {

    private static final String TAG = BitmapPool.class.getSimpleName();

    protected LinkedHashMap<SizeKey, LinkedList<Bitmap>> map;

    protected LinkedList<SizeKey> mSortKeys;

    protected int size;

    protected int maxSize;

    protected String name;

    public BitmapPool(int maxSize, String name) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.name = name;
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<SizeKey, LinkedList<Bitmap>>(0, 0.75f, true);
        this.mSortKeys = new LinkedList<SizeKey>();
    }

    public void resize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }

        synchronized (this) {
            this.maxSize = maxSize;
        }
        trimToSize(maxSize);
    }

    public synchronized Bitmap get(int width, int height, boolean forceFit, boolean autoCreate) {
        SizeKey key = SizeKey.obtain();
        key.mWidth = width;
        key.mHeight = height;
        LinkedList<Bitmap> mapValue;
        mapValue = map.get(key);
        Bitmap bitmap = null;
        if (mapValue != null) {
            Iterator<Bitmap> iter = mapValue.iterator();
            while (iter.hasNext()) {
                bitmap = iter.next();
                size -= safeSizeOf(key, bitmap);
                iter.remove();
                ImageLoaderLog.d(TAG, name + " reuse:" + "w:" + key.mWidth + ",h:" + key.mHeight);
                break;
            }
            if (mapValue.isEmpty()) {
                map.remove(key);
                mSortKeys.remove(key);
            }
        } else if (!forceFit) {
            Iterator<SizeKey> iter = mSortKeys.iterator();
            while (iter.hasNext()) {
                SizeKey sk = iter.next();
                if (sk.isLagerThan(key)) {
                    mapValue = map.get(sk);
                    Iterator<Bitmap> iterb = mapValue.iterator();
                    while (iterb.hasNext()) {
                        bitmap = iterb.next();
                        iterb.remove();
                        size -= safeSizeOf(key, bitmap);
                        ImageLoaderLog.d(TAG, name + " reuse:ow:" + sk.mWidth + ",oh:" + sk.mHeight + ",w:" + key.mWidth + ",h:"
                                + key.mHeight);
                        break;
                    }
                    if (mapValue.isEmpty()) {
                        map.remove(key);
                        iter.remove();
                    }
                    if(bitmap!=null){
                        break;
                    }
                }
            }
        }

        if (bitmap != null) {
            key.recycle();
            return bitmap;
        }

        if (autoCreate) {
            int preSize = preComputeSize(key);

            if (preSize > maxSize) {
                key.recycle();
                return null;
            }

            Bitmap createdValue = create(key);
            if (createdValue == null) {
                key.recycle();
                return null;
            }
            ImageLoaderLog.d(TAG, name + " create:" + "w:" + key.mWidth + ",h:" + key.mHeight);
            key.recycle();
            return createdValue;
        }
        key.recycle();
        return null;
    }

    public synchronized void put(Bitmap value) {
        if (value == null || value.isRecycled()) {
            throw new NullPointerException("key == null || value == null");
        }

        SizeKey key = SizeKey.obtain();
        key.mWidth = value.getWidth();
        key.mHeight = value.getHeight();
        int safeSize = safeSizeOf(key, value);

        if (safeSize > maxSize) {
            value.recycle();
            key.recycle();
            return;
        }

        LinkedList<Bitmap> list = map.get(key);

        if (list == null) {
            list = new LinkedList<Bitmap>();
            map.put(key, list);
            int size = mSortKeys.size();
            int i = 0;
            for (i = 0; i < size; i++) {
                SizeKey sk = mSortKeys.get(i);
                if (sk.isLagerThan(key)) {
                    break;
                }
            }
            mSortKeys.add(i, key);
        }

        ImageLoaderLog.d(TAG, name + " put");
        size += safeSize;
        list.add(value);
        trimToSize(maxSize);
        return;
    }

    public void trimToSize(int maxSize) {
        ImageLoaderLog.d(TAG, name+" size:" + size + ",maxsize:" + maxSize);
        Iterator<Map.Entry<SizeKey, LinkedList<Bitmap>>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            SizeKey key;
            LinkedList<Bitmap> value;
            synchronized (this) {
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw new IllegalStateException(getClass().getName()
                            + ".sizeOf() is reporting inconsistent results!");
                }

                ImageLoaderLog.d(TAG, "size:" + size + ",maxsize:" + maxSize);
                if (size <= maxSize || map.isEmpty()) {
                    break;
                }

                Map.Entry<SizeKey, LinkedList<Bitmap>> toEvict = iter.next();
                key = toEvict.getKey();
                value = toEvict.getValue();
                Iterator<Bitmap> iterb = value.iterator();
                while (iterb.hasNext()) {
                    if (size <= maxSize) {
                        break;
                    }
                    Bitmap bm = iterb.next();
                    int safeSize = safeSizeOf(key, bm);
                    size -= safeSize;
                    iterb.remove();
                    if (bm != null && !bm.isRecycled()) {
                        bm.recycle();
                        ImageLoaderLog.d(TAG, "recycle");
                    }
                }

                if (value.isEmpty()) {
                    iter.remove();
                    mSortKeys.remove(key);
                    key.recycle();
                }

            }

        }
    }

    protected int preComputeSize(SizeKey key) {
        return key.mWidth * key.mHeight * 4;
    }

    protected Bitmap create(SizeKey key) {
        return Bitmap.createBitmap(key.mWidth, key.mHeight, Config.ARGB_8888);
    }

    private int safeSizeOf(SizeKey key, Bitmap value) {
        int result = sizeOf(key, value);
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + key + "=" + value);
        }
        return result;
    }

    @SuppressLint("NewApi")
    protected int sizeOf(SizeKey key, Bitmap value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return value.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return value.getByteCount();
        }
        return value.getRowBytes() * value.getHeight();
    }

}
