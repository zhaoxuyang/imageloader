package com.baidu.iknow.imageloader.widgets;

import java.util.Iterator;

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
           if(mActivity!=null){
               Iterator<CustomImageView> iterator = mActivity.imageViews.iterator();
               while(iterator.hasNext()){
                   CustomImageView imageview = iterator.next();
                   imageview.refresh();
               }
           }
        }
    };

    private CustomActivity mActivity;


    private OnScrollListener mProxyScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            System.out.println("scrollState:" + scrollState);
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(view, scrollState);
            }
            if (mActivity!=null) {
                if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                    removeCallbacks(mRefreshRunnable);
                    mActivity.isFastScroll = true;
                } else if (mActivity.isFastScroll) {
                    mActivity.isFastScroll = false;
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
        Context context = getContext();
        if(context instanceof CustomActivity){
            mActivity = (CustomActivity) context;
        }
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        super.setOnScrollListener(mProxyScrollListener);
        if (l != mProxyScrollListener) {
            mOnScrollListener = l;
        }

    }

}
