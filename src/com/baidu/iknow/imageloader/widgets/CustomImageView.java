package com.baidu.iknow.imageloader.widgets;

import com.baidu.iknow.imageloader.R;
import com.baidu.iknow.imageloader.cache.UrlSizeKey;
import com.baidu.iknow.imageloader.decoder.DecodeInfo;
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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
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
    private int mWidthMeasureMode;

    private int mHeightMeasureMode;

    private DrawableWrapper mDrawableWrapper;

    public boolean mNeedComputeBounds = true;

    private String mUrl;

    private boolean mHasFrame;

    private boolean mNeedResize;

    private DrawablePlayer mPlayer;

    private CustomActivity mActivity;

    private CustomImageBuilder mBuilder;

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
        if(mPendingScaleType==null){
            mPendingScaleType = ScaleType.FIT_XY;
        }
        mBuilder = new CustomImageBuilder(this);
        mBuilder.setScaleType(mPendingScaleType);
        try{
            if (attrs != null) {
                TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomImageView);

                mBuilder.mRadius = a.getDimensionPixelSize(R.styleable.CustomImageView_civ_radius,
                        dipToPixel(getContext(), DEFAULT_RADIUS));

                mBuilder.mHasBorder = a.getBoolean(R.styleable.CustomImageView_civ_hasBorder, true);
                mBuilder.mBorderWidth = a.getDimensionPixelSize(R.styleable.CustomImageView_civ_borderWidth,
                        dipToPixel(getContext(), DEFAULT_BORDER_WIDTH));
                mBuilder.mBorderColor = a.getColor(R.styleable.CustomImageView_civ_borderColor, DEFAULT_BORDER_COLOR);
                mBuilder.mBorderSurroundContent = a.getBoolean(R.styleable.CustomImageView_civ_borderSurroundContent, true);
                mBuilder.mIsNight = a.getBoolean(R.styleable.CustomImageView_civ_isNight, false);
                mBuilder.mAlpha = a.getFloat(R.styleable.CustomImageView_civ_alpha, 1.0f);
                mBuilder.mDrawerType = a.getInt(R.styleable.CustomImageView_civ_drawerType, DrawerFactory.NORMAL);
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
            Context context = getContext();
            if (context instanceof CustomActivity) {
                mActivity = (CustomActivity) context;
                mActivity.imageViews.add(this);
            }

            mBuilder.mergeDrawArgs();
            if(mPendingDrawable!=null){
                setImageDrawable(mPendingDrawable);
            }

            mIsVisible = getVisibility()==View.VISIBLE;
        }catch(Exception e){
            e.printStackTrace();
        }


    }

    @SuppressLint("NewApi")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int w;
        int h;

        boolean resizeWidth = false;
        boolean resizeHeight = false;
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

        }

        int pleft = getPaddingLeft();
        int pright = getPaddingRight();
        int ptop = getPaddingTop();
        int pbottom = getPaddingBottom();

        int widthSize;
        int heightSize;

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
        resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
        mNeedResize = resizeWidth || resizeHeight;

        w += pleft + pright;
        h += ptop + pbottom;

        w = Math.max(w, getSuggestedMinimumWidth());
        h = Math.max(h, getSuggestedMinimumHeight());

        widthSize = resolveSize(w, widthMeasureSpec);
        heightSize = resolveSize(h, heightMeasureSpec);

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        mHasFrame = true;
        return super.setFrame(l, t, r, b);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        stopLoad();
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
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if (mDrawableWrapper == null) {
            mDrawableWrapper = new DrawableWrapper();
        }
        mDrawableWrapper.mScrollX = x;
        mDrawableWrapper.mScrollY = y;
    }

    @Override
    public void requestLayout() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp != null && lp.width > 0 && lp.height > 0) {
            if (getWidth() == lp.width && getHeight() == lp.height) {
                return;
            }
        }
        super.requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // long startTime = System.currentTimeMillis();
        drawBackground(canvas);
        drawContent(canvas);
        // Log.d(TAG, (System.currentTimeMillis() - startTime) + "");
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

    /**
     * 绘制背景
     *
     * @param canvas
     */
    private void drawBackground(Canvas canvas) {
        final Drawable background = getBackground();
        if (background != null) {
            final int scrollX = mDrawableWrapper.mScrollX;
            final int scrollY = mDrawableWrapper.mScrollY;

            background.setBounds(0, 0, mDrawableWrapper.mViewWidth, mDrawableWrapper.mViewHeight);

            if ((scrollX | scrollY) == 0) {
                background.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                background.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }
    }

    private ScaleType mPendingScaleType;

    @Override
    public void setScaleType(ScaleType scaleType) {
        if(mBuilder==null){
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
        if(mBuilder==null){
            return super.getScaleType();
        }
        return mBuilder.mScaleType;
    }

    private Drawable mPendingDrawable;

    @Override
    public void setImageDrawable(Drawable drawable) {
        if(mBuilder==null){
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
            mDrawableWrapper.mDrawer = mDrawer;
        } else if (ImageLoader.getInstance().mFailedQuene.contains(mUrl)) {
            mDrawableWrapper.mDrawable = mBuilder.mErrorDrawable;
            mDrawableWrapper.mScaleType = mBuilder.mErrorScaleType;
            mDrawableWrapper.mDrawer = mErrorDrawer;
            drawable = mBuilder.mErrorDrawable;
        } else {
            mDrawableWrapper.mDrawable = mBuilder.mBlankDrawable;
            mDrawableWrapper.mScaleType = mBuilder.mBlankScaleType;
            mDrawableWrapper.mDrawer = mBlankDrawer;
            drawable = mBuilder.mBlankDrawable;
        }
        return drawable;
    }

    public Drawable getDrawableFromCache() {
        if (!mHasFrame || TextUtils.isEmpty(mUrl)) {
            return null;
        }
        CustomDrawable drawable = ImageLoader.getInstance().getMemmory(mUrl, mDrawableWrapper.mViewWidth,
                mDrawableWrapper.mViewHeight);
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

        mNeedComputeBounds = true;
        stopLoad();
        mUrl = url;
        startLoad();
        return;
    }

    private boolean isFastScroll() {
        boolean isFastScroll = false;
        if (mActivity != null) {
            isFastScroll = mActivity.isFastScroll;
        }
        return isFastScroll;
    }

    private void startLoad() {
        if (!mHasFrame) {
            return;
        }

        if(!mIsAttach || !mIsVisible){
            return;
        }

        ImageLoader.getInstance().load(mUrl, mDrawableWrapper.mViewWidth, mDrawableWrapper.mViewHeight,
                this, isFastScroll());
        requestLayout();
        invalidate();
    }

    private void stopLoad() {
        ImageLoader.getInstance().cancelLoad(mUrl, mDrawableWrapper.mViewWidth, mDrawableWrapper.mViewHeight, this);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer = null;
        }
        clearAnimation();
    }

    private boolean mIsAttach;

    private boolean mIsVisible;

    @Override
    public void onStartTemporaryDetach() {
        if (!mIsAttach) {
            return;
        }
        mIsAttach = false;
        super.onStartTemporaryDetach();
        if (mActivity != null) {
            mActivity.imageViews.remove(this);
        }
        stopLoad();
    }

    @Override
    public void onFinishTemporaryDetach() {
        if (mIsAttach) {
            return;
        }
        mIsAttach = true;
        super.onFinishTemporaryDetach();
        if (mActivity != null) {
            mActivity.imageViews.add(this);
        }
        startLoad();
    }

    @Override
    protected void onAttachedToWindow() {
        if (mIsAttach) {
            return;
        }
        mIsAttach = true;
        super.onAttachedToWindow();
        if (mActivity != null) {
            mActivity.imageViews.add(this);
        }
        startLoad();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!mIsAttach) {
            return;
        }
        mIsAttach = false;
        super.onDetachedFromWindow();
        if (mActivity != null) {
            mActivity.imageViews.remove(this);
        }
        stopLoad();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            stopLoad();
        } else {
            startLoad();
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility!=View.VISIBLE) {
            if(!mIsVisible){
                return;
            }
            mIsVisible = false;
            stopLoad();
        } else {
            if(mIsVisible){
                return;
            }
            mIsVisible = true;
            startLoad();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable dr) {
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
        startLoad();
    }

    @Override
    public void onLoadingStarted(UrlSizeKey key) {

    }

    @Override
    public void onLoadingFailed(UrlSizeKey key, Exception failReason) {
        if (!mIsAttach) {
            return;
        }
        if (key.mUrl.equals(mUrl) && key.mViewWidth == mDrawableWrapper.mViewWidth
                && key.mViewHeight == mDrawableWrapper.mViewHeight) {
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
        if (key.mUrl.equals(mUrl) && key.mViewWidth == mDrawableWrapper.mViewWidth
                && key.mViewHeight == mDrawableWrapper.mViewHeight) {
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
        if (key.mUrl.equals(mUrl) && key.mViewWidth == mDrawableWrapper.mViewWidth
                && key.mViewHeight == mDrawableWrapper.mViewHeight) {
            mNeedComputeBounds = true;
            if (mNeedResize) {
                requestLayout();
            }
        }

    }

    public CustomImageBuilder getBuilder(){
        return mBuilder;
    }


    public static class CustomImageBuilder {

        private boolean mContentChange;

        private int mDrawerType = DrawerFactory.NORMAL;

        private ScaleType mScaleType = ScaleType.FIT_XY;

        private boolean mBlankContentChange;

        private int mBlankRes = R.drawable.ic_default_picture;

        private ScaleType mBlankScaleType = ScaleType.FIT_XY;

        private int mBlankDrawerType = DrawerFactory.NORMAL;

        private Drawable mBlankDrawable;

        private boolean mErrorContentChange;

        private int mErrorRes = R.drawable.ic_default_picture;

        private ScaleType mErrorScaleType = ScaleType.FIT_XY;

        private int mErrorDrawerType = DrawerFactory.NORMAL;

        private Drawable mErrorDrawable;

        private float mRadius;

        private boolean mHasBorder;

        private int mBorderWidth;

        private int mBorderColor;

        private boolean mIsNight;

        private boolean mBorderSurroundContent;

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
            if (mDrawerType != drawerType) {
                mDrawerType = drawerType;
                mContentChange = true;
            }
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

        public CustomImageBuilder setBlankRes(int blankRes) {
            if (mBlankRes != blankRes) {
                mBlankRes = blankRes;
                mBlankContentChange = true;
            }
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
            if (mBlankDrawerType != blankDrawerType) {
                mBlankDrawerType = blankDrawerType;
                mBlankContentChange = true;
            }
            return this;
        }

        public CustomImageBuilder setErrorRes(int errorRes) {
            if (mErrorRes != errorRes) {
                mErrorRes = errorRes;
                mErrorContentChange = true;
            }
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
            if (mErrorDrawerType != errorDrawerType) {
                mErrorDrawerType = errorDrawerType;
                mErrorContentChange = true;
            }
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

        public CustomImageBuilder setBorderSurroundContent(boolean borderSurroundContent) {
            mBorderSurroundContent = borderSurroundContent;
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
            mArgs.mBorderSurroundContent = mBorderSurroundContent;
            mArgs.mAlpha = mAlpha;
            mArgs.mExtraMatrix = mExtraMatrix;
            mArgs.mPath = mPath;
            mArgs.mBorderPath = mBorderPath;
            mCiv.invalidate();
            return mArgs;
        }

        private void initBlank() {
            if (mBlankRes <= 0) {
                return;
            }
            Drawable drawable = mCiv.getContext().getResources().getDrawable(mBlankRes);
            if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                drawable = BitmapDrawableFactory
                        .createBitmapDrawable(((android.graphics.drawable.BitmapDrawable) drawable).getBitmap());
            }
            mBlankDrawable = drawable;
            mCiv.mNeedComputeBounds = true;
            mCiv.mBlankDrawer = DrawerFactory.getInstance(mCiv.getContext()).getDrawer(drawable, mBlankDrawerType,
                    mCiv.mBlankDrawer);
            mCiv.invalidate();
        }

        private void initError() {
            if (mErrorRes <= 0) {
                return;
            }
            Drawable drawable = mCiv.getContext().getResources().getDrawable(mErrorRes);
            if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                drawable = BitmapDrawableFactory
                        .createBitmapDrawable(((android.graphics.drawable.BitmapDrawable) drawable).getBitmap());
            }
            mErrorDrawable = drawable;
            mCiv.mNeedComputeBounds = true;
            mCiv.mErrorDrawer = DrawerFactory.getInstance(mCiv.getContext()).getDrawer(drawable, mErrorDrawerType,
                    mCiv.mErrorDrawer);
            mCiv.invalidate();
        }

        public CustomImageView build() {
            if (mCiv == null) {
                return null;
            }
            if (mBlankContentChange) {
                initBlank();
                mBlankContentChange = false;
            }

            if (mErrorContentChange) {
                initError();
                mErrorContentChange = false;
            }

            if (mContentChange) {
                Drawable drawable = mCiv.getDrawable();
                if (drawable != null) {
                    mCiv.mNeedComputeBounds = true;
                    mCiv.mDrawer = DrawerFactory.getInstance(mCiv.getContext()).getDrawer(drawable, mDrawerType,
                            mCiv.mDrawer);
                    mCiv.invalidate();
                }
                mContentChange = false;
            }

            mergeDrawArgs();
            return mCiv;
        }

    }

}
