package com.baidu.iknow.imageloader.drawer;

import com.baidu.iknow.imageloader.drawable.BitmapDrawable;
import com.baidu.iknow.imageloader.drawable.DrawableWrapper;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;


public abstract class BitmapShaderDrawer extends AbsDrawer {
    
    protected Matrix mShaderMatrix = new Matrix();

    protected RectF mTransformBounds = new RectF();
    
    public BitmapShaderDrawer() {
        super();
    }


    @Override
    public boolean applyBounds(DrawableWrapper drawable) {
        if(!(drawable.mDrawable instanceof BitmapDrawable)){
            return false;
        }
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable.mDrawable;
        if(!bitmapDrawable.checkLegal()){
            return false;
        }
        // 计算mBounds经过矩阵变换后的范围
        Bitmap bm = bitmapDrawable.mBitmap;
        int bw = bm.getWidth();
        int bh = bm.getHeight();
        applyMatrix(mBounds.left, mBounds.top, mDrawMatrix);
        int left = (int) mPoint.x;
        int top = (int) mPoint.y;
        applyMatrix(mBounds.right, mBounds.bottom, mDrawMatrix);
        int right = (int) mPoint.x;
        int bottom = (int) mPoint.y;
        float scaleW = (float) (right - left) / (float) bw;
        float scaleH = (float) (bottom - top) / (float) bh;
        mShaderMatrix.reset();
        mShaderMatrix.setScale(scaleW, scaleH);
        mShaderMatrix.postTranslate(left, top);
      
        if (bitmapDrawable.mShader == null) {
            return false;
        }
        bitmapDrawable.mShader.setLocalMatrix(mShaderMatrix);
        mPaint.setShader(bitmapDrawable.mShader);

        int vwidth = drawable.mViewWidth - drawable.mPaddingLeft - drawable.mPaddingRight;
        int vheight = drawable.mViewHeight - drawable.mPaddingTop - drawable.mPaddingBottom;
        // 经过矩阵变换后的绘制范围，并和当前控件的范围取交集
        left = (int) Math.max(left, 0);
        top = (int) Math.max(top, 0);
        right = (int) Math.min(right, vwidth);
        bottom = (int) Math.min(bottom, vheight);
        mTransformBounds.set(left, top, right, bottom);

        if (!mArgs.mHasBorder) {
            return true;
        }

        float half = mArgs.mBorderWidth / 2.0f;
        mBorderRect.set(mTransformBounds.left + half, mTransformBounds.top + half, mTransformBounds.right - half,
                mTransformBounds.bottom - half);

        return true;
    }

  
}
