package com.baidu.iknow.imageloader.widgets;

import java.util.HashSet;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

public class CustomActivity extends FragmentActivity{

    public HashSet<CustomImageView> imageViews = new HashSet<>();

    public boolean isFastScroll;

}
