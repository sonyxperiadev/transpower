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

import com.sony.transmitpower.sensor.util.Vector;
import com.sony.transmitpower.util.Util;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that implements acquisition of accelerometer data
 * from SensorManager using a SensorEventListener. We use
 * a dead simple comparison of data with a value from
 * resources since for this purpose the simple movement
 * detection is good enough and behaved better in power
 * management tests over calculating square error in
 * either Cartesian or polar coordinates or over
 * "dead reckoning" through Runge-Kutta integration.
 */
public final class Accelerometer
        extends SensorBase
        implements SensorEventListener {
    private static final String TAG = Accelerometer.class.getCanonicalName();

    private static final int SAMPLING_COUNT = 5;
    private static final int AXIS_X = SensorManager.AXIS_X - 1;
    private static final int AXIS_Y = SensorManager.AXIS_Y - 1;
    private static final int AXIS_Z = SensorManager.AXIS_Z - 1;

    private float mMotionThreshold;
    private int mSampleCount = 0;
    private Vector mRunningSum = new Vector(0.0f, 0.0f, 0.0f);

    private final Set<Listener> mListeners = new HashSet();

    public interface Listener {
        void onMotionStateChanged(boolean isStable);
    }

    public Accelerometer() {
        super(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    public void addListener(@NonNull final Listener listener)
            throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("null Listener supplied.");
        }

        mListeners.add(listener);
    }

    @Override
    public void init(@NonNull final Context context)
            throws IllegalArgumentException,
                   IllegalStateException {
        super.init(context);

        mMotionThreshold = context.getResources()
            .getFloat(R.dimen.config_motion_threshold);
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

        if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) return;

        // stat:
        mRunningSum = mRunningSum.plus(new Vector(Math.abs(event.values[AXIS_X]),
                                                  Math.abs(event.values[AXIS_Y]),
                                                  Math.abs(event.values[AXIS_Z])));

        if (Util.DEBUG) {
            Util.logd(TAG, "Running stat: " + mRunningSum);
            Util.logd(TAG, "iteration: " + mSampleCount);
        }

        if (mSampleCount <= SAMPLING_COUNT) {
            mSampleCount++;
            return;
        }

        final Vector avg = mRunningSum.times((float) 1 / mSampleCount);
        if (Util.DEBUG) {
            Util.logd(TAG, "Average: " + avg);
        }

        boolean isDetected = avg.x > mMotionThreshold
                || avg.y > mMotionThreshold
                || avg.z > mMotionThreshold;

        if (Util.DEBUG && isDetected) {
            Util.logd(TAG, "motion detected");
        }

        for (Listener listener: mListeners) {
            listener.onMotionStateChanged(!isDetected);
        }

        mRunningSum.x = mRunningSum.y = mRunningSum.z = 0.0f;
        mSampleCount = 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // NOP; We don't care about accuracy
    }
}
