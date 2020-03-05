/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.sensor.util;

import androidx.annotation.NonNull;

public final class Vector {
    public float x;
    public float y;
    public float z;

    public Vector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector plus(@NonNull Vector v) {
        return new Vector(x + v.x, y + v.y, z + v.z);
    }

    public Vector times(float c) {
        return new Vector(c * x, c * y, c * z);
    }

    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
