package com.baidu.iknow.imageloader.request;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.baidu.iknow.imageloader.cache.BitmapPool;
import com.baidu.iknow.imageloader.cache.ByteArrayPool;
import com.baidu.iknow.imageloader.cache.DiskCache;
import com.baidu.iknow.imageloader.cache.ExternalCacheDiskCacheFactory;
import com.baidu.iknow.imageloader.cache.FileLruCache;
import com.baidu.iknow.imageloader.cache.ImageLoaderLog;
import com.baidu.iknow.imageloader.cache.InternalCacheDiskCacheFactory;
import com.baidu.iknow.imageloader.cache.MemmoryLruCache;
import com.baidu.iknow.imageloader.cache.MemorySizeCalculator;
import com.baidu.iknow.imageloader.cache.SizeLruCache;
import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.drawable.CustomDrawable;
import com.baidu.iknow.imageloader.drawable.FileDrawable;
import com.baidu.iknow.imageloader.drawable.SizeDrawable;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

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

    public ByteArrayPool mByteArrayPool;

    public DiskCache mDiskLruCache;

    private ImageLoadingListener mEmptyListener = new SimpleImageLoadingListener();

    private BlockingQueue<ImageLoadTask> mWaitingQuene = new LinkedBlockingQueue<ImageLoadTask>();

    private BlockingQueue<ImageLoadTask> mRunningQuene = new LinkedBlockingQueue<ImageLoadTask>();

    public BlockingQueue<String> mFailedQuene = new LinkedBlockingQueue<String>(128);

    private ConcurrentHashMap<UrlSizeKey, ImageLoadTask> mTasks = new ConcurrentHashMap<UrlSizeKey, ImageLoadTask>();

    private ConcurrentHashMap<UrlSizeKey, HashSet<ImageLoadingListener>> mListeners =
            new ConcurrentHashMap<UrlSizeKey, HashSet<ImageLoadingListener>>();

    private ImageLoadingListener mDispatchListener = new ImageLoadingListener() {

        @Override
        public void onLoadingStarted(UrlSizeKey key) {
            HashSet<ImageLoadingListener> listenersSet = mListeners.get(key);
            if (listenersSet != null) {
                Iterator<ImageLoadingListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageLoadingListener listener = iter.next();
                    if (listener != null) {
                        listener.onLoadingStarted(key);
                    }
                }
            }

        }

        @Override
        public void onLoadingFailed(UrlSizeKey key, Exception failReason) {
            ImageLoadTask task = mTasks.remove(key);
            mRunningQuene.remove(task);
            HashSet<ImageLoadingListener> listenersSet = mListeners.remove(key);
            if (task.retryCount < ImageLoadTask.RETYR_MAX_COUNT) {
                ImageLoaderLog.d(TAG,
                        "failed retry URL:" + key.mUrl + ",width:" + key.mViewWidth + ",height:" + key.mViewHeight);
                task.retryCount++;
                ImageLoadTask newtask = new ImageLoadTask(task);
                mTasks.put(newtask.mKey, newtask);
                mWaitingQuene.add(newtask);
                mListeners.put(newtask.mKey, listenersSet);
            } else {
                if (listenersSet != null) {
                    Iterator<ImageLoadingListener> iter = listenersSet.iterator();
                    while (iter.hasNext()) {
                        ImageLoadingListener listener = iter.next();
                        if (listener != null) {
                            listener.onLoadingFailed(key, failReason);
                        }
                    }
                    ImageLoaderLog.d(TAG,
                            "failed URL:" + key.mUrl + ",width:" + key.mViewWidth + ",height:" + key.mViewHeight);
                }
                mFailedQuene.offer(key.mUrl);
            }
            runNext();
        }

        @Override
        public void onLoadingComplete(UrlSizeKey key, CustomDrawable drawable, boolean fromMemmoryCache) {
            ImageLoadTask task = mTasks.remove(key);
            mRunningQuene.remove(task);
            HashSet<ImageLoadingListener> listenersSet = mListeners.remove(key);
            if (drawable.checkLegal()) {
                if (!fromMemmoryCache) {
                    mMemmoryCache.put(UrlSizeKey.obtain(key), drawable);
                }
                if (listenersSet != null) {
                    Iterator<ImageLoadingListener> iter = listenersSet.iterator();
                    while (iter.hasNext()) {
                        ImageLoadingListener listener = iter.next();
                        if (listener != null) {
                            listener.onLoadingComplete(key, drawable, fromMemmoryCache);
                        }
                    }
                    ImageLoaderLog.d(TAG,
                            "complete URL:" + key.mUrl + ",width:" + key.mViewWidth + ",height:" + key.mViewHeight);
                }
            } else if (task.retryCount < ImageLoadTask.RETYR_MAX_COUNT) {
                ImageLoaderLog.d(TAG,
                        "complete retry URL:" + key.mUrl + ",width:" + key.mViewWidth + ",height:" + key.mViewHeight);
                task.retryCount++;
                ImageLoadTask newtask = new ImageLoadTask(task);
                mTasks.put(newtask.mKey, newtask);
                mWaitingQuene.add(newtask);
                mListeners.put(newtask.mKey, listenersSet);
            }

            runNext();
        }

        @Override
        public void onLoadingCancelled(UrlSizeKey key) {
            ImageLoadTask task = mTasks.remove(key);
            mRunningQuene.remove(task);
            HashSet<ImageLoadingListener> listenersSet = mListeners.remove(key);
            if (listenersSet != null) {
                Iterator<ImageLoadingListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageLoadingListener listener = iter.next();
                    if (listener != null) {
                        listener.onLoadingCancelled(key);
                    }
                }
            }
            runNext();
        }

    };

    private SizeLruCache mSizeLruCache;

    private ConcurrentHashMap<UrlSizeKey, HashSet<ImageSizeListener>> mSizeListeners =
            new ConcurrentHashMap<UrlSizeKey, HashSet<ImageSizeListener>>();

    private ImageSizeListener mEmptySizeListener = new SimpleImageSizeListener();

    private ImageSizeListener mDispatchImageSizeListener = new ImageSizeListener() {

        @Override
        public void onGetSizeStart(UrlSizeKey key) {
            HashSet<ImageSizeListener> listenersSet = mSizeListeners.get(key);
            if (listenersSet != null) {
                Iterator<ImageSizeListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageSizeListener listener = iter.next();
                    if (listener != null) {
                        listener.onGetSizeStart(key);
                    }
                }
            }
        }

        @Override
        public void onGetSizeFailed(UrlSizeKey key, Exception failReason) {
            ImageLoadTask task = mTasks.remove(key);
            mRunningQuene.remove(task);
            HashSet<ImageSizeListener> listenersSet = mSizeListeners.remove(key);
            if (listenersSet != null) {
                Iterator<ImageSizeListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageSizeListener listener = iter.next();
                    if (listener != null) {
                        listener.onGetSizeFailed(key, failReason);
                    }
                }
            }
            runNext();
        }

        @Override
        public void onGetSizeComplete(UrlSizeKey key, SizeDrawable drawable, boolean fromMemmoryCache) {
            ImageLoaderLog.d(TAG,
                    "completesize URL:" + key.mUrl + ",width:" + drawable.getIntrinsicWidth()+ ",height:" + drawable
                            .getIntrinsicHeight());
            if(!fromMemmoryCache){
                mSizeLruCache.put(UrlSizeKey.obtain(key),drawable);
            }
            ImageLoadTask task = mTasks.remove(key);
            mRunningQuene.remove(task);
            HashSet<ImageSizeListener> listenersSet = mSizeListeners.remove(key);
            if (listenersSet != null) {
                Iterator<ImageSizeListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageSizeListener listener = iter.next();
                    if (listener != null) {
                        listener.onGetSizeComplete(key, drawable, fromMemmoryCache);
                    }
                }
            }
            runNext();
        }
    };

    private FileLruCache mFileLruCache;

    private ConcurrentHashMap<UrlSizeKey, HashSet<ImageFileListener>> mFileListeners =
            new ConcurrentHashMap<UrlSizeKey, HashSet<ImageFileListener>>();

    private ImageFileListener mEmptyFileListener = new SimpleImageFileListener();

    private ImageFileListener mDispatchImageFileListener = new ImageFileListener() {

        @Override
        public void onGetFileStart(UrlSizeKey key) {
            HashSet<ImageFileListener> listenersSet = mFileListeners.get(key);
            if (listenersSet != null) {
                Iterator<ImageFileListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageFileListener listener = iter.next();
                    if (listener != null) {
                        listener.onGetFileStart(key);
                    }
                }
            }
        }

        @Override
        public void onGetFileFailed(UrlSizeKey key, Exception failReason) {
            ImageLoadTask task = mTasks.remove(key);
            mRunningQuene.remove(task);
            HashSet<ImageFileListener> listenersSet = mFileListeners.remove(key);
            if (listenersSet != null) {
                Iterator<ImageFileListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageFileListener listener = iter.next();
                    if (listener != null) {
                        listener.onGetFileFailed(key,failReason);
                    }
                }
            }
            runNext();
        }

        @Override
        public void onGetFileComplete(UrlSizeKey key, FileDrawable drawable, boolean fromMemmoryCache) {
            if(!fromMemmoryCache){
                mFileLruCache.put(UrlSizeKey.obtain(key),drawable);
            }
            ImageLoadTask task = mTasks.remove(key);
            mRunningQuene.remove(task);
            HashSet<ImageFileListener> listenersSet = mFileListeners.remove(key);
            if (listenersSet != null) {
                Iterator<ImageFileListener> iter = listenersSet.iterator();
                while (iter.hasNext()) {
                    ImageFileListener listener = iter.next();
                    if (listener != null) {
                        listener.onGetFileComplete(key, drawable, fromMemmoryCache);
                    }
                }
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
        mGifBitmapPool = new BitmapPool(memorySizeCalator.getGifBitmapPoolSize(), "gif");
        mBitmapPool = new BitmapPool(memorySizeCalator.getBitmapPoolSize(), "bitmap");
        mMemmoryCache = new MemmoryLruCache(memorySizeCalator.getMemoryCacheSize());
        mByteArrayPool = new ByteArrayPool(memorySizeCalator.getArrayPoolSizeInBytes());
        mSizeLruCache = new SizeLruCache(100);
        mFileLruCache = new FileLruCache(100);
        boolean hasExtrenalStorage = true;
        try {
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
            } else {
                hasExtrenalStorage = false;
            }
        } catch (Exception e) {
            hasExtrenalStorage = false;
        }

        if (hasExtrenalStorage) {
            mDiskLruCache = new ExternalCacheDiskCacheFactory(context).build();
        }
        if (mDiskLruCache == null) {
            mDiskLruCache = new InternalCacheDiskCacheFactory(context).build();
        }

        CustomDrawable.sTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
    }

    public void clearMemmoryCache(){
        ImageLoaderLog.d(TAG,"clear memmory");
        mMemmoryCache.trimToSize(0);
        mBitmapPool.trimToSize(0);
        mGifBitmapPool.trimToSize(0);
    }

    public void removeFromDiskCache(String url){
        clearMemmoryCache();
        mDiskLruCache.delete(url);
    }

    public void clearDiskCache(){
        clearMemmoryCache();
        mDiskLruCache.clear();
    }

    public void loadImage(String url, ImageLoadingListener listener) {
        loadImage(url, 0, 0, listener, false, false);
    }

    public void loadImage(String url, int width, int height, ImageLoadingListener listener) {
        loadImage(url, width, height, listener, false, false);
    }

    public void loadImage(String url, int width, int height, ImageLoadingListener listener,
                          boolean isFastScroll,boolean needRecycleUse) {

        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (listener == null) {
            listener = mEmptyListener;
        }

        UrlSizeKey key = obtain(url, width, height, UrlSizeKey.TYPE_LOADIMAGE);

        CustomDrawable drawable = mMemmoryCache.get(key);
        if (drawable != null) {
            listener.onLoadingStarted(key);
            listener.onLoadingComplete(key, drawable, true);
            release(key);
            return;
        }

        if (isFastScroll) {
            ImageLoaderLog.d(TAG, "is fast scroll:" + url);
            release(key);
            return;
        }

        ImageLoadTask task = mTasks.get(key);
        if (task != null && mListeners.containsKey(key)) {
            HashSet<ImageLoadingListener> listeners = mListeners.get(key);
            listeners.add(listener);
            release(key);
            return;
        }

        ImageLoaderLog.d(TAG, "real startload URL:" + url + ",width:" + width + ",height:" + height);

        task = new ImageLoadTask();
        task.mImageLoadingListener = mDispatchListener;
        task.mKey = key;
        task.mDecodeInfo.needRecycleUse = needRecycleUse;
        HashSet<ImageLoadingListener> listeners = new HashSet<ImageLoadingListener>();
        listeners.add(listener);
        mTasks.put(key, task);
        mWaitingQuene.offer(task);
        mListeners.put(key, listeners);
        runNext();
    }

    public void loadImageOnlyGetSize(String url, ImageSizeListener listener) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (listener == null) {
            listener = mEmptySizeListener;
        }

        UrlSizeKey key = obtain(url, 0, 0, UrlSizeKey.TYPE_LOADSIZE);

        SizeDrawable drawable = mSizeLruCache.get(key);
        if (drawable != null) {
            listener.onGetSizeStart(key);
            listener.onGetSizeComplete(key, drawable, true);
            release(key);
            return;
        }

        ImageLoadTask task = mTasks.get(key);
        if (task != null && mSizeListeners.containsKey(key)) {
            HashSet<ImageSizeListener> listeners = mSizeListeners.get(key);
            listeners.add(listener);
            release(key);
            return;
        }

        task = new ImageLoadTask();
        task.mImageSizeListener = mDispatchImageSizeListener;
        task.mKey = key;
        HashSet<ImageSizeListener> listeners = new HashSet<ImageSizeListener>();
        listeners.add(listener);
        mTasks.put(key, task);
        mWaitingQuene.offer(task);
        mSizeListeners.put(key, listeners);
        runNext();
    }

    public void loadImageOnlyGetFile(String url, ImageFileListener listener) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (listener == null) {
            listener = mEmptyFileListener;
        }

        UrlSizeKey key = obtain(url, 0, 0, UrlSizeKey.TYPE_LOADFILE);

        FileDrawable drawable = mFileLruCache.get(key);
        if (drawable != null) {
            listener.onGetFileStart(key);
            listener.onGetFileComplete(key, drawable, true);
            release(key);
            return;
        }

        ImageLoadTask task = mTasks.get(key);
        if (task != null && mFileListeners.containsKey(key)) {
            HashSet<ImageFileListener> listeners = mFileListeners.get(key);
            listeners.add(listener);
            release(key);
            return;
        }

        task = new ImageLoadTask();
        task.mImageFileListener = mDispatchImageFileListener;
        task.mKey = key;
        HashSet<ImageFileListener> listeners = new HashSet<ImageFileListener>();
        listeners.add(listener);
        mTasks.put(key, task);
        mWaitingQuene.offer(task);
        mFileListeners.put(key, listeners);
        runNext();
    }

    private void runNext() {
        while (mRunningQuene.size() < MAX_RUNNING_TASK && mWaitingQuene.size() > 0) {
            ImageLoadTask task = mWaitingQuene.poll();
            mRunningQuene.offer(task);
            task.execute();
        }
    }

    public void cancelLoad(String url, int width, int height, ImageLoadingListener listener, boolean cancelRunning) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        mFailedQuene.remove(url);
        UrlSizeKey key = obtain(url, width, height, UrlSizeKey.TYPE_LOADIMAGE);
        if (mTasks.containsKey(key)) {
            ImageLoaderLog.d(TAG, "cancel waiting URL:" + url + ",width:" + width + ",height:" + height);
            ImageLoadTask task = mTasks.get(key);
            if (mWaitingQuene.contains(task)) {
                HashSet<ImageLoadingListener> listeners = mListeners.get(key);
                listeners.remove(listener);
                listener.onLoadingCancelled(key);
                if (listeners.isEmpty()) {
                    mWaitingQuene.remove(task);
                    mTasks.remove(key);
                    mListeners.remove(key);
                }
            } else if (cancelRunning) {
                task.cancel(false);
            }

        }
        release(key);

    }

    public CustomDrawable getMemmory(String url, int width, int height) {
        UrlSizeKey key = obtain(url, width, height, UrlSizeKey.TYPE_LOADIMAGE);
        CustomDrawable drawable = mMemmoryCache.get(key);
        key.recycle();
        return drawable;
    }

    private UrlSizeKey obtain(String url, int width, int height, int type) {
        UrlSizeKey key = UrlSizeKey.obtain();
        key.mUrl = url;
        key.mViewWidth = width;
        key.mViewHeight = height;
        key.mType = type;
        return key;
    }

    private void release(UrlSizeKey key) {
        key.recycle();
    }

}
