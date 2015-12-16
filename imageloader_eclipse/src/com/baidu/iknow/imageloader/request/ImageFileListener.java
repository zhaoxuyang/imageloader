package com.baidu.iknow.imageloader.request;

import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.drawable.FileDrawable;

public interface ImageFileListener {

	void onGetFileStart(UrlSizeKey key);

	void onGetFileFailed(UrlSizeKey key, Exception failReason);

	void onGetFileComplete(UrlSizeKey key, FileDrawable drawable, boolean fromMemmoryCache);

}
