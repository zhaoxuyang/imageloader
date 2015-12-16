package com.baidu.iknow.imageloader.drawable;

/**
 * Created by zhaoxuyang on 15/12/14.
 */
public class SizeDrawable extends CustomDrawable {

    private int mWidth;

    private int mHeight;

    public SizeDrawable(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    @Override
    public void recycle() {

    }

    @Override
    public boolean checkLegal() {
        return true;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }
}
