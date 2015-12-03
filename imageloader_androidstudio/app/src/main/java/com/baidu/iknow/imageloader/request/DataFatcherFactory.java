package com.baidu.iknow.imageloader.request;

import java.io.InputStream;

import com.baidu.iknow.imageloader.cache.UrlSizeKey;

public class DataFatcherFactory {

    public static DataFetcher<InputStream> getNetworkDataFetcher(UrlSizeKey mKey){
        return new HttpUrlFetcher(mKey);
    }
}
