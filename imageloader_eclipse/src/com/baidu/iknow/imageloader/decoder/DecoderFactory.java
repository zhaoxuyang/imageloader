package com.baidu.iknow.imageloader.decoder;

import java.util.ArrayList;
import java.util.Iterator;

public class DecoderFactory {

    private static ArrayList<BaseDecoder> sCustomDecoder = new ArrayList<BaseDecoder>();

    private static BaseDecoder sCommonDecoder = new BitmapDecoder();
    static {
        sCustomDecoder.add(new GifDecoder());
        sCustomDecoder.add(new WebPDecoder());
    }

    private DecoderFactory() {

    }

    public static BaseDecoder getDecoder(byte[] bytes) {
        Iterator<BaseDecoder> iter = sCustomDecoder.iterator();
        while (iter.hasNext()) {
            BaseDecoder decoder = iter.next();
            if (decoder.checkType(bytes)) {
                return decoder;
            }
        }
        return sCommonDecoder;
    }

}
