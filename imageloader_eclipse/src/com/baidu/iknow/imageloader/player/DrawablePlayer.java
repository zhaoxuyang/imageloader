package com.baidu.iknow.imageloader.player;

import com.baidu.iknow.imageloader.widgets.CustomImageView;

import android.graphics.drawable.Drawable;

public abstract class DrawablePlayer {

    protected CustomImageView mView;

    public DrawablePlayer(CustomImageView view) {
        mView = view;
    }

    public abstract Drawable play(Drawable drawable, boolean isFastScroll);

    public abstract void stop();
}
