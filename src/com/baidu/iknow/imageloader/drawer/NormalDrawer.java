package com.baidu.iknow.imageloader.drawer;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;

import com.baidu.iknow.imageloader.drawable.DrawableWrapper;

public class NormalDrawer extends AbsDrawer{
    
    private Rect mRect = new Rect();

    public NormalDrawer() {
        super();
    }

    @Override
    public boolean applyBounds(DrawableWrapper drawable) {
        mRect.set((int)mBounds.left, (int)mBounds.top, (int)mBounds.right, (int)mBounds.bottom);
        drawable.mDrawable.setBounds(mRect);
        return true;
    }

    @Override
    public void drawContentReal(Canvas canvas, DrawableWrapper drawable) {
        canvas.concat(mDrawMatrix);
        drawable.mDrawable.draw(canvas);
    }

    @Override
    public void drawBorder(Canvas canvas, DrawableWrapper drawable) {
        if (!mArgs.mHasBorder) {
            return;
        }
        mArgs.mBorderPath.reset();
        mArgs.mBorderPath.addRect(mBorderRect, Path.Direction.CW);
        canvas.concat(mDrawMatrix);
        canvas.drawPath(mArgs.mBorderPath, mBorderPaint);
    }

}
