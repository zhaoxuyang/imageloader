package com.baidu.iknow.imageloader.request;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.baidu.iknow.imageloader.cache.BitmapPool;
import com.baidu.iknow.imageloader.cache.DiskCache;
import com.baidu.iknow.imageloader.cache.ExternalCacheDiskCacheFactory;
import com.baidu.iknow.imageloader.cache.InternalCacheDiskCacheFactory;
import com.baidu.iknow.imageloader.cache.MemmoryLruCache;
import com.baidu.iknow.imageloader.cache.MemorySizeCalculator;
import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.decoder.DecodeInfo;
import com.baidu.iknow.imageloader.drawable.CustomDrawable;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

/**
 * 图片加载器
 * 
 * @author zhaoxuyang
 * @since 2015-10-12
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    private static final int MAX_RUNNING_TASK = 5;

    private static ImageLoader mInstance;

    public BitmapPool mGifBitmapPool;

    public BitmapPool mBitmapPool;

    public MemmoryLruCache mMemmoryCache;

    public DiskCache mDiskLruCache;

    private ImageLoadingListener mEmptyListener = new SimpleImageLoadingListener();

    private BlockingQueue<ImageLoadTask> mWaitingQuene = new LinkedBlockingQueue<ImageLoadTask>();

    private BlockingQueue<ImageLoadTask> mRunningQuene = new LinkedBlockingQueue<ImageLoadTask>();

    public BlockingQueue<String> mFailedQuene = new LinkedBlockingQueue<String>(128);

    private ConcurrentHashMap<UrlSizeKey, ImageLoadTask> mTasks = new ConcurrentHashMap<UrlSizeKey, ImageLoadTask>();

    private ConcurrentHashMap<UrlSizeKey, HashSet<ImageLoadingListener>> mListeners = new ConcurrentHashMap<UrlSizeKey, HashSet<ImageLoadingListener>>();

    private ImageLoadingListener mDispatchListener = new ImageLoadingListener() {

        @Override
        public void onLoadingStarted(UrlSizeKey key) {
            HashSet<ImageLoadingListener> listenersSet = mListeners.get(key);
            if (listenersSet != null) {
                Iterator<ImageLoadingListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageLoadingListener listener = iter.next();
                    listener.onLoadingStarted(key);
                }
            }
        }

        @Override
        public void onLoadingFailed(UrlSizeKey key, Exception failReason) {
            ImageLoadTask task = mTasks.remove(key);
            mRunningQuene.remove(task);
            HashSet<ImageLoadingListener> listenersSet = mListeners.get(key);
            if (listenersSet != null) {
                Iterator<ImageLoadingListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageLoadingListener listener = iter.next();
                    listener.onLoadingFailed(key, failReason);
                }
                mListeners.remove(key);
                Log.d(TAG, "failed URL:" + key.mUrl + ",width:" + key.mViewWidth + ",height:" + key.mViewHeight);
            }
            mFailedQuene.offer(key.mUrl);
            runNext();
        }

        @Override
        public void onLoadingComplete(UrlSizeKey key, CustomDrawable drawable, boolean fromMemmoryCache) {
            mMemmoryCache.put(key, drawable);
            ImageLoadTask task = mTasks.remove(key);
            mRunningQuene.remove(task);
            HashSet<ImageLoadingListener> listenersSet = mListeners.get(key);
            if (listenersSet != null) {
                Iterator<ImageLoadingListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageLoadingListener listener = iter.next();
                    listener.onLoadingComplete(key, drawable, fromMemmoryCache);
                }
                mListeners.remove(key);
                Log.d(TAG, "complete URL:" + key.mUrl + ",width:" + key.mViewWidth + ",height:" + key.mViewHeight);
            }
            runNext();
        }

        @Override
        public void onLoadingCancelled(UrlSizeKey key) {
            ImageLoadTask task = mTasks.remove(key);
            mRunningQuene.remove(task);
            mWaitingQuene.remove(task);
            HashSet<ImageLoadingListener> listenersSet = mListeners.get(key);
            if (listenersSet != null) {
                Iterator<ImageLoadingListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageLoadingListener listener = iter.next();
                    listener.onLoadingCancelled(key);
                }
                mListeners.remove(key);
            }
            runNext();
        }
    };

    private ImageLoader() {
    }

    public static synchronized ImageLoader getInstance() {
        if (mInstance == null) {
            mInstance = new ImageLoader();
        }
        return mInstance;
    }

    public void init(Context context) {
        MemorySizeCalculator memorySizeCalator = new MemorySizeCalculator.Builder(context).build();
        mGifBitmapPool = new BitmapPool(memorySizeCalator.getGifBitmapPoolSize(),"gif");
        mBitmapPool = new BitmapPool(memorySizeCalator.getBitmapPoolSize(),"bitmap");
        mMemmoryCache = new MemmoryLruCache(memorySizeCalator.getMemoryCacheSize());
        boolean hasExtrenalStorage = true;
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File sdCard = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(sdCard.getPath());
            long blockSize = stat.getBlockSize();
            long available = stat.getAvailableBlocks();
            long size = blockSize * available;
            if (size < ExternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_SIZE) {
                hasExtrenalStorage = false;
            }
        }

        if (hasExtrenalStorage) {
            mDiskLruCache = new ExternalCacheDiskCacheFactory(context).build();
        } else {
            mDiskLruCache = new InternalCacheDiskCacheFactory(context).build();
        }
    }

    public void load(String url, int width, int height, DecodeInfo decodeInfo, ImageLoadingListener listener,
            boolean isFastScroll) {

        if (decodeInfo == null) {
            decodeInfo = new DecodeInfo.DecodeInfoBuilder().build();
        }
        
        if (listener == null) {
            listener = mEmptyListener;
        }

        if (TextUtils.isEmpty(url)) {
            return;
        }

        UrlSizeKey key = obtain(url, width, height);

        CustomDrawable drawable = mMemmoryCache.get(key);
        if (drawable != null) {
            listener.onLoadingStarted(key);
            listener.onLoadingComplete(key, drawable, true);
            release(key);
            return;
        }

        if (isFastScroll) {
            return;
        }

        if (mTasks.containsKey(key)) {
            if (mListeners.containsKey(key)) {
                HashSet<ImageLoadingListener> listeners = mListeners.get(key);
                listeners.add(listener);
            }
            release(key);
            return;
        }

        Log.d(TAG, "startload URL:" + url + ",width:" + width + ",height:" + height);

        ImageLoadTask task = new ImageLoadTask();
        task.mImageLoadingListener = mDispatchListener;
        task.mKey = key;
        task.mDecodeInfo = decodeInfo;
        mTasks.put(key, task);
        mWaitingQuene.offer(task);
        HashSet<ImageLoadingListener> listeners = new HashSet<ImageLoadingListener>();
        listeners.add(listener);
        mListeners.put(key, listeners);
        runNext();
    }

    private void runNext() {
        while (mRunningQuene.size() < MAX_RUNNING_TASK && mWaitingQuene.size() > 0) {
            ImageLoadTask task = mWaitingQuene.poll();
            mRunningQuene.offer(task);
            task.execute();
        }
    }

    public void cancelLoad(String url, int width, int height, ImageLoadingListener listener) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        mFailedQuene.remove(url);
        UrlSizeKey key = obtain(url, width, height);
        if (mTasks.containsKey(key)) {
            Log.d(TAG, "cancel waiting URL:" + url + ",width:" + width + ",height:" + height);
            ImageLoadTask task = mTasks.get(key);
            if (mWaitingQuene.contains(task)) {
                HashSet<ImageLoadingListener> listeners = mListeners.get(key);
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    mWaitingQuene.remove(task);
                    mTasks.remove(key);
                    mListeners.remove(key);
                }
            } else {
                Log.d(TAG, "cancel running URL:" + url + ",width:" + width + ",height:" + height);
                HashSet<ImageLoadingListener> listeners = mListeners.get(key);
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    boolean res = task.cancel(true);
                    if (!res) {
                        mRunningQuene.remove(task);
                        mTasks.remove(key);
                        mListeners.remove(key);
                    }
                }
            }

        }
        release(key);

    }

    public CustomDrawable getMemmory(String url, int width, int height) {
        UrlSizeKey key = obtain(url, width, height);
        CustomDrawable drawable = mMemmoryCache.get(key);
        key.recycle();
        return drawable;
    }

    private UrlSizeKey obtain(String url, int width, int height) {
        UrlSizeKey key = UrlSizeKey.obtain();
        key.mUrl = url;
        key.mViewWidth = width;
        key.mViewHeight = height;
        return key;
    }

    private void release(UrlSizeKey key) {
        key.recycle();
    }

}
