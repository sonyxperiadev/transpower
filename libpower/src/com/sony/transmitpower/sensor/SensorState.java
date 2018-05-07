/*
 * Licensed under the LICENSE.
 * Copyright 2018, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.sensor;

/**
 * Data class containing interpretation of the world
 * for sensors depending on the activity of device.
 * The interpretation is based on the table provided
 * by Qualcomm.
 *
 * Can only be instantiated through a factory method
 * {@see getSensorEvent} on a need to have basis.
 */
public final class SensorState {

    public static final boolean POWER_ON = true;
    public static final boolean POWER_OFF = false;
    public static final int NOT_DETECTED = -1;
    public static final int DETECTING = 0;
    public static final int DETECTED = 1;

    private final boolean mPower;
    private final int mState;

    /**
     * Factory of SensorState objects. Invoke when you need
     * the interpretation of device activities.
     *
     * @param isCallActive true if there is currently an ongoing
     *                     voice call, false otherwise.
     * @param isDataActive true if there is currently a data
     *                     transfer over data line, false otherwise.
     * @param isScreenOn   true if the screen is on, false otherwise.
     * @return An instance of SensorState containing requested interpretation
     *         of device states.
     */
    public static SensorState getSensorState(boolean isCallActive,
                                             boolean isDataActive,
                                             boolean isScreenOn) {
        if (isCallActive) {
            return new SensorState(POWER_ON, DETECTING);
        }

        if (isDataActive) {
            if (isScreenOn) {
                return new SensorState(POWER_ON, DETECTING);
            }

            return new SensorState(POWER_OFF, DETECTED);
        }

        return new SensorState(POWER_OFF, NOT_DETECTED);
    }

    public boolean isPowerOn() {
        return mPower;
    }

    public int getState() {
        return mState;
    }

    private SensorState(boolean isPowerOn, int sensorState) {
        mPower = isPowerOn;
        mState = sensorState;
    }
}
