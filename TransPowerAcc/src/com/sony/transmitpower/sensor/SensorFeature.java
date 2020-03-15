/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.sensor;

import androidx.annotation.NonNull;
import android.content.Context;

import com.sony.transmitpower.feature.IFeature;
import com.sony.transmitpower.observer.ObserverMediator;
import com.sony.transmitpower.observer.PowerObserverBase;
import com.sony.transmitpower.sensor.observer.AccelerometerObserver;
import com.sony.transmitpower.util.OemPowerConsts;
//import com.sony.transmitpower.util.Util;

/**
 * SensorFeature implements the feature plugin. Initializes an accelerometer
 * and adds it to the pool of observers. (Cleans up after itself.)
 */
public final class SensorFeature implements IFeature {
    //private static final String TAG = SensorFeature.class.getCanonicalName();

    private final Accelerometer mAccelerometer = new Accelerometer();

    /**
     * Does nothing but instantiate object.
     */
    public SensorFeature() {
        // Intentionally empty.
    }

    @Override
    public void init(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("null context supplied");
        }

        // initialize sensors
        mAccelerometer.init(context);

        // initialize AccelerometerObserver
        PowerObserverBase accObserver = new AccelerometerObserver(context,
                OemPowerConsts.ACCSENSOR_EXT,
                OemPowerConsts.ACCSENSOR_EXT,
                OemPowerConsts.VALUE_OFF,
                OemPowerConsts.INVALID_VALUE);
        ((AccelerometerObserver) accObserver).init(mAccelerometer);

        mAccelerometer.addListener((AccelerometerObserver) accObserver);

        ObserverMediator.getInstance().addPowerObserver(accObserver);
        ObserverMediator.getInstance()
            .addScreenListener((AccelerometerObserver) accObserver);
        ObserverMediator.getInstance()
            .addTelephonyStateListener((AccelerometerObserver) accObserver);
        ObserverMediator.getInstance()
            .addTelecommListener((AccelerometerObserver) accObserver);

        // finally persist feature with ObserverMediator
        ObserverMediator.getInstance().addFeature(this);
    }

    @Override
    public void clean(@NonNull Context context) {
        // observers should have already been cleaned up
        // by ObserverMediator and this is basically
        // a notification of that so just clean the sensors
        mAccelerometer.clean();
    }
}
