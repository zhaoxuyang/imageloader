package com.baidu.iknow.imageloader.drawer;

import com.baidu.iknow.imageloader.drawable.DrawableWrapper;

import android.graphics.Canvas;

public class CustomBitmapDrawer extends BitmapShaderDrawer{

    public CustomBitmapDrawer() {
        super();
    }

    @Override
    public void drawContentReal(Canvas canvas, DrawableWrapper drawable) {
        canvas.drawPath(mArgs.mPath, mPaint);
    }

    @Override
    public void drawBorder(Canvas canvas, DrawableWrapper drawable) {
        if (!mArgs.mHasBorder) {
            return;
        }
        canvas.drawPath(mArgs.mBorderPath, mBorderPaint);
    }
}
