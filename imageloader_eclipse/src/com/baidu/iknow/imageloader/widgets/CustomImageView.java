package com.baidu.iknow.imageloader.widgets;

import com.baidu.iknow.imageloader.R;
import com.baidu.iknow.imageloader.cache.ImageLoaderLog;
import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.drawable.BitmapDrawable;
import com.baidu.iknow.imageloader.drawable.BitmapDrawable.BitmapDrawableFactory;
import com.baidu.iknow.imageloader.drawable.CustomDrawable;
import com.baidu.iknow.imageloader.drawable.DrawableWrapper;
import com.baidu.iknow.imageloader.drawer.AbsDrawer;
import com.baidu.iknow.imageloader.drawer.DrawerArgs;
import com.baidu.iknow.imageloader.drawer.DrawerFactory;
import com.baidu.iknow.imageloader.player.DrawablePlayer;
import com.baidu.iknow.imageloader.player.DrawablePlayerFactory;
import com.baidu.iknow.imageloader.request.ImageLoadingListener;
import com.baidu.iknow.imageloader.request.ImageLoader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * 自定义imageview 与图片数据解耦
 *
 * @author zhaoxuyang
 * @since 2015-9-17
 */
public class CustomImageView extends ImageView implements ImageLoadingListener {

    private static final String TAG = CustomImageView.class.getSimpleName();

    private final int DEFAULT_RADIUS = 4;

    private final int DEFAULT_BORDER_WIDTH = 1;

    private final int DEFAULT_BORDER_COLOR = 0x0C000000;

    private AbsDrawer mDrawer;

    private AbsDrawer mBlankDrawer;

    private AbsDrawer mErrorDrawer;

    /**
     * 记录原始的measuremode，用来处理wrapcontent
     */
    private int mWidthMeasureSpec;

    private int mHeightMeasureSpec;

    private DrawableWrapper mDrawableWrapper;

    public boolean mNeedComputeBounds = true;

    private String mUrl;

    private boolean mHasFrame;

    private boolean mNeedResize;

    private DrawablePlayer mPlayer;

    private CustomListView mListView;

    private CustomImageBuilder mBuilder;

    private ImageLoadingListener mListener;

    private boolean mAdjustViewBoundsCustom = false;

    private int mMaxWidthCustom = Integer.MAX_VALUE;

    private int mMaxHeightCustom = Integer.MAX_VALUE;

    private boolean mAdjustContentBounds = false;

    public CustomImageView(Context context) {
        super(context);
        init(null);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
        
    }

    /**
     * 初始化，主要是读取属性，填充绘制参数。
     *
     * @param attrs
     */
    private void init(AttributeSet attrs) {
        if (mPendingScaleType == null) {
            mPendingScaleType = ScaleType.FIT_XY;
        }
        mBuilder = new CustomImageBuilder(this);
        mBuilder.setScaleType(mPendingScaleType);
        try {
            if (attrs != null) {
                TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomImageView);

                mBuilder.mRadius = a.getDimensionPixelSize(R.styleable.CustomImageView_civ_radius,
                        dipToPixel(getContext(), DEFAULT_RADIUS));

                mBuilder.mHasBorder = a.getBoolean(R.styleable.CustomImageView_civ_hasBorder, false);
                mBuilder.mBorderWidth = a.getDimensionPixelSize(R.styleable.CustomImageView_civ_borderWidth,
                        dipToPixel(getContext(), DEFAULT_BORDER_WIDTH));
                mBuilder.mBorderColor = a.getColor(R.styleable.CustomImageView_civ_borderColor, DEFAULT_BORDER_COLOR);
                mBuilder.mIsNight = a.getBoolean(R.styleable.CustomImageView_civ_isNight, false);
                mBuilder.mAlpha = a.getFloat(R.styleable.CustomImageView_civ_alpha, 1.0f);
                mBuilder.mDrawerType = a.getInt(R.styleable.CustomImageView_civ_drawerType, DrawerFactory.NORMAL);
                mAdjustContentBounds = a.getBoolean(R.styleable.CustomImageView_civ_adjustContentBounds, false);
                mMaxWidthCustom = a.getDimensionPixelSize(R.styleable.CustomImageView_civ_maxWidth, Integer.MAX_VALUE);
                mMaxHeightCustom = a.getDimensionPixelSize(R.styleable.CustomImageView_civ_maxHeight, Integer.MAX_VALUE);
                a.recycle();
            } else {
                mBuilder.mRadius = dipToPixel(getContext(), DEFAULT_RADIUS);
                mBuilder.mBorderWidth = dipToPixel(getContext(), DEFAULT_BORDER_WIDTH);
                mBuilder.mBorderColor = DEFAULT_BORDER_COLOR;
                mBuilder.mDrawerType = DrawerFactory.NORMAL;
            }
            if (mDrawableWrapper == null) {
                mDrawableWrapper = new DrawableWrapper();
            }
            mDrawableWrapper.mPaddingLeft = getPaddingLeft();
            mDrawableWrapper.mPaddingRight = getPaddingRight();
            mDrawableWrapper.mPaddingTop = getPaddingTop();
            mDrawableWrapper.mPaddingBottom = getPaddingBottom();
            if (mPendingDrawable != null) {
                setImageDrawable(mPendingDrawable);
            }

            mBuilder.build();
            mIsVisible = getVisibility() == View.VISIBLE;
            getListView();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("NewApi")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int w;
        int h;

        float desiredAspect = 0.0f;

        boolean resizeWidth = false;
        boolean resizeHeight = false;
        mWidthMeasureSpec = widthMeasureSpec;
        mHeightMeasureSpec = heightMeasureSpec;
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        mNeedResize = (widthSpecMode != MeasureSpec.EXACTLY) || (heightSpecMode != MeasureSpec.EXACTLY);
        Drawable drawable = getDrawable();

        if (drawable == null) {
            w = h = 0;
        } else {
            w = drawable.getIntrinsicWidth();
            h = drawable.getIntrinsicHeight();
            if (w <= 0) {
                w = 1;
            }
            if (h <= 0) {
                h = 1;
            }
            if (mAdjustViewBoundsCustom || mAdjustContentBounds) {
                resizeWidth = (widthSpecMode != MeasureSpec.EXACTLY);
                resizeHeight = (heightSpecMode != MeasureSpec.EXACTLY);
                desiredAspect = (float) w / (float) h;
            }

        }

        int pleft = getPaddingLeft();
        int pright = getPaddingRight();
        int ptop = getPaddingTop();
        int pbottom = getPaddingBottom();

        int widthSize;
        int heightSize;

        if (resizeWidth || resizeHeight) {

            widthSize = resolveAdjustedSize(w + pleft + pright, mMaxWidthCustom, widthMeasureSpec);

            heightSize = resolveAdjustedSize(h + ptop + pbottom, mMaxHeightCustom, heightMeasureSpec);

            if (desiredAspect != 0.0f) {
                float actualAspect = (float) (widthSize - pleft - pright) / (heightSize - ptop - pbottom);

                if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {

                    boolean done = false;

                    if (resizeWidth) {
                        int newWidth = (int) (desiredAspect * (heightSize - ptop - pbottom)) + pleft + pright;

                        
                        if (mAdjustContentBounds) {
                            if (newWidth >= widthSize) {
                                widthSize = newWidth;
                                done = true;
                            }
                            if (!resizeHeight) {
                                widthSize = resolveAdjustedSize(newWidth, mMaxWidthCustom, widthMeasureSpec);
                            }

                        } else if (mAdjustViewBoundsCustom) {
                            if (!resizeHeight) {
                                widthSize = resolveAdjustedSize(newWidth, mMaxWidthCustom, widthMeasureSpec);
                            }
                            if (newWidth <= widthSize) {
                                widthSize = newWidth;
                                done = true;
                            }
                        }

                    }

                    if (!done && resizeHeight) {
                        int newHeight = (int) ((widthSize - pleft - pright) / desiredAspect) + ptop + pbottom;
                        
                        if (mAdjustContentBounds) {
                            if (newHeight >= heightSize) {
                                heightSize = newHeight;
                            }
                            if (!resizeWidth) {
                                heightSize = resolveAdjustedSize(newHeight, mMaxHeightCustom, heightMeasureSpec);
                            }
                        } else if (mAdjustViewBoundsCustom) {
                            if (!resizeWidth) {
                                heightSize = resolveAdjustedSize(newHeight, mMaxHeightCustom, heightMeasureSpec);
                            }
                            if (newHeight <= heightSize) {
                                heightSize = newHeight;
                            }
                        }

                    }
                }
            }
        } else {
            w += pleft + pright;
            h += ptop + pbottom;

            w = Math.max(w, getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());

            w = Math.min(w, mMaxWidthCustom);
            h = Math.min(h, mMaxHeightCustom);
            widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
            heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    private int resolveAdjustedSize(int desiredSize, int maxSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = Math.min(desiredSize, maxSize);
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(Math.min(desiredSize, specSize), maxSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        mHasFrame = true;
        return super.setFrame(l, t, r, b);
    }
    
    

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        stopLoad(true);
        mDrawableWrapper.mViewWidth = w;
        mDrawableWrapper.mViewHeight = h;
        mNeedComputeBounds = true;
        startLoad();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        if (mDrawableWrapper == null) {
            mDrawableWrapper = new DrawableWrapper();
        }
        mDrawableWrapper.mPaddingLeft = left;
        mDrawableWrapper.mPaddingRight = right;
        mDrawableWrapper.mPaddingTop = top;
        mDrawableWrapper.mPaddingBottom = bottom;
        mNeedComputeBounds = true;
        invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if (mDrawableWrapper == null) {
            mDrawableWrapper = new DrawableWrapper();
        }
        mDrawableWrapper.mScrollX = x;
        mDrawableWrapper.mScrollY = y;
        mNeedComputeBounds = true;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // long startTime = System.currentTimeMillis();
        drawContent(canvas);
        // Log.d(TAG, (System.currentTimeMillis() - startTime) + "");
    }

    @Override
    public void setMaxWidth(int maxWidth) {
        mMaxWidthCustom = maxWidth;
    }

    public int getMaxWidth() {
        return mMaxWidthCustom;
    }

    @Override
    public void setMaxHeight(int maxHeight) {
        mMaxHeightCustom = maxHeight;
    }

    public int getMaxHeight() {
        return mMaxHeightCustom;
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        super.setAdjustViewBounds(adjustViewBounds);
        mAdjustViewBoundsCustom = adjustViewBounds;
    }
    
    public void setAdjustContentBounds(boolean adjustContentBounds){
        mAdjustContentBounds = adjustContentBounds;
    }

    /**
     * 绘制内容
     *
     * @param canvas
     */
    private void drawContent(Canvas canvas) {
        getDrawable();
        if (mDrawableWrapper.mDrawable == null || mDrawableWrapper.mDrawer == null) {
            return;
        }
        mDrawableWrapper.mDrawer.updateArgs(mBuilder.mArgs);
        mDrawableWrapper.mNeedComputeBounds = mNeedComputeBounds;
        mDrawableWrapper.draw(canvas);
        mDrawableWrapper.mDrawable = null;
        mNeedComputeBounds = false;
    }

    private ScaleType mPendingScaleType;

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (mBuilder == null) {
            mPendingScaleType = scaleType;
            return;
        }
        if (mBuilder.mScaleType != scaleType) {
            mBuilder.mScaleType = scaleType;
            mNeedComputeBounds = true;
            invalidate();
        }
    }

    @Override
    public ScaleType getScaleType() {
        if (mBuilder == null) {
            return super.getScaleType();
        }
        return mBuilder.mScaleType;
    }

    private Drawable mPendingDrawable;

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (mBuilder == null) {
            mPendingDrawable = drawable;
            return;
        }
        if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
            BitmapDrawable bd = BitmapDrawableFactory
                    .createBitmapDrawable(((android.graphics.drawable.BitmapDrawable) drawable).getBitmap());
            drawable = bd;
            mDrawer = DrawerFactory.getInstance(getContext()).getDrawer(drawable, mBuilder.mDrawerType, mDrawer);
        }
        mNeedComputeBounds = true;
        super.setImageDrawable(drawable);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        Drawable drawable = getDrawable();
        setImageDrawable(drawable);
    }

    @Override
    public void setImageMatrix(Matrix matrix) {
        mDrawableWrapper.mCustomMatrix.set(matrix);
    }

    @Override
    public Matrix getImageMatrix() {
        if (mDrawer == null) {
            return new Matrix();
        }
        return mDrawer.getDrawMatrix();
    }

    @Override
    public Drawable getDrawable() {
        Drawable drawable = getDrawableFromCache();
        drawable = drawable != null ? drawable : super.getDrawable();

        if (mPlayer == null) {
            mPlayer = DrawablePlayerFactory.getPlayer(drawable, this);
        }
        if (mPlayer != null) {
            drawable = mPlayer.play(drawable, isFastScroll());
        }

        if (drawable != null) {
            mDrawableWrapper.mDrawable = drawable;
            mDrawableWrapper.mScaleType = mBuilder.mScaleType;
            mDrawableWrapper.mMatrixScaleType = mBuilder.mMatrixScaleType;
            mDrawableWrapper.mDrawer = DrawerFactory.getInstance(getContext()).getDrawer(drawable, mBuilder.mDrawerType,
                    mDrawer);

        } else {
            if (!mIsLoad) {
                startLoad();
            }
            if (ImageLoader.getInstance().mFailedQuene.contains(mUrl)) {
                mDrawableWrapper.mDrawable = mBuilder.mErrorDrawable;
                mDrawableWrapper.mScaleType = mBuilder.mErrorScaleType;
                mDrawableWrapper.mMatrixScaleType = mBuilder.mMatrixScaleType;
                mDrawableWrapper.mDrawer = mErrorDrawer;
                drawable = mBuilder.mErrorDrawable;
            } else {
                mDrawableWrapper.mDrawable = mBuilder.mBlankDrawable;
                mDrawableWrapper.mScaleType = mBuilder.mBlankScaleType;
                mDrawableWrapper.mMatrixScaleType = mBuilder.mMatrixScaleType;
                mDrawableWrapper.mDrawer = mBlankDrawer;
                drawable = mBuilder.mBlankDrawable;
            }
        }
        return drawable;
    }

    public Drawable getDrawableFromCache() {
        if (TextUtils.isEmpty(mUrl)) {
            return null;
        }
        int[] res = getKeySize();
        CustomDrawable drawable = ImageLoader.getInstance().getMemmory(mUrl, res[0], res[1]);
        return drawable;
    }

    public void url(String url) {
        if (mUrl == null) {
            if (url == null) {
                return;
            }
        } else if (mUrl.equals(url)) {
            return;
        }
        ImageLoaderLog.d(TAG, "VISIBLE:" + mIsVisible);

        mNeedComputeBounds = true;
        mIsVisible = true;
        stopLoad(true);
        mUrl = url;
        startLoad();
        requestLayout();
        return;
    }

    public void file(String filePath) {
        if (!filePath.startsWith("file:///")) {
            filePath = "file:///" + filePath;
        }
        url(filePath);
    }

    private boolean isFastScroll() {
        boolean isFastScroll = false;
        if (mListView != null) {
            isFastScroll = mListView.isFastScroll;
        }
        return isFastScroll;
    }

    private void startLoad() {
        if (!mHasFrame) {
            return;
        }

        if (!mIsAttach || !mIsVisible) {
            return;
        }

        int[] res = getKeySize();
        mIsLoad = true;
        ImageLoader.getInstance().loadImage(mUrl, res[0], res[1], this, isFastScroll(), true);
        invalidate();
    }

    private void stopLoad(boolean cancelRunning) {
        int[] res = getKeySize();
        mIsLoad = false;
        ImageLoader.getInstance().cancelLoad(mUrl, res[0], res[1], this, cancelRunning);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer = null;
        }
        clearAnimation();
    }

    private boolean mIsAttach;

    private boolean mIsVisible = true;

    private boolean mIsLoad = false;

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        if (!mIsAttach) {
            return;
        }
        mIsAttach = false;
        if (mListView != null) {
            mListView.imageViews.remove(this);
        }
        stopLoad(false);
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        if (mIsAttach) {
            return;
        }
        mIsAttach = true;
        if (mListView != null) {
            mListView.imageViews.add(this);
        }
        startLoad();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIsAttach) {
            return;
        }
        mIsAttach = true;
        mIsVisible = getVisibility() == View.VISIBLE;
        getListView();
        if (mListView != null) {
            mListView.imageViews.add(this);
        }
        startLoad();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!mIsAttach) {
            return;
        }
        mIsAttach = false;
        if (mListView != null) {
            mListView.imageViews.remove(this);
        }
        stopLoad(true);
        mListView = null;
    }

    private void getListView() {
        if (mListView != null) {
            return;
        }
        ViewParent parent = getParent();
        while (parent instanceof View) {
            if (parent instanceof CustomListView) {
                mListView = (CustomListView) parent;
                break;
            }
            parent = parent.getParent();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            stopLoad(false);
        } else {
            startLoad();
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE) {
            if (!mIsVisible) {
                return;
            }
            mIsVisible = false;
            stopLoad(false);
        } else {
            if (mIsVisible) {
                return;
            }
            mIsVisible = true;
            startLoad();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != View.VISIBLE) {
            if (!mIsVisible) {
                return;
            }
            mIsVisible = false;
            stopLoad(false);
        } else {
            if (mIsVisible) {
                return;
            }
            mIsVisible = true;
            startLoad();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable dr) {
        super.verifyDrawable(dr);
        return true;
    }

    /**
     * dip转px
     *
     * @param context
     * @param dip
     *
     * @return
     */
    public static int dipToPixel(Context context, int dip) {
        float density = context.getResources().getDisplayMetrics().density;
        int res = (int) (dip * density);
        return res;
    }

    void refresh() {
        ImageLoaderLog.d(TAG, " refresh VISIBLE:" + mIsVisible);
        startLoad();
    }

    @Override
    public void onLoadingStarted(UrlSizeKey key) {
        if (mListener != null) {
            mListener.onLoadingStarted(key);
        }
    }

    @Override
    public void onLoadingFailed(UrlSizeKey key, Exception failReason) {
        if (!mIsAttach) {
            return;
        }
        if (mListener != null) {
            mListener.onLoadingFailed(key, failReason);
        }
        int[] res = getKeySize();
        if (key.mUrl.equals(mUrl) && key.mViewWidth == res[0] && key.mViewHeight == res[1]) {
            mNeedComputeBounds = true;
            clearAnimation();
            invalidate();
            if (mNeedResize) {
                requestLayout();
            }
        }
    }

    @Override
    public void onLoadingComplete(UrlSizeKey key, CustomDrawable drawable, boolean fromMemmoryCache) {
        if (!mIsAttach) {
            return;
        }
        if (mListener != null) {
            mListener.onLoadingComplete(key, drawable, fromMemmoryCache);
        }
        int[] res = getKeySize();
        if (key.mUrl.equals(mUrl) && key.mViewWidth == res[0] && key.mViewHeight == res[1]) {
            mNeedComputeBounds = true;
            clearAnimation();
            mDrawer = DrawerFactory.getInstance(getContext()).getDrawer(drawable, mBuilder.mDrawerType, mDrawer);
            if (!fromMemmoryCache) {
                Animation anim = new AlphaAnimation(0, 1);
                anim.setInterpolator(new DecelerateInterpolator());
                anim.setDuration(1000);
                anim.setFillAfter(true);
                startAnimation(anim);
            }
            invalidate();
            if (mNeedResize) {
                requestLayout();
            }
        }
    }

    @Override
    public void onLoadingCancelled(UrlSizeKey key) {
        if (!mIsAttach) {
            return;
        }
        if (mListener != null) {
            mListener.onLoadingCancelled(key);
        }
        int[] res = getKeySize();
        if (key.mUrl.equals(mUrl) && key.mViewWidth == res[0] && key.mViewHeight == res[1]) {
            mNeedComputeBounds = true;
            if (mNeedResize) {
                requestLayout();
            }
        }

    }

    private int[] getKeySize() {
        int[] res = new int[2];
        int vw = mDrawableWrapper.mViewWidth;
        int vh = mDrawableWrapper.mViewHeight;
        if (mNeedResize) {
            vw = 0;
            vh = 0;
        }
        res[0] = vw;
        res[1] = vh;
        return res;
    }

    public CustomImageView setImageLoadingListener(ImageLoadingListener listener) {
        mListener = listener;
        return this;
    }

    public CustomImageBuilder getBuilder() {
        return mBuilder;
    }

    public static class CustomImageBuilder {

        public static int BLANK_DEFAULT = R.drawable.ic_default_picture;

        public static int ERROR_DEFAULT = R.drawable.ic_default_picture;

        private int mDrawerType = DrawerFactory.NORMAL;

        private ScaleType mScaleType = ScaleType.FIT_XY;

        private MatrixScaleType mMatrixScaleType = MatrixScaleType.MATRIX;

        private int mBlankRes = BLANK_DEFAULT;

        private ScaleType mBlankScaleType = null;

        private int mBlankDrawerType = -1;

        private Drawable mBlankDrawable;

        private int mErrorRes = ERROR_DEFAULT;

        private ScaleType mErrorScaleType = null;

        private int mErrorDrawerType = -1;

        private Drawable mErrorDrawable;

        private float mRadius;

        private boolean mHasBorder;

        private int mBorderWidth;

        private int mBorderColor;

        private boolean mIsNight;

        private float mAlpha = 1.0f;

        private Matrix mExtraMatrix;

        private Path mPath = new Path();

        private Path mBorderPath = new Path();

        private DrawerArgs mArgs;

        private CustomImageView mCiv;

        private CustomImageBuilder(CustomImageView civ) {
            mCiv = civ;
            mArgs = new DrawerArgs();
            initBlank();
            initError();
        }

        public CustomImageBuilder setDrawerType(int drawerType) {
            mDrawerType = drawerType;
            return this;
        }

        public CustomImageBuilder setScaleType(ScaleType scaleType) {
            if (mScaleType != scaleType) {
                mScaleType = scaleType;
                mCiv.mNeedComputeBounds = true;
                mCiv.invalidate();
            }
            return this;
        }

        public CustomImageBuilder setMatrixScaleType(MatrixScaleType matrixScaleType) {
            if (mScaleType != ScaleType.MATRIX) {
                return this;
            }
            if (mMatrixScaleType != matrixScaleType) {
                mMatrixScaleType = matrixScaleType;
                mCiv.mNeedComputeBounds = true;
                mCiv.invalidate();
            }
            return this;
        }

        public CustomImageBuilder setBlankRes(int blankRes) {
            mBlankRes = blankRes;
            return this;
        }

        public CustomImageBuilder setBlankDrawable(Drawable blankDrawable) {
            mBlankDrawable = blankDrawable;
            return this;
        }

        public CustomImageBuilder setBlankScaleType(ScaleType blankScaleType) {
            if (mBlankScaleType != blankScaleType) {
                mBlankScaleType = blankScaleType;
                mCiv.mNeedComputeBounds = true;
                mCiv.invalidate();
            }
            return this;
        }

        public CustomImageBuilder setBlankDrawerType(int blankDrawerType) {
            mBlankDrawerType = blankDrawerType;
            return this;
        }

        public CustomImageBuilder setErrorRes(int errorRes) {
            mErrorRes = errorRes;
            return this;
        }

        public CustomImageBuilder setErrorDrawable(Drawable errorDrawable) {
            mErrorDrawable = errorDrawable;
            return this;
        }

        public CustomImageBuilder setErrorScaleType(ScaleType errorScaleType) {
            if (mErrorScaleType != errorScaleType) {
                mErrorScaleType = errorScaleType;
                mCiv.mNeedComputeBounds = true;
                mCiv.invalidate();
            }
            return this;
        }

        public CustomImageBuilder setErrorDrawerType(int errorDrawerType) {
            mErrorDrawerType = errorDrawerType;
            return this;
        }

        public CustomImageBuilder setRadius(float radius) {
            mRadius = radius;
            return this;
        }

        public CustomImageBuilder setHasBorder(boolean hasBorder) {
            mHasBorder = hasBorder;
            return this;
        }

        public CustomImageBuilder setBorderWidth(int borderWidth) {
            mBorderWidth = borderWidth;
            return this;
        }

        public CustomImageBuilder setBorderColor(int borderColor) {
            mBorderColor = borderColor;
            return this;
        }

        public CustomImageBuilder setNight(boolean night) {
            mIsNight = night;
            return this;
        }

        public CustomImageBuilder setAlpha(float alpha) {
            mAlpha = alpha;
            return this;
        }

        public CustomImageBuilder setExtraMatrix(Matrix extraMatrix) {
            mExtraMatrix = extraMatrix;
            return this;
        }

        public CustomImageBuilder setCustomPath(Path path) {
            mPath = path;
            return this;
        }

        public CustomImageBuilder setCustomBorderPath(Path borderPath) {
            mBorderPath = borderPath;
            return this;
        }

        private DrawerArgs mergeDrawArgs() {
            mArgs.mRadius = mRadius;
            mArgs.mHasBorder = mHasBorder;
            mArgs.mBorderWidth = mBorderWidth;
            mArgs.mBorderColor = mBorderColor;
            mArgs.mIsNight = mIsNight;
            mArgs.mAlpha = mAlpha;
            mArgs.mExtraMatrix = mExtraMatrix;
            mArgs.mPath = mPath;
            mArgs.mBorderPath = mBorderPath;
            mCiv.invalidate();
            return mArgs;
        }

        private void initBlank() {
            if (mBlankRes > 0) {
                Drawable drawable = mCiv.getContext().getResources().getDrawable(mBlankRes);
                if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                    drawable = BitmapDrawableFactory
                            .createBitmapDrawable(((android.graphics.drawable.BitmapDrawable) drawable).getBitmap());
                }
                mBlankDrawable = drawable;
            }
            if (mBlankDrawable == null) {
                return;
            }
            mCiv.mNeedComputeBounds = true;
            mCiv.mBlankDrawer = DrawerFactory.getInstance(mCiv.getContext()).getDrawer(mBlankDrawable, mBlankDrawerType,
                    mCiv.mBlankDrawer);
            mCiv.invalidate();
        }

        private void initError() {
            if (mErrorRes > 0) {
                Drawable drawable = mCiv.getContext().getResources().getDrawable(mErrorRes);
                if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                    drawable = BitmapDrawableFactory
                            .createBitmapDrawable(((android.graphics.drawable.BitmapDrawable) drawable).getBitmap());
                }
                mErrorDrawable = drawable;
            }
            if (mErrorDrawable == null) {
                return;
            }
            mCiv.mNeedComputeBounds = true;
            mCiv.mErrorDrawer = DrawerFactory.getInstance(mCiv.getContext()).getDrawer(mErrorDrawable, mErrorDrawerType,
                    mCiv.mErrorDrawer);
            mCiv.invalidate();
        }

        public CustomImageView build() {

            if (mBlankDrawerType == -1) {
                mBlankDrawerType = mDrawerType;
            }
            if (mBlankScaleType == null) {
                mBlankScaleType = mScaleType;
                mCiv.mNeedComputeBounds = true;
            }

            if (mErrorDrawerType == -1) {
                mErrorDrawerType = mDrawerType;
            }
            if (mErrorScaleType == null) {
                mErrorScaleType = mScaleType;
                mCiv.mNeedComputeBounds = true;
            }

            initBlank();
            initError();
            if (mErrorDrawable == null) {
                mErrorDrawable = mBlankDrawable;
                mCiv.mErrorDrawer = mCiv.mBlankDrawer;
            }
            Drawable drawable = mCiv.getDrawable();
            if (drawable != null) {
                mCiv.mNeedComputeBounds = true;
                mCiv.mDrawer = DrawerFactory.getInstance(mCiv.getContext()).getDrawer(drawable, mDrawerType,
                        mCiv.mDrawer);
                mCiv.invalidate();
            }
            mergeDrawArgs();
            return mCiv;
        }

    }

    /**
     * scaletype为matrix时，可以设置
     */
    public enum MatrixScaleType {
        /**
         * 就按原有的
         */
        MATRIX(0), /**
                    * 头部要完整显示
                    */
        TOP_CROP(1), /**
                      * 以宽度填满控件为准，高度相应的缩放
                      */
        FIT_WIDTH(2),

        /**
         * 以高度填满控件为准，宽度相应的缩放
         */
        FIT_HEIGHT(3);

        MatrixScaleType(int ni) {
            nativeInt = ni;
        }

        final int nativeInt;
    }

}
