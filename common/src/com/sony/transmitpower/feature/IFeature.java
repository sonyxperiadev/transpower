/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.feature;

import android.annotation.NonNull;
import android.content.Context;

/**
 * TODO
 */
public interface IFeature {
    void init(@NonNull Context context);
    void clean(@NonNull Context context);
}
