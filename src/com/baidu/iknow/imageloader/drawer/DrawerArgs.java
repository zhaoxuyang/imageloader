package com.baidu.iknow.imageloader.drawer;

import android.graphics.Matrix;
import android.graphics.Path;

/**
 * @author zhaoxuyang 绘制参数
 */
public class DrawerArgs {

    public float mRadius;

    public boolean mHasBorder = true;

    public int mBorderWidth;

    public int mBorderColor;

    public boolean mIsNight;

    public boolean mBorderSurroundContent;

    public float mAlpha = 1.0f;

    public Matrix mExtraMatrix;

    public Path mPath = new Path();

    public Path mBorderPath = new Path();

}
