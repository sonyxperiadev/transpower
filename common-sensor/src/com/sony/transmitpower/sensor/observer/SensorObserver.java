/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.sensor.observer;

import android.annotation.NonNull;
import android.content.Context;
import android.telephony.ServiceState;

import com.sony.transmitpower.observer.PowerObserverBase;
import com.sony.transmitpower.observer.ScreenObserver;
import com.sony.transmitpower.observer.TelecommObserver;
import com.sony.transmitpower.observer.TelephonyStateObserver;
import com.sony.transmitpower.sensor.SensorBase;
import com.sony.transmitpower.sensor.SensorState;
import com.sony.transmitpower.util.Util;

/**
 * Base class for observers to report changes in device states
 * influenced by sensors, e.g. a proximity sensor or accelerometer etc.
 * and control registration life of a listener.
 * Subclass a specific observer from this class to add sensor specific
 * handling.
 * The idea is:
 *    - have one sensor to control registration of listener
 *    - passively listen to potential other sensors and act upon that
 * e.g. AccelerometerObserver extending SensorObserver can control
 *      an Accelerometer but also listen to Proximity and unregister
 *      Accelerometer if needed.
 */
public class SensorObserver
        extends PowerObserverBase
        implements ScreenObserver.Listener,
                   TelephonyStateObserver.Listener,
                   TelecommObserver.Listener {
    private static final String TAG = SensorObserver.class.getCanonicalName();

    protected SensorBase mSensor = null;

    public SensorObserver(Context context,
                          int key,
                          int valueOn,
                          int valueOff,
                          int initValue) {
        super(context, key, valueOn, valueOff, initValue);
    }

    public void init(@NonNull final SensorBase sensor)
            throws IllegalArgumentException {
        if (sensor == null) {
            throw new IllegalArgumentException("null sensor supplied");
        }

        mSensor = sensor;
    }

    @Override
    public void clean() {
        super.clean();
    }

    @Override
    public synchronized void update(int value) {
        super.update(value);

        transmitPower(value);
    }

    @Override
    public void onScreenStateChanged(boolean isScreenOn) {
        updateSensorState(TelephonyStateObserver.isCallActive(mContext),
                          TelephonyStateObserver.isDataActive(mContext),
                          isScreenOn);
    }

    @Override
    public void onServiceStateChanged(int state) {
        if (state == ServiceState.STATE_IN_SERVICE
                || state == ServiceState.STATE_EMERGENCY_ONLY) {
            if (Util.DEBUG) {
                Util.logd(TAG, "Telephony is turned on:"
                          + " initializing observers to current values");
            }

            updateSensorState(TelephonyStateObserver.isCallActive(mContext),
                          TelephonyStateObserver.isDataActive(mContext),
                          ScreenObserver.isScreenOn(mContext));
        }
    }

    @Override
    public void onCallAudioStateChanged(boolean isBuiltinSpeaker) {
        // NOP; not interested in this
    }

    @Override
    public void onCallStateChanged(boolean isCallActive, boolean isBuiltinSpeaker) {
        // don't care if the speaker is builtin or not
        updateSensorState(isCallActive,
                          TelephonyStateObserver.isDataActive(mContext),
                          ScreenObserver.isScreenOn(mContext));
    }

    @Override
    public void onDataStateChanged() {
        updateSensorState(TelephonyStateObserver.isCallActive(mContext),
                          TelephonyStateObserver.isDataActive(mContext),
                          ScreenObserver.isScreenOn(mContext));
    }

    private void updateSensorState(boolean isCallActive,
                                   boolean isDataActive,
                                   boolean isScreenOn) {

        if (Util.DEBUG) {
            Util.logd(TAG, "Updating sensor state:"
                      + "\ncall active = " + isCallActive
                      + "\ndata active = " + isDataActive
                      + "\n  screen on = " + isScreenOn);
        }

        final SensorState se = SensorState
                .getSensorState(isCallActive,
                                isDataActive,
                                isScreenOn);

        if (mSensor != null) {
            mSensor.listen(se.isPowerOn());
        }

        if (se.getState() == SensorState.DETECTING) {
            return;
        }

        update(se.getState() == SensorState.DETECTED ? mValueOn : mValueOff);
    }
}
