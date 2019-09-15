# Licensed under the LICENSE.
# Copyright 2017, Sony Mobile Communications Inc.
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := TransPowerCommonSensor
LOCAL_MODULE_TAGS := optional

ifneq ($(shell echo "$(PLATFORM_SDK_VERSION)" ),$(shell echo "$(PRODUCT_SHIPPING_API_LEVEL)" ))
    LOCAL_PRIVATE_PLATFORM_APIS := true
endif

LOCAL_PROPRIETARY_MODULE := true

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := \
    libpower \
    TransPowerCommon

include $(BUILD_STATIC_JAVA_LIBRARY)
