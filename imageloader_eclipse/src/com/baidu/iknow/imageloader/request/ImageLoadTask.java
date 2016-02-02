package com.baidu.iknow.imageloader.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.baidu.iknow.imageloader.cache.PoolingByteArrayOutputStream;
import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.cache.DiskLruCache.Snapshot;
import com.baidu.iknow.imageloader.decoder.BaseDecoder;
import com.baidu.iknow.imageloader.decoder.DecodeInfo;
import com.baidu.iknow.imageloader.decoder.DecoderFactory;
import com.baidu.iknow.imageloader.drawable.CustomDrawable;
import com.baidu.iknow.imageloader.drawable.FileDrawable;
import com.baidu.iknow.imageloader.drawable.SizeDrawable;
import com.baidu.iknow.imageloader.request.DataFetcher.DataCallback;

import android.net.Uri;

/**
 * 图片加载任务
 *
 * @author zhaoxuyang
 * @since 2015-10-12
 */
public class ImageLoadTask extends AsyncTask<UrlSizeKey, Integer, CustomDrawable> {

    private static final String TAG = ImageLoadTask.class.getSimpleName();

    public static final int RETYR_MAX_COUNT = 1;

    public ImageLoadingListener mImageLoadingListener;

    public ImageSizeListener mImageSizeListener;

    public ImageFileListener mImageFileListener;

    public UrlSizeKey mKey;

    public DecodeInfo mDecodeInfo;

    public int retryCount;

    private DataFetcher<InputStream> mHttpUrlFetcher;

    private Exception mException;

    public ImageLoadTask() {

    }

    public ImageLoadTask(ImageLoadTask task) {
        if (task != null) {
            mKey = UrlSizeKey.obtain(task.mKey);
            mImageLoadingListener = task.mImageLoadingListener;
            mDecodeInfo = task.mDecodeInfo;
            retryCount = task.retryCount;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mKey == null) {
            return;
        }
        switch (mKey.mType) {
            case UrlSizeKey.TYPE_LOADFILE:
                mImageFileListener.onGetFileStart(mKey);
                break;
            case UrlSizeKey.TYPE_LOADIMAGE:
                mImageLoadingListener.onLoadingStarted(mKey);
                break;
            case UrlSizeKey.TYPE_LOADSIZE:
                mImageSizeListener.onGetSizeStart(mKey);
                break;
        }

    }

    @Override
    protected CustomDrawable doInBackground(UrlSizeKey... params) {
        if (mKey == null) {
            return null;
        }

        if (mDecodeInfo == null) {
            mDecodeInfo = new DecodeInfo.DecodeInfoBuilder().build();
        }

        Uri uri = Uri.parse(mKey.mUrl);
        String schema = uri.getScheme();
        CustomDrawable drawable = null;

        switch (mKey.mType) {
            case UrlSizeKey.TYPE_LOADFILE:
                File file = null;
                if ("file".equals(schema)) {
                    file = new File(uri.getPath());
                } else {
                    file = ImageLoader.getInstance().mDiskLruCache.getFile(mKey.mUrl, 0);
                    if (file == null || !file.exists()) {
                        fetchDataFromNet();
                        file = ImageLoader.getInstance().mDiskLruCache.getFile(mKey.mUrl, 0);
                    }
                }
                if (file == null || !file.exists()) {
                    mException = new Exception("no such file");
                    return null;
                }
                drawable = new FileDrawable(file);
                break;
            case UrlSizeKey.TYPE_LOADIMAGE:
            case UrlSizeKey.TYPE_LOADSIZE:
                InputStream is = null;
                Snapshot ss = null;
                int from = BaseDecoder.FROM_DISK;
                if ("file".equals(uri.getScheme())) {
                    try {
                        is = new FileInputStream(uri.getPath());
                    } catch (FileNotFoundException e) {
                        mException = e;
                    }
                } else {
                    try {
                        ss = ImageLoader.getInstance().mDiskLruCache.get(mKey.toString());
                        if(ss==null){
                            ss = ImageLoader.getInstance().mDiskLruCache.get(mKey.mUrl);
                        }
                        if (ss == null) {
                            fetchDataFromNet();
                            from = BaseDecoder.FROM_NET;
                            ss = ImageLoader.getInstance().mDiskLruCache.get(mKey.mUrl);
                        }
                        if (ss != null) {
                            is = ss.getInputStream(0);
                        }
                    } catch (Exception e) {
                        mException = e;
                    }
                }

                if (is == null && mException == null) {
                    mException = new Exception("save sdcard failed");
                    return null;
                } else if (mException != null) {
                    return null;
                }
                drawable = doDecode(is,from);
                if (ss != null) {
                    ss.close();
                }
                break;
        }

        return drawable;
    }

    @Override
    protected void onPostExecute(CustomDrawable result) {
        super.onPostExecute(result);
        if (mKey == null) {
            return;
        }
        switch (mKey.mType) {
            case UrlSizeKey.TYPE_LOADIMAGE:
                if (result != null) {
                    mImageLoadingListener.onLoadingComplete(mKey, result, false);
                } else {
                    mImageLoadingListener.onLoadingFailed(mKey, mException);
                }
                break;
            case UrlSizeKey.TYPE_LOADSIZE:
                if (result != null) {
                    mImageSizeListener.onGetSizeComplete(mKey, (SizeDrawable) result, false);
                } else {
                    mImageSizeListener.onGetSizeFailed(mKey, mException);
                }
                break;
            case UrlSizeKey.TYPE_LOADFILE:
                if (result != null) {
                    mImageFileListener.onGetFileComplete(mKey, (FileDrawable) result, false);
                } else {
                    mImageFileListener.onGetFileFailed(mKey, mException);
                }
                break;
        }

        if (mKey != null) {
            mKey.recycle();
            mKey = null;
        }
    }

    @Override
    protected void onCancelled(CustomDrawable result) {
        super.onCancelled(result);
        if (mKey == null) {
            return;
        }
        if (mHttpUrlFetcher != null) {
            mHttpUrlFetcher.cancel();
            mHttpUrlFetcher.cleanup();
            mHttpUrlFetcher = null;
        }

        switch (mKey.mType) {
            case UrlSizeKey.TYPE_LOADIMAGE:
                if (result != null) {
                    mImageLoadingListener.onLoadingComplete(mKey, result, false);
                } else {
                    mImageLoadingListener.onLoadingCancelled(mKey);
                }
                break;
            case UrlSizeKey.TYPE_LOADSIZE:
            case UrlSizeKey.TYPE_LOADFILE:
                break;
        }
        if (mKey != null) {
            mKey.recycle();
            mKey = null;
        }
    }

    private void fetchDataFromNet() {

        mHttpUrlFetcher = DataFatcherFactory.getNetworkDataFetcher(mKey);
        mHttpUrlFetcher.loadData(new DataCallback<InputStream>() {

            @Override
            public void onDataReady(InputStream data) {
                try {
                    ImageLoader.getInstance().mDiskLruCache.put(mKey.mUrl, data);
                } catch (Exception e) {
                    mException = e;
                } finally {
                    if (data != null) {
                        try {
                            data.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            @Override
            public void onLoadFailed(Exception e) {
                mException = e;
            }
        });

    }

    private CustomDrawable doDecode(InputStream is, int from) {
        if (is == null) {
            return null;
        }

        PoolingByteArrayOutputStream baos = null;
        try {
            baos = new PoolingByteArrayOutputStream(ImageLoader.getInstance().mByteArrayPool);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            byte[] bytes = baos.toByteArray();
            BaseDecoder decoder = DecoderFactory.getDecoder(bytes);
            if (mKey.mType == UrlSizeKey.TYPE_LOADSIZE) {
                return decoder.getSize(bytes, mDecodeInfo);
            }
            return decoder.doDecode(bytes, mDecodeInfo, mKey,from);
        } catch (Exception e) {
            mException = e;
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }

}
