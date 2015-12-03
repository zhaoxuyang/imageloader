package com.baidu.iknow.imageloader.gif;

import android.graphics.Bitmap;

/**
 * 
 * @author zhaoxuyang
 * @since 2015-4-7
 */
public class Gif {

    /**
     * gif native层的指针
     */
    private int mPtr;

    /**
     * gif的宽度
     */
    public int mWidth;

    /**
     * gif的高度
     */
    public int mHeight;

    /**
     * gif的帧数
     */
    public int mFrameCount;

    /**
     * 是否已经recycle
     */
    public boolean isRecycled;

    /**
     * 获取当前帧
     * 
     * @param index
     *            index
     * @return int
     */
    public int getFrame(int index,Bitmap bm) {
        return writeFrameToBitmap(index, bm);
    }


    /**
     * 刷新bitmap
     * 
     * @param index
     * @param bm
     * @return
     */
    private native int writeFrameToBitmap(int index, Bitmap bm);

    /**
     * 回收
     */
    public void recycle() {
        if (mPtr != -1) {
            nativeRelease();
            mPtr = -1;
        }
        isRecycled = true;
    }

    /**
     * 回收native内存
     */
    private native void nativeRelease();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        recycle();
    }

}
