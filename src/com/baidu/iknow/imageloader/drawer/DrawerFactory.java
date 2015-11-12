package com.baidu.iknow.imageloader.drawer;

import com.baidu.iknow.imageloader.drawable.CustomDrawable;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * 绘制器工厂
 * 
 * @author zhaoxuyang
 * 
 */
public class DrawerFactory {

    public static final int NORMAL = 0;

    public static final int ROUND_RECT = 1;

    public static final int CIRCLE = 2;

    private static DrawerFactory mInstance;

    private int mDensity;

    private DrawerFactory(Context context) {
        mDensity = context.getResources().getDisplayMetrics().densityDpi;
    }

    public synchronized static DrawerFactory getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DrawerFactory(context);
        }
        return mInstance;
    }

    public AbsDrawer getDrawer(Drawable drawable, int type, AbsDrawer drawer) {
        
        if (drawable == null) {
            return null;
        }
        
        Class clazz = null;
        if (drawable instanceof CustomDrawable) {
            if (CIRCLE == type) {
                clazz = CircleBitmapDrawer.class;
            } else if (ROUND_RECT == type) {
                clazz = RoundRectBitmapDrawer.class;
            } else {
                clazz = NormalBitmapDrawer.class;
            }
        } else {
            clazz = NormalDrawer.class;
        }

        if (drawer != null && drawer.getClass() == clazz) {
            return drawer;
        }

        try {
            drawer = (AbsDrawer) clazz.newInstance();
            drawer.setDensity(mDensity);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return drawer;
    }

}
