/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.sensor.observer;

import android.content.Context;

import com.sony.transmitpower.sensor.Proximity;
import com.sony.transmitpower.util.Util;

/**
 * Observer which reports changes in device states influenced
 * by proximity sensor, i.e. state of device indicating that
 * it might be close to the user.
 */
public final class ProximityObserver
        extends SensorObserver
        implements Proximity.Listener {

    private static final String TAG = ProximityObserver.class.getCanonicalName();

    public ProximityObserver(Context context,
                             int key,
                             int valueOn,
                             int valueOff,
                             int initValue) {
        super(context, key, valueOn, valueOff, initValue);
    }

    @Override
    public void onProximityStateChanged(boolean isNear) {
        if (Util.DEBUG) {
            Util.logd(TAG, "transmitting ON:" + isNear);
        }

        transmitPower(isNear ? mValueOn : mValueOff);
    }
}
