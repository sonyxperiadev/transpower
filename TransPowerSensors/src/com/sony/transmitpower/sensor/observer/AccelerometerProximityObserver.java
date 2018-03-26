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
 * by accelerometer, i.e. movement of device indicating that
 * it might be close to the user. Also, stop listening to
 * accelerometer if proximity sensor has indicated device is
 * NEAR an object.
 */
public final class AccelerometerProximityObserver
        extends AccelerometerObserver
        implements Proximity.Listener {

    private static final String TAG = AccelerometerProximityObserver.class
            .getCanonicalName();

    public AccelerometerProximityObserver(Context context,
                             int key,
                             int valueOn,
                             int valueOff,
                             int initValue) {
        super(context, key, valueOn, valueOff, initValue);
    }

    @Override
    public void onProximityStateChanged(boolean isNear) {
        if (Util.DEBUG) {
            Util.logd(TAG, "proximity NEAR:" + isNear);
        }

        if (isNear) {
            mSensor.listen(false);
        }
    }
}
