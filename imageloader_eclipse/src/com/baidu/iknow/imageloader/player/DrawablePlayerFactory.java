package com.baidu.iknow.imageloader.player;

import com.baidu.iknow.imageloader.drawable.GifDrawable;
import com.baidu.iknow.imageloader.widgets.CustomImageView;

import android.graphics.drawable.Drawable;

public class DrawablePlayerFactory {

    private DrawablePlayerFactory(){
        
    }
    
    public static DrawablePlayer getPlayer(Drawable drawable,CustomImageView view){
        if(drawable instanceof GifDrawable){
            return new GifDrawablePlayer(view);
        }
        return null;
    }
}
