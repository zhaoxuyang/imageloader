LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
    libnsgif.c \
    GifFactory.c \

LOCAL_C_INCLUDES += $(LOCAL_PATH)


LOCAL_MODULE:= nsgif
LOCAL_LDLIBS := -llog -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
