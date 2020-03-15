/*
 * Licensed under the LICENSE.
 * Copyright 2018, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sony.opentelephony.hookmediator.IHooks;
import com.sony.transmitpower.util.OemPowerConsts;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.charset.StandardCharsets;

public final class Transmitter {
    private static final boolean DEBUG = false;
    private static final String TAG = Transmitter.class.getCanonicalName();
    private CompletableFuture<IHooks> mHooks = new CompletableFuture<IHooks>();

    public void transmitPower(final int key, final int value) {
        if (DEBUG) Log.d(TAG, "setting key: " + key + ", value: " + value);

        if (!validate(key, value)) {
            return;
        }

        if (mHooks == null) {
            throw new RuntimeException("IHooks service disappeared!");
        }

        IHooks hooks = null;
        try {
            hooks = mHooks.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            hooks.setTransmitPower(key, value);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        // TODO: Decide whether to use the below codepath
        // or have hook-specific functionality in HookMediator

        // byte[] request = new byte[OemPowerConsts.HEADER_SIZE +
        // OemPowerConsts.INT_SIZE
        // + OemPowerConsts.INT_SIZE];
        // ByteBuffer buf = ByteBuffer.wrap(request);
        // buf.order(ByteOrder.nativeOrder());

        // buf.put(OemPowerConsts.OEM_IDENTIFIER.getBytes(StandardCharsets.US_ASCII));

        // // Add Request ID
        // buf.putInt(OemPowerConsts.OEMHOOK_EVT_HOOK_SET_TRANSMIT_POWER);
        // // Add Request payload
        // buf.putInt(OemPowerConsts.INT_SIZE);
        // buf.putInt(key);
        // buf.putInt(value);

        // hooks.sendCommand(request);
    }

    private static boolean validate(final int key, final int value) {
        switch (key) {
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

        switch (value) {
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

    public Transmitter(Context context) {
        final Intent intent = new Intent();
        final String pkg = IHooks.class.getPackage().getName();
        intent.setClassName(pkg, pkg + ".HookMediatorService");
        ServiceConnection connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                IHooks hooks = IHooks.Stub.asInterface(service);
                if (mHooks == null) {
                    mHooks = CompletableFuture.completedFuture(hooks);
                } else {
                    mHooks.complete(hooks);
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                // TODO: Try to reconnect?
                mHooks = null;
            }
        };
        if (!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            throw new RuntimeException("Failed to bind IHooks service!");
        }
    }
}
