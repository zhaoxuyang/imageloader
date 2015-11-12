package com.baidu.iknow.imageloader.widgets;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * 配合图片加载框架使用，实现快速滑动的时候，不异步加载图片
 * @author zhaoxuyang
 * @since 2015-10-12
 */
public class CustomListView extends ListView {

    private OnScrollListener mOnScrollListener;

    private Handler mHandler = new Handler();

    private Runnable mRefreshRunnable = new Runnable() {

        @Override
        public void run() {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                refreshView(getChildAt(i));
            }
        }
    };

    private void refreshView(View v) {
        if (v instanceof CustomImageView) {
            CustomImageView imageview = (CustomImageView) v;
            imageview.refresh();
        } else if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            int count = vg.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = vg.getChildAt(i);
                refreshView(view);
            }
        }

    }

    private OnScrollListener mProxyScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            System.out.println("scrollState:" + scrollState);
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(view, scrollState);
            }
            Context context = view.getContext();
            if (context instanceof CustomActivity) {
                CustomActivity ca = (CustomActivity) context;
                if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                    removeCallbacks(mRefreshRunnable);
                    ca.isFastScroll = true;
                } else if (ca.isFastScroll) {
                    ca.isFastScroll = false;
                    mHandler.removeCallbacks(mRefreshRunnable);
                    mHandler.postDelayed(mRefreshRunnable, 200);
                }
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

}
