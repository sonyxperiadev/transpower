/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.sensor;

import android.annotation.NonNull;
import android.content.Context;

import com.sony.transmitpower.feature.IFeature;
import com.sony.transmitpower.observer.ObserverMediator;
import com.sony.transmitpower.observer.PowerObserverBase;
import com.sony.transmitpower.sensor.observer.ProximityObserver;
import com.sony.transmitpower.util.OemPowerConsts;
//import com.sony.transmitpower.util.Util;

/**
 * TODO
 */
public final class SensorFeature implements IFeature {
    //private static final String TAG = SensorFeature.class.getCanonicalName();

    private final Proximity mProximity = new Proximity();

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
        mProximity.init(context);

        // initialize ProximityObserver
        PowerObserverBase proximityObserver = new ProximityObserver(context,
                OemPowerConsts.PSENSOR_EXT,
                OemPowerConsts.PSENSOR_EXT,
                OemPowerConsts.VALUE_OFF,
                OemPowerConsts.INVALID_VALUE);
        ((ProximityObserver) proximityObserver).init(mProximity);

        mProximity.addListener((ProximityObserver) proximityObserver);

        ObserverMediator.getInstance().addPowerObserver(proximityObserver);
        ObserverMediator.getInstance()
            .addScreenListener((ProximityObserver) proximityObserver);
        ObserverMediator.getInstance()
            .addTelephonyStateListener((ProximityObserver) proximityObserver);
        ObserverMediator.getInstance()
            .addTelecommListener((ProximityObserver) proximityObserver);

        // finally persist feature with ObserverMediator
        ObserverMediator.getInstance().addFeature(this);
    }

    @Override
    public void clean(@NonNull Context context) {
        // observers should have already been cleaned up
        // by ObserverMediator and this is basically
        // a notification of that so just clean the sensors
        mProximity.clean();
    }
}

