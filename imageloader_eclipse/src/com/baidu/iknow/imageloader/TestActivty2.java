package com.baidu.iknow.imageloader;

import com.baidu.iknow.imageloader.widgets.CustomImageView;
import com.baidu.iknow.imageloader.widgets.CustomImageView.MatrixScaleType;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView.ScaleType;

public class TestActivty2 extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list2);
        CustomImageView civ = (CustomImageView) findViewById(R.id.single);
        civ.getBuilder().setScaleType(ScaleType.MATRIX).setMatrixScaleType(MatrixScaleType.FIT_WIDTH).build().url("http://hiphotos.baidu.com/exp/wh%3D450%2C600/sign=9a82a12d4336acaf59b59ef849e9a126/f603918fa0ec08fa0050f1b351ee3d6d55fbda56.jpg");
    }
}
