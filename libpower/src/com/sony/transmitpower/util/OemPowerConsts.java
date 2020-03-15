/*
 * Licensed under the LICENSE.
 * Copyright 2018, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.util;

/**
 * Platform specific constants used in transmit power
 * command protocol.
 *   NOTE: OEMHOOK API is deprecated in O in favor of
 *         vendor extensions.
 */
public final class OemPowerConsts {
    // values intrinsic to the transmit power OEM hook
    public static final int INVALID_KEY   = 0x00000000;
    public static final int INVALID_VALUE = 0xffffffff;
    public static final int VALUE_OFF     = 0x00000000;
    public static final int BATTERY_EXT   = 0x00000001;
    public static final int VOICECALL_EXT = 0x00000002;
    public static final int PSENSOR_EXT   = 0x00000004;
    public static final int WIFI_EXT      = 0x00000008;
    public static final int ACCSENSOR_EXT = 0x00000010;

    // OEMHOOK protocol constants
    public static final int OEMHOOK_BASE = 0x80000;
    public static final int OEMHOOK_EVT_HOOK_SET_TRANSMIT_POWER = OEMHOOK_BASE + 201;
    public static final String OEM_IDENTIFIER = "QOEMHOOK";
    public static final int INT_SIZE = 4;
    public static final int HEADER_SIZE = OEM_IDENTIFIER.length() + 2 * INT_SIZE;
}
