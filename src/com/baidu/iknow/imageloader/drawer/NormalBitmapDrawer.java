package com.baidu.iknow.imageloader.drawer;

import android.graphics.Canvas;
import android.graphics.Path.Direction;

import com.baidu.iknow.imageloader.drawable.DrawableWrapper;

public class NormalBitmapDrawer extends BitmapShaderDrawer{

    public NormalBitmapDrawer() {
        super();
    }

    @Override
    public void drawContentReal(Canvas canvas, DrawableWrapper drawable) {
        mArgs.mPath.reset();
        mArgs.mPath.addRect(mTransformBounds, Direction.CW);
        canvas.drawPath(mArgs.mPath, mPaint);
    }

    @Override
    public void drawBorder(Canvas canvas, DrawableWrapper drawable) {
        if (!mArgs.mHasBorder) {
            return;
        }
        mArgs.mBorderPath.reset();
        mArgs.mBorderPath.addRect(mBorderRect, Direction.CW);
        canvas.drawPath(mArgs.mBorderPath, mBorderPaint);
    }
}
