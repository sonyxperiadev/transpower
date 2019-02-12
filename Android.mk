# Licensed under the LICENSE.
# Copyright 2017, Sony Mobile Communications Inc.
ifeq ($(PRODUCT_PLATFORM_SOD),true)

LOCAL_PATH:= $(call my-dir)
include $(call all-makefiles-under,$(LOCAL_PATH))

endif
