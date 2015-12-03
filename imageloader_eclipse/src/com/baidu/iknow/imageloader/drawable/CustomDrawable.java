package com.baidu.iknow.imageloader.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

public abstract class CustomDrawable extends Drawable {

    @Override
    public void draw(Canvas canvas) {

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public abstract void recycle();

    public abstract boolean checkLegal();
    
    public abstract int getSize();

}
