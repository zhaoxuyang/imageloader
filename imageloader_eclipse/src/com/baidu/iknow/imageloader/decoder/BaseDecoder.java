package com.baidu.iknow.imageloader.decoder;

import javax.microedition.khronos.opengles.GL10;

import com.baidu.iknow.imageloader.drawable.CustomDrawable;
import com.baidu.iknow.imageloader.drawable.SizeDrawable;

import android.opengl.GLES10;

public abstract class BaseDecoder {

    private static final int DEFAULT_MAX_BITMAP_DIMENSION = 2048;

    private static final float INTERVAL_ROUNDING = 1.0f / 3;

    public static int maxBitmapSize;

    static {
        int[] maxTextureSize = new int[1];
        GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        maxBitmapSize = Math.max(maxTextureSize[0], DEFAULT_MAX_BITMAP_DIMENSION);
    }

    public abstract boolean checkType(byte[] bytes);

    public abstract CustomDrawable doDecode(byte[] bytes, DecodeInfo decodeInfo, int viewWidth, int viewHeight);

    public abstract SizeDrawable getSize(byte[] bytes, DecodeInfo decodeInfo);

    private float getMaxRatio(int bitmapWidth, int bitmapHeight, int viewWidth, int viewHeight) {
        if (viewWidth <= 0 || viewHeight <= 0) {
            return 1;
        }
        final float widthRatio = ((float) viewWidth) / bitmapWidth;
        final float heightRatio = ((float) viewHeight) / bitmapHeight;
        float ratio = Math.max(widthRatio, heightRatio);
        return ratio;
    }

    private int ratioToSampleSize(float ratio) {
        if (ratio > 0.5f + 0.5f * INTERVAL_ROUNDING) {
            return 1;
        }
        int sampleSize = 2;
        while (true) {
            double intervalLength = 1.0 / (2 * sampleSize);
            double compare = (1.0 / (2 * sampleSize)) + (intervalLength * INTERVAL_ROUNDING);
            if (compare <= ratio) {
                return sampleSize;
            }
            sampleSize *= 2;
        }
    }

    protected int getSampleSize(DecodeInfo decodeInfo, int bitmapWidth, int bitmapHeight, int viewWidth,
            int viewHeight) {
        int sampleSize = 1;
        if (decodeInfo.mScaleToFitView) {
            float ratio = getMaxRatio(bitmapWidth, bitmapHeight, viewWidth, viewHeight);
            sampleSize = ratioToSampleSize(ratio);
            bitmapWidth /= sampleSize;
            bitmapHeight /= sampleSize;
        }

        while (bitmapWidth > maxBitmapSize || bitmapHeight > maxBitmapSize) {
            sampleSize *= 2;
            bitmapWidth /= sampleSize;
            bitmapHeight /= sampleSize;
        }
        return sampleSize;
    }

}
