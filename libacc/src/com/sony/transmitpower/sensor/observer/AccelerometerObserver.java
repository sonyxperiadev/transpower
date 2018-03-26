/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.sensor.observer;

import android.content.Context;

import com.sony.transmitpower.sensor.Accelerometer;
import com.sony.transmitpower.util.Util;

/**
 * Observer which reports changes in device states influenced
 * by accelerometer.
 */
public class AccelerometerObserver
        extends SensorObserver
        implements Accelerometer.Listener {

    private static final String TAG = AccelerometerObserver.class.getCanonicalName();

    public AccelerometerObserver(Context context,
                             int key,
                             int valueOn,
                             int valueOff,
                             int initValue) {
        super(context, key, valueOn, valueOff, initValue);
    }

    @Override
    public void onMotionStateChanged(boolean isStable) {
        if (Util.DEBUG) {
            Util.logd(TAG, "transmiting ON:" + !isStable);
        }

        transmitPower(isStable ? mValueOff : mValueOn);
    }
}
