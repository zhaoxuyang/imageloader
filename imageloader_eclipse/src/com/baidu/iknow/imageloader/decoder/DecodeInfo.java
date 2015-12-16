package com.baidu.iknow.imageloader.decoder;

import com.baidu.iknow.imageloader.gif.GifFactory;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory.Options;
import android.os.Build;

public class DecodeInfo {

    public Options mBitmapOptions = new Options();

    public GifFactory.Options mGifOptions = new GifFactory.Options();

    public boolean mScaleToFitView = true;

    private DecodeInfo(DecodeInfoBuilder builder) {
        mBitmapOptions = builder.mBitmapOptions;
        mGifOptions = builder.mGifOptions;
        mScaleToFitView = builder.mScaleToFitView;
    }

    public static class DecodeInfoBuilder {
        
        private Options mBitmapOptions = new Options();
        
        private GifFactory.Options mGifOptions = new GifFactory.Options();

        private boolean mScaleToFitView = true;

        @SuppressLint("NewApi")
        public DecodeInfoBuilder() {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                mBitmapOptions.inPurgeable = true;
            } else {
                mBitmapOptions.inMutable = true;
            }
        }

        public DecodeInfoBuilder setBitmapOptions(Options opts) {
            mBitmapOptions = opts;
            return this;
        }

        public DecodeInfoBuilder setGifOptions(GifFactory.Options opts) {
            mGifOptions = opts;
            return this;
        }

        public DecodeInfoBuilder setScaleToFitView(boolean scaleToFitView) {
            mScaleToFitView = scaleToFitView;
            return this;
        }

        public DecodeInfo build() {
            return new DecodeInfo(this);
        }
    }

}
