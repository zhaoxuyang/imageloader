package com.baidu.iknow.imageloader.drawer;

import com.baidu.iknow.imageloader.cache.ImageLoaderLog;
import com.baidu.iknow.imageloader.drawable.DrawableWrapper;
import com.baidu.iknow.imageloader.widgets.CustomImageView.MatrixScaleType;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.widget.ImageView.ScaleType;

/**
 * 1、计算绘制范围及变换矩阵 2、应用范围和矩阵 3、绘制
 * 
 * @author zhaoxuyang
 * @since 2015-9-17
 */
public abstract class AbsDrawer {

    /**
     * 
     */
    private static final String TAG = AbsDrawer.class.getName();

    /**
     * copy from source code
     */
    private static final Matrix.ScaleToFit[] sS2FArray = { Matrix.ScaleToFit.FILL, Matrix.ScaleToFit.START,
            Matrix.ScaleToFit.CENTER, Matrix.ScaleToFit.END };

    /**
     * 用于夜间模式
     */
    private static final int IMAGE_COLORFILTER_NIGHT = 0xFFB3B3B3;

    /**
     * 
     */
    private static final PorterDuffColorFilter sColorFilterForSkin = new PorterDuffColorFilter(IMAGE_COLORFILTER_NIGHT,
            Mode.MULTIPLY);

    /**
     * 
     */
    private static final int DEFAULT_PAINT_FLAGS = Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG;

    /**
     * 画图的画笔
     */
    protected Paint mPaint = new Paint(DEFAULT_PAINT_FLAGS);

    /**
     * 画边框的画笔
     */
    protected Paint mBorderPaint = new Paint();

    /**
     * 前景画笔
     */
    protected Paint mForegroundPaint = new Paint();

    /**
     * 根据scaletype计算出的绘制矩阵
     */
    protected Matrix mDrawMatrix = new Matrix();

    /**
     * 绘制范围
     */
    protected RectF mBounds = new RectF();

    /**
     * 边框范围
     */
    protected RectF mBorderRect = new RectF();

    /**
     * 临时使用的矩形
     */
    private RectF mTempSrc = new RectF();

    /**
     * 
     */
    private RectF mTempDst = new RectF();

    /**
     * 绘制参数相关
     */
    protected DrawerArgs mArgs;

    /**
     * 
     */
    private float[] mValues = new float[9];

    /**
     * 
     */
    protected PointF mPoint = new PointF();

    /**
     * 
     */
    protected RectF mForegroundRect = new RectF();

    /**
     * 
     */
    protected int mDensity;

    /**
     * 构造方法，初始化画笔
     */
    public AbsDrawer() {
        mBorderPaint.setStyle(Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mPaint.setAntiAlias(true);
        mForegroundPaint.setAntiAlias(true);
        mForegroundPaint.setStyle(Style.FILL);
    }

    public void setDensity(int density) {
        mDensity = density;
    }

    /**
     * 根据scaletype计算矩阵，参照imgaeview源码
     */
    public boolean computeBounds(DrawableWrapper drawable) {

        if (drawable == null || !drawable.checkIsLegal()) {
            return false;
        }

        int dwidth = drawable.getScaledWidth();
        int dheight = drawable.getScaledHeight();
        // 要去掉padding，因为绘制的时候是不包括padding的
        int vwidth = drawable.mViewWidth - drawable.mPaddingLeft - drawable.mPaddingRight;
        int vheight = drawable.mViewHeight - drawable.mPaddingTop - drawable.mPaddingBottom;

        if (dwidth < 0) {
            dwidth = vwidth;
        }

        if (dheight < 0) {
            dheight = vheight;
        }

        boolean fits = false;
        fits = (dwidth <= 0 || vwidth == dwidth) && (dheight <= 0 || vheight == dheight);
        mDrawMatrix.reset();

        // scaletype为fixxy的时候，绘制范围为控件的大小（减去padding），否则为图片的大小
        // 矩阵变换代码参考imagview源码
        if (ScaleType.FIT_XY == drawable.mScaleType || fits) {
            mBounds.set(0, 0, vwidth, vheight);
        } else {
            mBounds.set(0, 0, dwidth, dheight);
            if (ScaleType.CENTER == drawable.mScaleType) {
                ImageLoaderLog.d(TAG, "vw:" + vwidth + ",dw:" + dwidth);
                ImageLoaderLog.d(TAG, "vh:" + vheight + ",dh:" + dheight);
                mDrawMatrix.setTranslate((vwidth - dwidth) * 0.5f + 0.5f, (vheight - dheight) * 0.5f + 0.5f);
            } else if (ScaleType.CENTER_CROP == drawable.mScaleType) {
                float scale;
                float dx = 0, dy = 0;
                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight;
                    dx = (vwidth - dwidth * scale) * 0.5f;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                    dy = (vheight - dheight * scale) * 0.5f;
                }
                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(dx, dy);
            } else if (ScaleType.CENTER_INSIDE == drawable.mScaleType) {
                float scale;
                float dx;
                float dy;
                if (dwidth <= vwidth && dheight <= vheight) {
                    scale = 1.0f;
                } else {
                    scale = Math.min((float) vwidth / (float) dwidth, (float) vheight / (float) dheight);
                }
                dx = (vwidth - dwidth * scale) * 0.5f;
                dy = (vheight - dheight * scale) * 0.5f;
                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(dx, dy);
            } else if (drawable.mScaleType == ScaleType.MATRIX) {
                if (drawable.mMatrixScaleType == MatrixScaleType.TOP_CROP) {
                    float scale;
                    float dx = 0, dy = 0;
                    if (dwidth * vheight > vwidth * dheight) {
                        scale = (float) vheight / (float) dheight;
                        dx = (vwidth - dwidth * scale) * 0.5f;
                    } else {
                        scale = (float) vwidth / (float) dwidth;
                        dy = (vheight - dheight * scale) * 0.5f;
                    }
                    mDrawMatrix.setScale(scale, scale);
                    mDrawMatrix.postTranslate(dx, 0);
                    System.out.println("top_crop");
                } else {
                    mDrawMatrix.set(drawable.mCustomMatrix);
                }
            } else {
                mTempSrc.set(0, 0, dwidth, dheight);
                mTempDst.set(0, 0, vwidth, vheight);
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst, scaleTypeToScaleToFit(drawable.mScaleType));
            }
        }
        return applyBounds(drawable);
    }

    public Matrix getDrawMatrix() {
        return mDrawMatrix;
    }

    /**
     * 绘制时的一些公共逻辑，绘制的时候不包括padding
     * 
     * @param canvas
     * @param drawable
     */
    public void drawContent(Canvas canvas, DrawableWrapper drawable) {

        updatePaint();

        final int scrollX = drawable.mScrollX;
        final int scrollY = drawable.mScrollY;
        final int pLeft = drawable.mPaddingLeft;
        final int pRight = drawable.mPaddingRight;
        final int pTop = drawable.mPaddingTop;
        final int pBottom = drawable.mPaddingBottom;

        canvas.clipRect(scrollX + pLeft, scrollY + pTop, scrollX + drawable.mViewWidth - pRight,
                scrollY + drawable.mViewHeight - pBottom);
        canvas.translate(pLeft, pTop);

        if (mArgs.mExtraMatrix != null) {
            canvas.concat(mArgs.mExtraMatrix);
        }

        drawContentReal(canvas, drawable);

        drawBorder(canvas, drawable);

    }

    /**
     * 更新绘制参数
     * 
     * @param args
     */
    public void updateArgs(DrawerArgs args) {
        mArgs = args;
    }

    /**
     * 更新画笔
     */
    private void updatePaint() {

        int alpha = (int) (255 * mArgs.mAlpha);
        mPaint.setAlpha(alpha);

        if (mArgs.mIsNight) {
            mPaint.setColorFilter(sColorFilterForSkin);
        } else {
            mPaint.setColorFilter(null);
        }
        mBorderPaint.setColor(mArgs.mBorderColor);
        mBorderPaint.setStrokeWidth(mArgs.mBorderWidth);
    }

    /**
     * @param st
     * @return copy from source code
     */
    private static Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType st) {
        int index = 1;
        if (st == ScaleType.FIT_XY) {
            index = 1;
        } else if (st == ScaleType.FIT_START) {
            index = 2;
        } else if (st == ScaleType.FIT_CENTER) {
            index = 3;
        } else if (st == ScaleType.FIT_END) {
            index = 4;
        }
        return sS2FArray[index - 1];
    }

    /**
     * 计算一个点经过矩阵运算后的位置
     * 
     * @param x
     * @param y
     * @param matrix
     * @return
     */
    protected void applyMatrix(float x, float y, Matrix matrix) {
        matrix.getValues(mValues);
        float rx = (int) (x * mValues[0] + y * mValues[1] + mValues[2]);
        float ry = (int) (x * mValues[3] + y * mValues[4] + mValues[5]);
        mPoint.set(rx, ry);
    }

    /**
     * 计算好矩阵后将矩阵应用到绘制逻辑，子类实现
     * 
     * @param bm
     * @param drawable
     */
    public abstract boolean applyBounds(DrawableWrapper drawable);

    /**
     * 真正绘制内容的方法，子类实现
     * 
     * @param canvas
     * @param drawable
     */
    public abstract void drawContentReal(Canvas canvas, DrawableWrapper drawable);

    /**
     * 绘制边框的方法，子类实现
     * 
     * @param canvas
     * @param drawable
     */
    public abstract void drawBorder(Canvas canvas, DrawableWrapper drawable);

}
