/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sony.transmitpower.util.TransmitPowerConsts;
import com.sony.transmitpower.util.Util;

/**
 * FeatureIntentReceiver instantiates and initializes a {@see
 * SensorFeature} plugable component upon receiving a {@see
 * TransmitPowerConsts.ACTION_FEATURE} intent.
 */
public final class FeatureIntentReceiver extends BroadcastReceiver {
    private static final String TAG = FeatureIntentReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Util.logw(TAG, "null intent");
            return;
        }

        final String action = intent.getAction();
        if (Util.DEBUG) {
            Util.logd(TAG, "--- FEATURE SENSOR received intent " + action);
        }
        if (TransmitPowerConsts.ACTION_FEATURE.equals(action)) {
            SensorFeature sf = new SensorFeature();
            sf.init(context);
        } else {
            Util.logw(TAG, "Received unknown intent:" + action);
            return;
        }
    }
}

