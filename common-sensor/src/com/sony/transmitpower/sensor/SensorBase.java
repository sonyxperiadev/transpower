/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.sensor;

import androidx.annotation.NonNull;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * SensorBase is a base class for sensors. It holds the sensor itself,
 * its type, the manager with which to handle it and whether or not there
 * is a listener listening to it. It implements a common initialization
 * and cleanup routines. Necessary event handlers should be implemented
 * by derived classes.
 */
public class SensorBase implements SensorEventListener {
    //private static final String TAG = SensorBase.class.getCanonicalName();

    private final int mSensorType;
    private SensorManager mSensorManager;
    private boolean mIsObserved = false;

    protected Sensor mSensor;

    public SensorBase(int sensorType) {
        mSensorType = sensorType;
    }

    public void init(@NonNull final Context context)
            throws IllegalArgumentException,
                   IllegalStateException {
        if (context == null) {
            throw new IllegalArgumentException("null Context supplied.");
        }

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager == null) {
            throw new IllegalStateException("SensorManager is null.");
        }

        mSensor = mSensorManager.getDefaultSensor(mSensorType);
        if (mSensor == null) {
            throw new IllegalStateException("Sensor is null.");
        }
    }

    public void clean() {
        mSensorManager.unregisterListener(this);
        mIsObserved = false;
    }

    public void listen(boolean turnOn) {
        if (turnOn && !mIsObserved) {
            mIsObserved = mSensorManager.registerListener(this,
                                                          mSensor,
                                                          SensorManager.SENSOR_DELAY_UI);
        } else if (!turnOn && mIsObserved) {
            mSensorManager.unregisterListener(this);
            mIsObserved = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Intentionally empty;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Intentionally empty;
    }
}
