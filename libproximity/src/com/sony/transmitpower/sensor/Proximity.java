/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.sensor;

import android.annotation.NonNull;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.google.android.collect.Sets;

import com.sony.transmitpower.util.Util;

import java.util.Set;

/**
 * Class that implements acquisition of proximity data
 * from the proximity sensor (Sensor.TYPE_PROXIMITY)
 * via SensorManager using a SensorEventListener.
 */
public final class Proximity
        extends SensorBase
        implements SensorEventListener {
    private static final String TAG = Proximity.class.getCanonicalName();

    private final Set<Listener> mListeners = Sets.newHashSet();

    public interface Listener {
        void onProximityStateChanged(boolean isNear);
    }

    public Proximity() {
        super(Sensor.TYPE_PROXIMITY);
    }

    public void addListener(@NonNull final Listener listener)
            throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("null Listener supplied.");
        }

        mListeners.add(listener);
    }

    @Override
    public void clean() {
        super.clean();
        mListeners.clear();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Util.DEBUG) {
            Util.logd(TAG, "onSensorChanged");
        }

        if (event.sensor.getType() != Sensor.TYPE_PROXIMITY) return;

        final float sensorRange = event.values[0];
        final boolean isNear = sensorRange < mSensor.getMaximumRange();
        for (Listener listener: mListeners) {
            listener.onProximityStateChanged(isNear);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // NOP; We don't care about accuracy
    }
}
