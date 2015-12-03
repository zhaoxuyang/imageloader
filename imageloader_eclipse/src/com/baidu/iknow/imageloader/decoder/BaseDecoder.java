package com.baidu.iknow.imageloader.decoder;

import com.baidu.iknow.imageloader.drawable.CustomDrawable;

public abstract class BaseDecoder {

    public abstract boolean checkType(byte[] bytes);
    
    public abstract CustomDrawable doDecode(byte[] bytes,DecodeInfo decodeInfo,int viewWidth,int viewHeight);
}
