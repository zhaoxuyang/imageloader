
package com.baidu.iknow.imageloader.request;

import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.drawable.SizeDrawable;

/**
 * 空的图片加载回调
 * @author zhaoxuyang
 * @since 2015-10-12
 */
public class SimpleImageSizeListener implements ImageSizeListener {

    @Override
    public void onGetSizeStart(UrlSizeKey key) {

    }

    @Override
    public void onGetSizeFailed(UrlSizeKey key, Exception failReason) {

    }

    @Override
    public void onGetSizeComplete(UrlSizeKey key, SizeDrawable drawable, boolean fromMemmoryCache) {

    }
}
