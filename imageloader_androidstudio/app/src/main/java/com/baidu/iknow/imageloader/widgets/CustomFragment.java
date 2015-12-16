package com.baidu.iknow.imageloader.widgets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.baidu.iknow.imageloader.cache.ImageLoaderLog;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

/**
 * 使用懒加载需要继承这个fragment
 * @author zhaoxuyang
 * @since 2015-10-12
 */
public class CustomFragment extends Fragment{

    private static final String TAG = CustomFragment.class.getSimpleName();

    static Method dispatchVisibilityChanged;

    static {
        try {
            dispatchVisibilityChanged = ViewGroup.class.getDeclaredMethod("dispatchVisibilityChanged",View.class,int
                    .class);
            dispatchVisibilityChanged.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void changeVisible(boolean visible){
        View view = getView();
        if(view!=null && view instanceof ViewGroup){
            ViewGroup vg = (ViewGroup) view;
            try {
                ImageLoaderLog.d(TAG,visible+"");
                dispatchVisibilityChanged.invoke(vg,vg,visible?View.VISIBLE:View.GONE);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        changeVisible(isVisibleToUser);
    }

    @Override
    public void onStart() {
        super.onStart();
        changeVisible(true);
    }

    @Override
    public void onStop(){
        super.onStop();
        changeVisible(false);
    }
}
