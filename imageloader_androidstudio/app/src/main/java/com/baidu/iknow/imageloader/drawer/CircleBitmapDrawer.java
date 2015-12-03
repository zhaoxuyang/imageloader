package com.baidu.iknow.imageloader.drawer;

import com.baidu.iknow.imageloader.drawable.DrawableWrapper;

import android.graphics.Canvas;
import android.graphics.Path.Direction;

public class CircleBitmapDrawer extends BitmapShaderDrawer {

    public CircleBitmapDrawer() {
        super();
    }

    @Override
    public void drawContentReal(Canvas canvas, DrawableWrapper drawable) {
        mArgs.mPath.reset();
        float cx = (mTransformBounds.right + mTransformBounds.left) / 2;
        float cy = (mTransformBounds.top + mTransformBounds.bottom) / 2;
        float r = Math.min(mTransformBounds.width(), mTransformBounds.height()) / 2;
        mArgs.mPath.addCircle(cx, cy, r, Direction.CW);
        canvas.drawPath(mArgs.mPath, mPaint);

    }

    @Override
    public void drawBorder(Canvas canvas, DrawableWrapper drawable) {
        if (!mArgs.mHasBorder) {
            return;
        }
        mArgs.mBorderPath.reset();
        float cx = (mBorderRect.right + mBorderRect.left) / 2;
        float cy = (mBorderRect.top + mBorderRect.bottom) / 2;
        float r = Math.min(mBorderRect.width(), mBorderRect.height()) / 2;
        mArgs.mBorderPath.addCircle(cx, cy, r, Direction.CW);
        canvas.drawPath(mArgs.mBorderPath, mBorderPaint);

    }

}
