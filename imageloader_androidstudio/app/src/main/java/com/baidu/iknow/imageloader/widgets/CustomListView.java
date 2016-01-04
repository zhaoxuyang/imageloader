package com.baidu.iknow.imageloader.widgets;

import java.util.HashSet;
import java.util.Iterator;

import com.baidu.iknow.imageloader.cache.ImageLoaderLog;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * 配合图片加载框架使用，实现快速滑动的时候，不异步加载图片
 *
 * @author zhaoxuyang
 * @since 2015-10-12
 */
public class CustomListView extends ListView {

    private static final String TAG = CustomListView.class.getSimpleName();

    public HashSet<CustomImageView> imageViews = new HashSet<>();

    public boolean isFastScroll;

    private OnScrollListener mOnScrollListener;

    private Handler mHandler = new Handler();

    private Runnable mRefreshRunnable = new Runnable() {

        @Override
        public void run() {
            Iterator<CustomImageView> iterator = imageViews.iterator();
            while (iterator.hasNext()) {
                CustomImageView imageview = iterator.next();
                imageview.refresh();
            }
        }
    };

    private OnScrollListener mProxyScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(view, scrollState);
            }
            if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                isFastScroll = true;
            } else if (isFastScroll) {
                isFastScroll = false;
                mHandler.removeCallbacks(mRefreshRunnable);
                mHandler.postDelayed(mRefreshRunnable, 200);
                ImageLoaderLog.d(TAG, "fast scroll end");
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    };

    public CustomListView(Context context) {
        super(context);
        init();
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CustomListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private void init() {
        setOnScrollListener(mProxyScrollListener);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        super.setOnScrollListener(mProxyScrollListener);
        if (l != mProxyScrollListener) {
            mOnScrollListener = l;
        }

    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        isFastScroll = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isFastScroll = false;
    }
}
