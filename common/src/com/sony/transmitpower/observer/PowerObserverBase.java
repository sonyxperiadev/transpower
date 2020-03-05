/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.observer;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sony.transmitpower.util.Util;

/**
 * Base class for all observers required to report their state to the
 * modem. Contains definitions of observer's name (key) and
 * interpretations of states - power excitation levels i.e. on/off
 * states, as well as messaging routine, i.e. intent broadcast to the
 * mediator.
 */
public class PowerObserverBase {
    private static final String TAG = PowerObserverBase.class.getCanonicalName();
    private int mCurrentValue;

    protected final Context mContext;
    protected final int mKey;
    protected final int mValueOn;
    protected final int mValueOff;

    public static final String ACTION_TRANSMIT_POWER_CHANGED =
            "com.sony.intent.action.TRANSMIT_POWER_CHANGED";
    public static final String TRANSMIT_POWER_KEY = "transmit_power_key";
    public static final String TRANSMIT_POWER_VALUE = "transmit_power_value";


    public PowerObserverBase(Context context,
                             int key,
                             int valueOn,
                             int valueOff,
                             int initValue) {
        mContext = context;
        mKey = key;
        mValueOn = valueOn;
        mValueOff = valueOff;
        mCurrentValue = initValue;
    }

    public void clean() {
        // Nothing to clean
    }

    public void update(int value) {
        mCurrentValue = value;
    }


    protected void transmitPower(int value) {
        if (mContext == null) {
            Util.logw(TAG, "null context.");
            return;
        }

        if (value == mCurrentValue) {
            if (Util.DEBUG) {
                Util.logd(TAG, "key (" + mKey + ") value not changed from " + value);
            }
            return;
        }

        if (value != mValueOn && value != mValueOff) {
            Util.logw(TAG, "Invalid value (" + value + ") transmit attempt.");
            return;
        }

        if (Util.DEBUG) {
            Util.logd(TAG, "key (" + mKey + ") -> value:" + value);
        }

        mCurrentValue = value;
        final Intent intent = new Intent(ACTION_TRANSMIT_POWER_CHANGED);
        intent.putExtra(TRANSMIT_POWER_KEY, mKey);
        intent.putExtra(TRANSMIT_POWER_VALUE, value);
        intent.setClass(mContext, ObserverMediator.class);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
