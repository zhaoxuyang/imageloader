package com.baidu.iknow.imageloader.drawable;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.ImageView.ScaleType;

import com.baidu.iknow.imageloader.drawer.AbsDrawer;

public class DrawableWrapper {
    public int mViewWidth;

    public int mViewHeight;

    public int mPaddingLeft;

    public int mPaddingRight;

    public int mPaddingTop;

    public int mPaddingBottom;

    public int mScrollX;

    public int mScrollY;

    public ScaleType mScaleType;

    public AbsDrawer mDrawer;

    public Drawable mDrawable;
    
    public boolean mNeedComputeBounds = true;

    public int getScaledWidth() {
        if (mDrawable != null) {
            return mDrawable.getIntrinsicWidth();
        }
        return -1;
    }

    public int getScaledHeight() {
        if (mDrawable != null) {
            return mDrawable.getIntrinsicHeight();
        }
        return -1;
    }
    
    public void draw(Canvas canvas) {
        if(mDrawer==null){
            return;
        }
        if(mNeedComputeBounds){
            mDrawer.computeBounds(this);
        }
        mDrawer.drawContent(canvas, this);
        mDrawer.drawBorder(canvas, this);

    }

    @Override
    public String toString() {
        return "vw:"+mViewWidth+",vh:"+mViewHeight+",mDrawer:"+mDrawer+",need:"+mNeedComputeBounds;
    }

    public boolean checkIsLegal(){
        return mDrawable!=null&&mDrawer!=null;
    }
}
