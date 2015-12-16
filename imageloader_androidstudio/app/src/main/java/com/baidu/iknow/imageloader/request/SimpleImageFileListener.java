
package com.baidu.iknow.imageloader.request;

import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.drawable.FileDrawable;

/**
 * 空的图片加载回调
 * @author zhaoxuyang
 * @since 2015-10-12
 */
public class SimpleImageFileListener implements ImageFileListener {

    @Override
    public void onGetFileStart(UrlSizeKey key) {

    }

    @Override
    public void onGetFileFailed(UrlSizeKey key, Exception failReason) {

    }

    @Override
    public void onGetFileComplete(UrlSizeKey key, FileDrawable drawable, boolean fromMemmoryCache) {

    }
}
