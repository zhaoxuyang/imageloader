/* gif的jni封装
 *
 *
 * @author zhaoxuyang
 */
#include <stdlib.h>
#include <android/log.h>
#include <errno.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <android/bitmap.h>
#include "BitmapLock.h"

#define TAG "BitmapLock"

JNIEXPORT void JNICALL lockBitmap(JNIEnv *env, jclass clazz, jobject obj) {
	char *buffer = NULL;
	AndroidBitmap_lockPixels(env, obj, (void **) &buffer);
}

JNIEXPORT void JNICALL unlockBitmap(JNIEnv *env, jclass clazz, jobject obj) {
	AndroidBitmap_unlockPixels(env, obj);
}

static JNINativeMethod BitmapLockMethods[] = { { "lockBitmap",
		"(Landroid/graphics/Bitmap;)V", (void *) lockBitmap }, { "unlockBitmap",
		"(Landroid/graphics/Bitmap;)V", (void *) unlockBitmap } };

static int registNativeMethods(JNIEnv *env) {

	jclass clazz = (*env)->FindClass(env, "com/baidu/iknow/imageloader/bitmap/BitmapLock");
	if (clazz == NULL ) {
		__android_log_print(ANDROID_LOG_DEBUG, TAG,
				"find bitmaplock class error");
		return -1;
	}

	if ((*env)->RegisterNatives(env, clazz, BitmapLockMethods,
			sizeof(BitmapLockMethods) / sizeof(BitmapLockMethods[0])) < 0) {
		__android_log_print(ANDROID_LOG_DEBUG, TAG,
				"register bitmaplock native methods error");
		return -1;
	}

	return 1;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv *env = NULL;
	if (((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4)) != JNI_OK) {
		return -1;
	}

	__android_log_print(ANDROID_LOG_DEBUG, TAG, "init bitmaplock filed");

	if (registNativeMethods(env) < 0) {
		return -1;
	}

	__android_log_print(ANDROID_LOG_DEBUG, TAG, "register methods");

	__android_log_print(ANDROID_LOG_DEBUG, TAG, "jni onload");

	return JNI_VERSION_1_4;
}
