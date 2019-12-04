/*
 * Licensed under the LICENSE.
 * Copyright 2018, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower;

import android.util.Log;

import android.telephony.TelephonyManager;

import com.sony.transmitpower.util.OemPowerConsts;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Transmitter {
    private static final boolean DEBUG = false;
    private static final String TAG = Transmitter.class.getCanonicalName();

    public static void transmitPower(final int key, final int value) {
        if (DEBUG) Log.d(TAG, "setting key: " + key + ", value: " + value);

        if (!validate(key, value)) {
            return;
        }

        final TelephonyManager telephonyManager = TelephonyManager.getDefault();
        if (telephonyManager == null)
            throw new IllegalStateException("No default telephonyManager");

        ByteBuffer buf = ByteBuffer.allocate(OemPowerConsts.HEADER_SIZE
                                             + OemPowerConsts.INT_SIZE
                                             + OemPowerConsts.INT_SIZE);
        buf.order(ByteOrder.nativeOrder());

        try {
            buf.put(OemPowerConsts.OEM_IDENTIFIER
                    .getBytes(OemPowerConsts.ENCODING_USASCII));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Encoding not supported: " + e);
            return;
        }

        // Add Request ID
        buf.putInt(OemPowerConsts.OEMHOOK_EVT_HOOK_SET_TRANSMIT_POWER);
        // Add Request payload
        buf.putInt(OemPowerConsts.INT_SIZE);
        buf.putInt(key);
        buf.putInt(value);

        // tm calls this through on the ITelephony service
        byte[] resp = new byte[1024];
        int ret = telephonyManager.invokeOemRilRequestRaw(buf.array(), resp);
        if (ret < 0)
            throw new IllegalArgumentException("invokeOemRilRequestRaw failed with rc = " + ret);
    }

    private static boolean validate(final int key, final int value) {
        switch(key) {
            case OemPowerConsts.BATTERY_EXT:
            case OemPowerConsts.VOICECALL_EXT:
            case OemPowerConsts.PSENSOR_EXT:
            case OemPowerConsts.WIFI_EXT:
            case OemPowerConsts.ACCSENSOR_EXT:
                break;
            case OemPowerConsts.INVALID_KEY:
            default:
                Log.w(TAG, "Invalid key: " + key);
                return false;
        }

        switch(value) {
            case OemPowerConsts.BATTERY_EXT:
            case OemPowerConsts.VOICECALL_EXT:
            case OemPowerConsts.PSENSOR_EXT:
            case OemPowerConsts.WIFI_EXT:
            case OemPowerConsts.ACCSENSOR_EXT:
            case OemPowerConsts.VALUE_OFF:
                break;
            case OemPowerConsts.INVALID_VALUE:
            default:
                Log.w(TAG, "Invalid value: " + value);
                return false;
        }

        return true;
    }

    private Transmitter() {
        // intentionally empty
    }
}
