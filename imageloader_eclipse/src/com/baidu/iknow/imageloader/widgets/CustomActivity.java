package com.baidu.iknow.imageloader.widgets;

import java.util.HashSet;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

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
