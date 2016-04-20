package com.baidu.iknow.imageloader;

import com.baidu.iknow.imageloader.request.ImageLoader;

import android.app.Application;

public class TestApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.getInstance().init(this);
    }
}