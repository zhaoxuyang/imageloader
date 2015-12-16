package com.baidu.iknow.imageloader.widgets;

import java.util.HashSet;

import android.support.v4.app.FragmentActivity;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

/**
 * 使用懒加载需要继承这个activity
 * @author zhaoxuyang
 * @since 2015-10-12
 */
public class CustomActivity extends FragmentActivity{

    public HashSet<CustomImageView> imageViews = new HashSet<>();

    public boolean isFastScroll;

    public HashSet<CustomListView> mListViews = new HashSet<>();

    @Override
    protected void onResume() {
        super.onResume();
        for(CustomListView listview : mListViews){
            if(listview!=null){
                ListAdapter adapter = listview.getAdapter();
                if(adapter!=null && adapter instanceof BaseAdapter){
                    ((BaseAdapter) adapter).notifyDataSetChanged();
                }
            }
        }

    }
}
