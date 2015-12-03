package com.baidu.iknow.imageloader.drawer;

import com.baidu.iknow.imageloader.drawable.DrawableWrapper;

import android.graphics.Canvas;
import android.graphics.Path.Direction;

public class RoundRectBitmapDrawer extends BitmapShaderDrawer{

    public RoundRectBitmapDrawer() {
        super();
    }

    @Override
    public void drawContentReal(Canvas canvas, DrawableWrapper drawable) {
        mArgs.mPath.reset();
        mArgs.mPath.addRoundRect(mTransformBounds, mArgs.mRadius, mArgs.mRadius, Direction.CW);
        canvas.drawPath(mArgs.mPath, mPaint);
    }

    @Override
    public void drawBorder(Canvas canvas, DrawableWrapper drawable) {
        if (!mArgs.mHasBorder) {
            return;
        }
        mArgs.mBorderPath.reset();
        mArgs.mBorderPath.addRoundRect(mBorderRect, mArgs.mRadius, mArgs.mRadius, Direction.CW);
        canvas.drawPath(mArgs.mBorderPath, mBorderPaint);
    }

}
