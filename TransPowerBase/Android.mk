# Licensed under the LICENSE.
# Copyright 2017, Sony Mobile Communications Inc.
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := TransPowerBase
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PROPRIETARY_MODULE := true
LOCAL_MODULE_TAGS := optional

LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_SRC_FILES := $(call all-java-files-under, ../common/src)
LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/../common/res

LOCAL_JAVA_LIBRARIES += telephony-common
LOCAL_STATIC_ANDROID_LIBRARIES += \
    android-support-v4 \
    androidx.annotation_annotation
LOCAL_STATIC_JAVA_LIBRARIES += \
    libpower

# proguard:
# use on debug
#LOCAL_PROGUARD_ENABLED := disabled
LOCAL_PROGUARD_FLAG_FILES := ../common/proguard.flags

include $(BUILD_PACKAGE)
