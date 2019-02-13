# Licensed under the LICENSE.
# Copyright 2017, Sony Mobile Communications Inc.
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := libacc
LOCAL_MODULE_TAGS := optional
LOCAL_PROPRIETARY_MODULE := true

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_STATIC_JAVA_LIBRARIES += TransPowerCommonSensor

include $(BUILD_STATIC_JAVA_LIBRARY)
