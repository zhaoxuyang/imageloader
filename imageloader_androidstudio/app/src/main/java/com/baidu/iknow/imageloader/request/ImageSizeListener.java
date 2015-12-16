package com.baidu.iknow.imageloader.request;

import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.drawable.SizeDrawable;

public interface ImageSizeListener {

	void onGetSizeStart(UrlSizeKey key);

	void onGetSizeFailed(UrlSizeKey key, Exception failReason);

	void onGetSizeComplete(UrlSizeKey key, SizeDrawable drawable, boolean fromMemmoryCache);

}
