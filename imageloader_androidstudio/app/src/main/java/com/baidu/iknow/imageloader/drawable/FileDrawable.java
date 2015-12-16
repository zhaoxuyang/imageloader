package com.baidu.iknow.imageloader.drawable;

import java.io.File;

/**
 * Created by zhaoxuyang on 15/12/15.
 */
public class FileDrawable extends CustomDrawable{

    public File file;

    public FileDrawable(File f){
        file = f;
    }


    @Override
    public void recycle() {

    }

    @Override
    public boolean checkLegal() {
        return true;
    }

    @Override
    public int getSize() {
        return 0;
    }
}
