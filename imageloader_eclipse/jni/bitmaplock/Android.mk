LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
    BitmapLock.c \

LOCAL_C_INCLUDES += $(LOCAL_PATH)


LOCAL_MODULE:= bitmaplock
LOCAL_LDLIBS := -llog -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
