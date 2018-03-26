/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.telephony.ServiceState;

import com.sony.transmitpower.util.OemPowerConsts;
import com.sony.transmitpower.util.Util;

public final class BatteryObserver
        extends PowerObserverBase
        implements TelephonyStateObserver.Listener {
    private static final String TAG = BatteryObserver.class.getCanonicalName();

    private static final int VOLTAGE_THRESHOLDS = 3600;

    private final BroadcastReceiver mBatteryStateReceiver = new BroadcastReceiver() {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Util.logw(TAG, "BatterStateReceiver null intent");
                return;
            }

            final String action = intent.getAction();
            if (!Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                Util.logw(TAG, "BatteryStateReceiver unknown action: " + action);
                    return;
            }

            transmitPowerByIntent(intent);
        }
    };

    public BatteryObserver(Context context,
                           int key,
                           int valueOn,
                           int valueOff,
                           int initValue) {
        super(context, key, valueOn, valueOff, initValue);
        mContext.registerReceiver(mBatteryStateReceiver,
                                  new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    @Override
    public void clean() {
        mContext.unregisterReceiver(mBatteryStateReceiver);
        super.clean();
    }

    @Override
    public synchronized void update(int value) {
        super.update(value);

        final Intent intent =  Util.getLatestStickyIntentOrNull(mContext,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        transmitPowerByIntent(intent);
    }

    @Override
    public void onServiceStateChanged(int state) {
        if (state == ServiceState.STATE_IN_SERVICE
                || state == ServiceState.STATE_EMERGENCY_ONLY) {
            if (Util.DEBUG) {
                Util.logd(TAG, "Telephony is turned on:"
                          + " initialiazing observers to current values");
            }

            update(OemPowerConsts.INVALID_VALUE);
        }
    }

    @Override
    public void onDataStateChanged() {
        // TODO
    }

    private void transmitPowerByIntent(final Intent intent) {
        if (intent == null) {
            transmitPower(mValueOff);
            return;
        }

        final int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        if (voltage < VOLTAGE_THRESHOLDS) {
            transmitPower(mValueOff);
        }

        transmitPower(mValueOn);
    }
}
