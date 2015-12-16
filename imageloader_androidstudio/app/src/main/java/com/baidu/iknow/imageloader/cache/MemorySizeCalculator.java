package com.baidu.iknow.imageloader.cache;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * 
 * @author zhaoxuyang
 * @since 2015-10-13
 */
public final class MemorySizeCalculator {
    private static final String TAG = "MemorySizeCalculator";
    static final int BYTES_PER_ARGB_8888_PIXEL = 4;
    static final int LOW_MEMORY_BYTE_ARRAY_POOL_DIVISOR = 2;

    private final int bitmapPoolSize;
    private final int memoryCacheSize;
    private final Context context;
    private final int arrayPoolSize;
    private final int gifPoolSize;

    interface ScreenDimensions {
        int getWidthPixels();

        int getHeightPixels();
    }

    MemorySizeCalculator(Context context, ActivityManager activityManager, ScreenDimensions screenDimensions,
            float memoryCacheScreens, float bitmapPoolScreens, float gifPoolScreens, int targetArrayPoolSize,
            float maxSizeMultiplier, float lowMemoryMaxSizeMultiplier) {
        this.context = context;
        arrayPoolSize = isLowMemoryDevice(activityManager) ? targetArrayPoolSize / LOW_MEMORY_BYTE_ARRAY_POOL_DIVISOR
                : targetArrayPoolSize;
        final int maxSize = getMaxSize(activityManager, maxSizeMultiplier, lowMemoryMaxSizeMultiplier);

        final int screenSize = screenDimensions.getWidthPixels() * screenDimensions.getHeightPixels()
                * BYTES_PER_ARGB_8888_PIXEL;

        int targetPoolSize = Math.round(screenSize * bitmapPoolScreens);
        int targetMemoryCacheSize = Math.round(screenSize * memoryCacheScreens);
        int targetGifPoolSize = Math.round(screenSize * gifPoolScreens);
        int availableSize = maxSize - arrayPoolSize;

        if ((targetMemoryCacheSize + targetPoolSize + targetGifPoolSize)<= availableSize) {
            memoryCacheSize = targetMemoryCacheSize;
            bitmapPoolSize = targetPoolSize;
            gifPoolSize = targetGifPoolSize;
        } else {
            float part = availableSize / (bitmapPoolScreens + memoryCacheScreens + gifPoolScreens);
            memoryCacheSize = Math.round(part * memoryCacheScreens);
            bitmapPoolSize = Math.round(part * bitmapPoolScreens);
            gifPoolSize = Math.round(part * gifPoolScreens);
        }

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            ImageLoaderLog.d(TAG, "Calculation complete" + ", Calculated memory cache size: " + toMb(memoryCacheSize)
                    + ", pool size: " + toMb(bitmapPoolSize) + ", byte array size: " + toMb(arrayPoolSize)
                    + ", memory class limited? " + (targetMemoryCacheSize + targetPoolSize > maxSize) + ", max size: "
                    + toMb(maxSize) + ", memoryClass: " + activityManager.getMemoryClass() + ", isLowMemoryDevice: "
                    + isLowMemoryDevice(activityManager));
        }
    }

    /**
     * Returns the recommended memory cache size for the device it is run on in
     * bytes.
     */
    public int getMemoryCacheSize() {
        return memoryCacheSize;
    }

    /**
     * Returns the recommended bitmap pool size for the device it is run on in
     * bytes.
     */
    public int getBitmapPoolSize() {
        return bitmapPoolSize;
    }

    /**
     * Returns the recommended array pool size for the device it is run on in
     * bytes.
     */
    public int getArrayPoolSizeInBytes() {
        return arrayPoolSize;
    }

    public int getGifBitmapPoolSize() {
        return gifPoolSize;
    }

    private static int getMaxSize(ActivityManager activityManager, float maxSizeMultiplier,
            float lowMemoryMaxSizeMultiplier) {
        final int memoryClassBytes = activityManager.getMemoryClass() * 1024 * 1024;
        final boolean isLowMemoryDevice = isLowMemoryDevice(activityManager);
        return Math.round(memoryClassBytes * (isLowMemoryDevice ? lowMemoryMaxSizeMultiplier : maxSizeMultiplier));
    }

    private String toMb(int bytes) {
        return Formatter.formatFileSize(context, bytes);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean isLowMemoryDevice(ActivityManager activityManager) {
        final int sdkInt = Build.VERSION.SDK_INT;
        return sdkInt < Build.VERSION_CODES.HONEYCOMB
                || (sdkInt >= Build.VERSION_CODES.KITKAT && activityManager.isLowRamDevice());
    }

    public static final class Builder {
        
        public static int MEMORY_CACHE_TARGET_SCREENS = 1;
        public static int BITMAP_POOL_TARGET_SCREENS = 2;
        public static int GIF_BITMAP_POOL_TARGET_SCREENS = 1;
        public static float MAX_SIZE_MULTIPLIER = 0.4f;
        public static float LOW_MEMORY_MAX_SIZE_MULTIPLIER = 0.33f;
        // 4MB.
        public static int ARRAY_POOL_SIZE_BYTES = 4 * 1024 * 1024;

        private final Context context;

        // Modifiable for testing.
        private ActivityManager activityManager;
        private ScreenDimensions screenDimensions;

        private float memoryCacheScreens = MEMORY_CACHE_TARGET_SCREENS;
        private float bitmapPoolScreens = BITMAP_POOL_TARGET_SCREENS;
        private float gifPoolScreens = GIF_BITMAP_POOL_TARGET_SCREENS;
        private float maxSizeMultiplier = MAX_SIZE_MULTIPLIER;
        private float lowMemoryMaxSizeMultiplier = LOW_MEMORY_MAX_SIZE_MULTIPLIER;
        private int arrayPoolSizeBytes = ARRAY_POOL_SIZE_BYTES;

        public Builder(Context context) {
            this.context = context;
            activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            screenDimensions = new DisplayMetricsScreenDimensions(context.getResources().getDisplayMetrics());
        }

        public Builder setMemoryCacheScreens(float memoryCacheScreens) {
            this.memoryCacheScreens = memoryCacheScreens;
            return this;
        }

        public Builder setBitmapPoolScreens(float bitmapPoolScreens) {
            this.bitmapPoolScreens = bitmapPoolScreens;
            return this;
        }

        public Builder setMaxSizeMultiplier(float maxSizeMultiplier) {
            this.maxSizeMultiplier = maxSizeMultiplier;
            return this;
        }

        public Builder setLowMemoryMaxSizeMultiplier(float lowMemoryMaxSizeMultiplier) {
            this.lowMemoryMaxSizeMultiplier = lowMemoryMaxSizeMultiplier;
            return this;
        }

        public Builder setArrayPoolSize(int arrayPoolSizeBytes) {
            this.arrayPoolSizeBytes = arrayPoolSizeBytes;
            return this;
        }

        // Visible for testing.
        Builder setActivityManager(ActivityManager activityManager) {
            this.activityManager = activityManager;
            return this;
        }

        // Visible for testing.
        Builder setScreenDimensions(ScreenDimensions screenDimensions) {
            this.screenDimensions = screenDimensions;
            return this;
        }

        public MemorySizeCalculator build() {
            return new MemorySizeCalculator(context, activityManager, screenDimensions, memoryCacheScreens,
                    bitmapPoolScreens, gifPoolScreens, arrayPoolSizeBytes, maxSizeMultiplier,
                    lowMemoryMaxSizeMultiplier);
        }
    }

    private static final class DisplayMetricsScreenDimensions implements ScreenDimensions {
        private final DisplayMetrics displayMetrics;

        public DisplayMetricsScreenDimensions(DisplayMetrics displayMetrics) {
            this.displayMetrics = displayMetrics;
        }

        @Override
        public int getWidthPixels() {
            return displayMetrics.widthPixels;
        }

        @Override
        public int getHeightPixels() {
            return displayMetrics.heightPixels;
        }
    }
}
