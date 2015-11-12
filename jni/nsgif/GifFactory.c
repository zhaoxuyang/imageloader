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
#include "libnsgif.h"
#include "GifFactory.h"

#define TAG "GifFactoryNative"

#define PIXEL_SIZE 4

jfieldID gifPtrId;
jfieldID gifWidthId;
jfieldID gifHeightId;
jfieldID gifFrameCountId;
jmethodID gifConstructorId;
jmethodID gifReleaseId;
jclass gifClass;
jfieldID inJustDecodeBoundsId;
jfieldID inSampleSizeId;
jfieldID outWidthId;
jfieldID outHeightId;

unsigned char *load_file(const char *path, size_t *data_size);
void warning(const char *context, int code);
void *bitmap_create(int width, int height);
void bitmap_set_opaque(void *bitmap, bool opaque);
bool bitmap_test_opaque(void *bitmap);
unsigned char *bitmap_get_buffer(void *bitmap);
void bitmap_destroy(void *bitmap);
void bitmap_modified(void *bitmap);

gif_bitmap_callback_vt bitmap_callbacks = { bitmap_create, bitmap_destroy,
		bitmap_get_buffer, bitmap_set_opaque, bitmap_test_opaque,
		bitmap_modified };

unsigned char *load_file(const char *path, size_t *data_size) {
	FILE *fd;
	struct stat sb;
	unsigned char *buffer;
	size_t size;
	size_t n;

	fd = fopen(path, "rb");
	if (!fd) {
		perror(path);
		exit(EXIT_FAILURE);
	}

	if (stat(path, &sb)) {
		perror(path);
		exit(EXIT_FAILURE);
	}
	size = sb.st_size;

	buffer = malloc(size);
	if (!buffer) {
		fprintf(stderr, "Unable to allocate %lld bytes\n",
		(long long) size);
		exit(EXIT_FAILURE);
	}

	n = fread(buffer, 1, size, fd);
	if (n != size) {
		perror(path);
		exit(EXIT_FAILURE);
	}

	fclose(fd);

	*data_size = size;
	return buffer;
}

void warning(const char *context, gif_result code) {
	fprintf(stderr, "%s failed: ", context);
	switch (code)
	{
		case GIF_INSUFFICIENT_FRAME_DATA:
		fprintf(stderr, "GIF_INSUFFICIENT_FRAME_DATA");
		break;
		case GIF_FRAME_DATA_ERROR:
		fprintf(stderr, "GIF_FRAME_DATA_ERROR");
		break;
		case GIF_INSUFFICIENT_DATA:
		fprintf(stderr, "GIF_INSUFFICIENT_DATA");
		break;
		case GIF_DATA_ERROR:
		fprintf(stderr, "GIF_DATA_ERROR");
		break;
		case GIF_INSUFFICIENT_MEMORY:
		fprintf(stderr, "GIF_INSUFFICIENT_MEMORY");
		break;
		default:
		fprintf(stderr, "unknown code %i", code);
		break;
	}
	fprintf(stderr, "\n");
}

void *bitmap_create(int width, int height) {
	return calloc(width * height, PIXEL_SIZE);
}

void bitmap_set_opaque(void *bitmap, bool opaque) {
	(void) opaque; /* unused */
}

bool bitmap_test_opaque(void *bitmap) {
	return false;
}

unsigned char *bitmap_get_buffer(void *bitmap) {
	return bitmap;
}

void bitmap_destroy(void *bitmap) {
	free(bitmap);
}

void bitmap_modified(void *bitmap) {
	return;
}

uint8_t multialpha(uint8_t a, uint8_t b) {
	uint8_t prod = a * b + 128;
	return (prod + (prod >> 8)) >> 8;
}

bool endian(void)
{
  char tmp[4]={1,0,0,0};
  return (*(int *)tmp)==1;
}

jobject decodeBytes(JNIEnv *env, char *data, size_t size, jobject opts) {

	gif_animation *gif = (gif_animation *) malloc(sizeof(gif_animation));

	gif_result code;
	unsigned int i;

	/* create our gif animation */
	gif_create(gif, &bitmap_callbacks);

	/* begin decoding */
	do {
		code = gif_initialise(gif, size, data);
		if (code != GIF_OK && code != GIF_WORKING) {
			warning("gif_initialise", code);
			gif_finalise(gif);
			return NULL ;
		}
	} while (code != GIF_OK);

	bool just_bounds = false;
	size_t sample_size = 1;
	if (opts != NULL ) {
		just_bounds = (*env)->GetBooleanField(env, opts, inJustDecodeBoundsId);
		sample_size = (*env)->GetIntField(env, opts, inSampleSizeId);
	}

	unsigned int width = gif->width;
	unsigned int height = gif->height;

	if (just_bounds) {
		(*env)->SetIntField(env, opts, outWidthId, width);
		(*env)->SetIntField(env, opts, outHeightId, height);
		gif_finalise(gif);
		return NULL ;
	}

	int temp = sample_size;
	int count = 0;
	if (temp % 2 == 0) {
		temp /= 2;
		count++;
	}
	if (1 != temp) {
		count++;
	}
	temp = 1;
	for (i = 0; i < count; i++) {
		temp *= 2;
	}
	sample_size = temp;
	if (sample_size < 1) {
		sample_size = 1;
	}

	width /= sample_size;
	height /= sample_size;
	for (i = 0; i < gif->frame_count; i++) {
		code = gif_decode_frame(gif, i);
		if (code != GIF_OK) {
			gif_finalise(gif);
			return NULL ;
		}
		if ((gif->frames[i].frame_image = gif->bitmap_callbacks.bitmap_create(
				width, height)) == NULL ) {
			gif_finalise(gif);
			return NULL ;
		}
		gif->decodedCount++;
		if (sample_size == 1) {
			memcpy(gif->frames[i].frame_image, gif->frame_image,
					(gif->width * gif->height * sizeof(int)));
		} else {
			int fx0 = sample_size >> 1;
			int fy0 = sample_size >> 1;
			int fdx = sample_size;
			int fdy = sample_size;
			uint32_t *dst = (uint32_t *) gif->frames[i].frame_image;
			uint8_t* source = (uint8_t *) gif->frame_image;
			int y = 0;
			for (y = 0;; y++) {
				uint8_t* src = source;
				src += fx0 * PIXEL_SIZE;
				int x = 0;
				for (x = 0; x < width; x++) {
					uint8_t alpha = src[3];
					uint8_t r = src[0];
					uint8_t g = src[1];
					uint8_t b = src[2];
					if (alpha != 255) {
						r = multialpha(r, alpha);
						g = multialpha(g, alpha);
						b = multialpha(b, alpha);
					}

					if(!endian()){
						dst[x] = alpha << 0 | r << 24 | g << 16 | b << 8;
					}else{
						dst[x] = alpha << 24 | r << 0 | g << 8 | b << 16;
					}
					src += fdx * PIXEL_SIZE;
				}
				dst += width;
				if ((height - 1) == y) {
					break;
				}
				source += fdy * gif->width * PIXEL_SIZE;
			}

		}

	}

	__android_log_print(ANDROID_LOG_DEBUG, TAG, "P3\n");
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "# width                %u \n",
			gif->width);
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "# height               %u \n",
			gif->height);
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "# frame_count          %u \n",
			gif->frame_count);
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "# frame_count_partial  %u \n",
			gif->frame_count_partial);
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "# loop_count           %u \n",
			gif->loop_count);
	__android_log_print(ANDROID_LOG_DEBUG, TAG, "%u %u 256\n", gif->width,
			gif->height * gif->frame_count);

	if (gif->frame_image) {
		if (gif->bitmap_callbacks.bitmap_destroy != NULL ) {
			gif->bitmap_callbacks.bitmap_destroy(gif->frame_image);
		}
		gif->frame_image = NULL;
	}
	jobject gifObj = (*env)->NewObject(env, gifClass, gifConstructorId);
	if (gifObj != NULL ) {
		(*env)->SetIntField(env, gifObj, gifPtrId, (jint) gif);
		(*env)->SetIntField(env, gifObj, gifWidthId, (jint) width);
		(*env)->SetIntField(env, gifObj, gifHeightId, (jint) height);
		(*env)->SetIntField(env, gifObj, gifFrameCountId,
				(jint) gif->frame_count);
	}

	return gifObj;
}

JNIEXPORT jobject JNICALL decodeFromFile(JNIEnv *env, jclass clazz,
		jstring path, jobject opts) {
	const char *pathCstr = (*env)->GetStringUTFChars(env, path, NULL );
	/* load file into memory */
	size_t size;
	unsigned char *data = load_file(pathCstr, &size);
	jobject gif = decodeBytes(env, data, size, opts);
	free(data);
	(*env)->ReleaseStringUTFChars(env, path, pathCstr);
	return gif;
}


JNIEXPORT jobject JNICALL decodeFromByteArray(JNIEnv *env, jclass clazz,
		jbyteArray bytes, jobject opts) {
	char * cbytes = (char *) (*env)->GetByteArrayElements(env, bytes, NULL );
	size_t size = (*env)->GetArrayLength(env, bytes);
	jobject gif = decodeBytes(env, cbytes, size, opts);
	(*env)->ReleaseByteArrayElements(env, bytes, cbytes, 0);
	return gif;
}

JNIEXPORT jint JNICALL writeFrameToBitmap(JNIEnv *env, jobject obj, jint index,
		jobject bm) {
	gif_animation *gif = (gif_animation *) (*env)->GetIntField(env, obj,
			gifPtrId);
	int width = (*env)->GetIntField(env,obj,gifWidthId);
	int height = (*env)->GetIntField(env,obj,gifHeightId);
	unsigned char *image = (unsigned char *) gif->frames[index].frame_image;
	char *buffer = NULL;
	AndroidBitmap_lockPixels(env, bm, (void **) &buffer);
	memcpy(buffer, image, (width * height * PIXEL_SIZE));
	AndroidBitmap_unlockPixels(env, bm);
	jint delay = gif->frames[index].frame_delay * 10;
	if(delay<=0){
		delay = 100;
	}
	return delay;
}
JNIEXPORT void JNICALL nativeRelease(JNIEnv *env, jobject obj) {
	gif_animation *gif = (gif_animation *) (*env)->GetIntField(env, obj,
			gifPtrId);
	gif_finalise(gif);
}

static JNINativeMethod gifMethods[] = { { "writeFrameToBitmap",
		"(ILandroid/graphics/Bitmap;)I", (void *) writeFrameToBitmap }, {
		"nativeRelease", "()V", (void *) nativeRelease } };

static JNINativeMethod gifFactoryMethods[] =
		{
				{ "decodeFromFileNative",
						"(Ljava/lang/String;Lcom/baidu/iknow/imageloader/gif/GifFactory$Options;)Lcom/baidu/iknow/imageloader/gif/Gif;",
						(void *) decodeFromFile },
				{ "decodeFromByteArrayNative",
						"([BLcom/baidu/iknow/imageloader/gif/GifFactory$Options;)Lcom/baidu/iknow/imageloader/gif/Gif;",
						(void *) decodeFromByteArray } };

static int registNativeMethods(JNIEnv *env) {

	if ((*env)->RegisterNatives(env, gifClass, gifMethods,
			sizeof(gifMethods) / sizeof(gifMethods[0])) < 0) {
		__android_log_print(ANDROID_LOG_DEBUG, TAG,
				"register gift native methods error");
		return -1;
	}

	jclass clazz = (*env)->FindClass(env,
			"com/baidu/iknow/imageloader/gif/GifFactory");
	if (clazz == NULL ) {
		__android_log_print(ANDROID_LOG_DEBUG, TAG,
				"find giftfactory class error");
		return -1;
	}

	if ((*env)->RegisterNatives(env, clazz, gifFactoryMethods,
			sizeof(gifFactoryMethods) / sizeof(gifFactoryMethods[0])) < 0) {
		__android_log_print(ANDROID_LOG_DEBUG, TAG,
				"register giftfactory native methods error");
		return -1;
	}

	return 1;
}

static int initGifId(JNIEnv *env) {
	jclass temp = (*env)->FindClass(env, "com/baidu/iknow/imageloader/gif/Gif");
	if (temp == NULL ) {
		__android_log_print(ANDROID_LOG_DEBUG, TAG, "find gift class error");
		return -1;
	}
	gifClass = (*env)->NewGlobalRef(env, temp);
	gifPtrId = (*env)->GetFieldID(env, gifClass, "mPtr", "I");
	gifWidthId = (*env)->GetFieldID(env, gifClass, "mWidth", "I");
	gifHeightId = (*env)->GetFieldID(env, gifClass, "mHeight", "I");
	gifFrameCountId = (*env)->GetFieldID(env, gifClass, "mFrameCount", "I");

	gifConstructorId = (*env)->GetMethodID(env, gifClass, "<init>", "()V");

	jclass clazz = (*env)->FindClass(env,
			"com/baidu/iknow/imageloader/gif/GifFactory$Options");
	if (clazz == NULL ) {
		__android_log_print(ANDROID_LOG_DEBUG, TAG,
				"find giftfactory options class error");
		return -1;
	}

	inJustDecodeBoundsId = (*env)->GetFieldID(env, clazz, "inJustDecodeBounds",
			"Z");
	inSampleSizeId = (*env)->GetFieldID(env, clazz, "inSampleSize", "I");
	outWidthId = (*env)->GetFieldID(env, clazz, "outWidth", "I");
	outHeightId = (*env)->GetFieldID(env, clazz, "outHeight", "I");
	return 1;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv *env = NULL;
	if (((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4)) != JNI_OK) {
		return -1;
	}

	if (initGifId(env) < 0) {
		return -1;
	}

	__android_log_print(ANDROID_LOG_DEBUG, TAG, "init gif filed");

	if (registNativeMethods(env) < 0) {
		return -1;
	}

	__android_log_print(ANDROID_LOG_DEBUG, TAG, "register methods");

	__android_log_print(ANDROID_LOG_DEBUG, TAG, "jni onload");

	return JNI_VERSION_1_4;
}
