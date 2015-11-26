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
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
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

    /**
     * 绘制所需要的参数
     */
    protected DrawerArgs mArgs = new DrawerArgs();

    private AbsDrawer mDrawer;

    private int mDrawerType;

    private ScaleType mScaleType = ScaleType.FIT_XY;

    /**
     * 记录原始的measuremode，用来处理wrapcontent
     */
    private int mWidthMeasureMode;

    private int mHeightMeasureMode;

    private int mBlankRes;

    private Drawable mBlankDrawable;

    private ScaleType mBlankScaleType = ScaleType.FIT_XY;

    private int mBlankDrawerType;

    private AbsDrawer mBlankDrawer;

    private int mErrorRes;

    private Drawable mErrorDrawable;

    private ScaleType mErrorScaleType = ScaleType.FIT_XY;

    private int mErrorDrawerType;

    private AbsDrawer mErrorDrawer;

    private DrawableWrapper mDrawableWrapper;

    public boolean mNeedComputeBounds = true;

    private String mUrl;

    private DecodeInfo mDecodeInfo;

    private boolean mHasFrame;

    private boolean mNeedResize;

    private DrawablePlayer mPlayer;

    private CustomActivity mActivity;

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
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomImageView);

            mArgs.mRadius = a.getDimensionPixelSize(R.styleable.CustomImageView_civ_radius,
                    dipToPixel(getContext(), DEFAULT_RADIUS));

            mArgs.mHasBorder = a.getBoolean(R.styleable.CustomImageView_civ_hasBorder, true);
            mArgs.mBorderWidth = a.getDimensionPixelSize(R.styleable.CustomImageView_civ_borderWidth,
                    dipToPixel(getContext(), DEFAULT_BORDER_WIDTH));
            mArgs.mBorderColor = a.getColor(R.styleable.CustomImageView_civ_borderColor, DEFAULT_BORDER_COLOR);
            mArgs.mBorderSurroundContent = a.getBoolean(R.styleable.CustomImageView_civ_borderSurroundContent, true);
            mArgs.mIsNight = a.getBoolean(R.styleable.CustomImageView_civ_isNight, false);
            mArgs.mAlpha = a.getFloat(R.styleable.CustomImageView_civ_alpha,1.0f);
            mDrawerType = a.getInt(R.styleable.CustomImageView_civ_drawerType, DrawerFactory.NORMAL);
            a.recycle();
        } else {
            mArgs.mRadius = dipToPixel(getContext(), DEFAULT_RADIUS);
            mArgs.mBorderWidth = dipToPixel(getContext(), DEFAULT_BORDER_WIDTH);
            mArgs.mBorderColor = DEFAULT_BORDER_COLOR;
            mDrawerType = DrawerFactory.NORMAL;
        }
        if (mDrawableWrapper == null) {
            mDrawableWrapper = new DrawableWrapper();
        }
        mDrawableWrapper.mPaddingLeft = getPaddingLeft();
        mDrawableWrapper.mPaddingRight = getPaddingRight();
        mDrawableWrapper.mPaddingTop = getPaddingTop();
        mDrawableWrapper.mPaddingBottom = getPaddingBottom();
        mDecodeInfo = new DecodeInfo.DecodeInfoBuilder().build();
        Context context = getContext();
        if(context instanceof  CustomActivity){
            mActivity = (CustomActivity) context;
            mActivity.imageViews.add(this);
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
            if (w <= 0)
                w = 1;
            if (h <= 0)
                h = 1;

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
        mDrawableWrapper.mDrawer.updateArgs(mArgs);
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

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            mNeedComputeBounds = true;
            invalidate();
        }
    }

    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
            BitmapDrawable bd = BitmapDrawableFactory
                    .createBitmapDrawable(((android.graphics.drawable.BitmapDrawable) drawable).getBitmap());
            drawable = bd;
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

    public void setDecodeInfo(DecodeInfo decodeInfo) {
        this.mDecodeInfo = decodeInfo;
    }

    @Override
    public Drawable getDrawable() {
        Drawable drawable = super.getDrawable();
        drawable = drawable != null ? drawable : getDrawableFromCache();
        if (mPlayer == null) {
            mPlayer = DrawablePlayerFactory.getPlayer(drawable, this);
        }
        if (mPlayer != null) {
            drawable = mPlayer.play(drawable, isFastScroll());
        }
        if (drawable != null) {
            mDrawableWrapper.mDrawable = drawable;
            mDrawableWrapper.mScaleType = mScaleType;
            mDrawableWrapper.mDrawer = mDrawer;
        } else if (ImageLoader.getInstance().mFailedQuene.contains(mUrl)) {
            mDrawableWrapper.mDrawable = mErrorDrawable;
            mDrawableWrapper.mScaleType = mErrorScaleType;
            mDrawableWrapper.mDrawer = mErrorDrawer;
            drawable = mErrorDrawable;
        } else {
            mDrawableWrapper.mDrawable = mBlankDrawable;
            mDrawableWrapper.mScaleType = mBlankScaleType;
            mDrawableWrapper.mDrawer = mBlankDrawer;
            drawable = mBlankDrawable;
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

    public CustomImageView scaleType(ScaleType scaleType) {
        setScaleType(scaleType);
        return this;
    }

    public CustomImageView drawerType(int drawerType) {
        if (mDrawerType != drawerType) {
            mDrawerType = drawerType;
            Drawable drawable = getDrawable();
            if (drawable != null) {
                mNeedComputeBounds = true;
                mDrawer = DrawerFactory.getInstance(getContext()).getDrawer(drawable, mDrawerType, mDrawer);
                invalidate();
            }
        }
        return this;
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
        if (mActivity!=null) {
            isFastScroll = mActivity.isFastScroll;
        }
        return isFastScroll;
    }

    private void startLoad() {
        if (!mHasFrame) {
            return;
        }

        ImageLoader.getInstance().load(mUrl, mDrawableWrapper.mViewWidth, mDrawableWrapper.mViewHeight, mDecodeInfo,
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

    public CustomImageView blankImage(int resid, ScaleType scaleType, int drawerType) {
        if (mBlankRes == resid && scaleType == mBlankScaleType && drawerType == mBlankDrawerType) {
            return this;
        }
        mBlankRes = resid;
        mBlankScaleType = scaleType;
        mBlankDrawerType = drawerType;
        mBlankDrawable = getContext().getResources().getDrawable(mBlankRes);
        if (mBlankDrawable instanceof android.graphics.drawable.BitmapDrawable) {
            mBlankDrawable = BitmapDrawableFactory
                    .createBitmapDrawable(((android.graphics.drawable.BitmapDrawable) mBlankDrawable).getBitmap());
        }
        mBlankDrawer = DrawerFactory.getInstance(getContext())
                .getDrawer(mBlankDrawable, mBlankDrawerType, mBlankDrawer);
        return this;
    }

    public CustomImageView errorImage(int resid, ScaleType scaleType, int drawerType) {
        if (mErrorRes == resid && scaleType == mErrorScaleType && drawerType == mErrorDrawerType) {
            return this;
        }
        mErrorRes = resid;
        mErrorScaleType = scaleType;
        mErrorDrawerType = drawerType;
        mErrorDrawable = getContext().getResources().getDrawable(resid);
        if (mErrorDrawable instanceof android.graphics.drawable.BitmapDrawable) {
            mErrorDrawable = BitmapDrawableFactory
                    .createBitmapDrawable(((android.graphics.drawable.BitmapDrawable) mErrorDrawable).getBitmap());
        }
        mErrorDrawer = DrawerFactory.getInstance(getContext()).getDrawer(mErrorDrawable, drawerType, mErrorDrawer);
        return this;
    }

    private boolean mIsAttach;


    @Override
    public void onStartTemporaryDetach() {
        if(!mIsAttach){
            return;
        }
        mIsAttach = false;
        super.onStartTemporaryDetach();
        if(mActivity!=null){
            mActivity.imageViews.remove(this);
        }
        stopLoad();
    }

    @Override
    public void onFinishTemporaryDetach() {
        if(mIsAttach){
            return;
        }
        mIsAttach = true;
        super.onFinishTemporaryDetach();
        if(mActivity!=null){
            mActivity.imageViews.add(this);
        }
        startLoad();
    }

    @Override
    protected void onAttachedToWindow() {
        if(mIsAttach){
            return;
        }
        mIsAttach = true;
        super.onAttachedToWindow();
        if(mActivity!=null){
            mActivity.imageViews.add(this);
        }
        startLoad();
    }

    @Override
    protected void onDetachedFromWindow() {
        if(!mIsAttach){
            return;
        }
        mIsAttach = false;
        super.onDetachedFromWindow();
        if(mActivity!=null){
            mActivity.imageViews.remove(this);
        }
        stopLoad();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            if(!mIsAttach){
                return;
            }
            mIsAttach = false;
            stopLoad();
        } else {
            if(mIsAttach){
                return;
            }
            mIsAttach = true;
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
        if(!mIsAttach){
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
        if(!mIsAttach){
            return;
        }
        if (key.mUrl.equals(mUrl) && key.mViewWidth == mDrawableWrapper.mViewWidth
                && key.mViewHeight == mDrawableWrapper.mViewHeight) {
            mNeedComputeBounds = true;
            clearAnimation();
            mDrawer = DrawerFactory.getInstance(getContext()).getDrawer(drawable, mDrawerType, mDrawer);
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
        if(!mIsAttach){
            return;
        }
        if (mNeedResize) {
            requestLayout();
        }
    }

    public DrawerArgs getDrawerArgs(){
        return mArgs;
    }

}
