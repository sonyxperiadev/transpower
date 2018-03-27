/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.feature;

import android.annotation.NonNull;
import android.content.Context;

/**
 * IFeature is a simple plugin interface. The idea is
 * to instantiate a feature object uniformly, e.g. upon
 * receiving an intent, initialize it and perform a
 * cleanup. In this incarnation, it looks like it can
 * perform any action whatsoever, but in fact most
 * features will only implement initialization of sensor
 * observers in their init methods and the cleanup of
 * said observers in the clean method.
 */
public interface IFeature {
    void init(@NonNull Context context);
    void clean(@NonNull Context context);
}
