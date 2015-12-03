package com.baidu.iknow.imageloader.player;

import com.baidu.iknow.imageloader.drawable.BitmapDrawable;
import com.baidu.iknow.imageloader.drawable.GifDrawable;
import com.baidu.iknow.imageloader.drawable.BitmapDrawable.BitmapDrawableFactory;
import com.baidu.iknow.imageloader.request.ImageLoader;
import com.baidu.iknow.imageloader.widgets.CustomImageView;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;

public class GifDrawablePlayer extends DrawablePlayer {

    private BitmapDrawable mGifDrawable;

    private int mCurrentFrame;

    private boolean mGifPlaying;

    private Handler mHandler = new Handler();

    private PlayRunnable mRunnable = new PlayRunnable();

    public GifDrawablePlayer(CustomImageView view) {
        super(view);
    }

    @Override
    public Drawable play(Drawable drawable, boolean isFastScroll) {
        if (!(drawable instanceof GifDrawable)) {
            return drawable;
        }
        GifDrawable gd = (GifDrawable) drawable;
        if (mGifDrawable == null) {
            Bitmap bm = ImageLoader.getInstance().mGifBitmapPool.get(gd.getIntrinsicWidth(), gd.getIntrinsicHeight(),
                    true, true);
            mGifDrawable = BitmapDrawableFactory.createBitmapDrawable(bm);
            if (mGifDrawable == null) {
                return null;
            }
            mView.mNeedComputeBounds = true;
            mCurrentFrame = 0;
        }
        int delay = gd.updateFrame(mCurrentFrame, mGifDrawable.mBitmap);
        mView.invalidate();
        if (isFastScroll) {
            return mGifDrawable;
        }

        if (!mGifPlaying) {
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, delay);
            mGifPlaying = true;
        }
        return mGifDrawable;
    }

    @Override
    public void stop() {
        mHandler.removeCallbacks(mRunnable);
        mCurrentFrame = 0;
        mGifPlaying = false;
        if (mGifDrawable != null) {
            Bitmap bm = mGifDrawable.mBitmap;
            ImageLoader.getInstance().mGifBitmapPool.put(bm);
            mGifDrawable = null;
        }

    }

    /**
     * gif播放的runnable
     * 
     * @author zhaoxuyang
     * @since 2015-9-22
     */
    public class PlayRunnable implements Runnable {

        @Override
        public void run() {
            if (!mGifPlaying) {
                return;
            }
            Drawable cd = mView.getDrawableFromCache();
            if (cd instanceof GifDrawable) {
                GifDrawable gd = (GifDrawable) cd;
                mCurrentFrame++;
                if (mCurrentFrame >= gd.getFrameCount()) {
                    mCurrentFrame = 0;
                }
                int delay = gd.updateFrame(mCurrentFrame, mGifDrawable.mBitmap);
                mView.invalidate();
                if (delay >= 0) {
                    mHandler.removeCallbacks(mRunnable);
                    mHandler.postDelayed(mRunnable, delay);
                }
            }

        }

    }

}
