
package com.baidu.iknow.imageloader.request;

import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.drawable.CustomDrawable;

/**
 * 空的图片加载回调
 * @author zhaoxuyang
 * @since 2015-10-12
 */
public class SimpleImageLoadingListener implements ImageLoadingListener {
    @Override
    public void onLoadingStarted(UrlSizeKey key) {
        // Empty implementation
    }

    @Override
    public void onLoadingFailed(UrlSizeKey key, Exception failReason) {
        // Empty implementation
    }

    @Override
    public void onLoadingComplete(UrlSizeKey key, CustomDrawable drawable, boolean fromMemmoey) {
        // Empty implementation
    }

    @Override
    public void onLoadingCancelled(UrlSizeKey key) {
        // Empty implementation
    }
}
