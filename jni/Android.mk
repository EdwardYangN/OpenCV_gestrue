LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)


LOCAL_MODULE    := OpenCV_Gestrue
LOCAL_SRC_FILES := OpenCV_Gestrue.cpp

include $(BUILD_SHARED_LIBRARY)
