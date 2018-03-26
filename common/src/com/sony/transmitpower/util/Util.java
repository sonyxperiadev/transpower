/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.util;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public final class Util {
    public static final boolean DEBUG = false;

    public static void logd(@NonNull final String tag, final String message)
            throws IllegalArgumentException {
        if (tag == null) {
            throw new IllegalArgumentException("null tag");
        }

        Log.d(tag, message);
    }

    public static void logw(@NonNull final String tag, final String message)
            throws IllegalArgumentException {
        if (tag == null) {
            throw new IllegalArgumentException("null tag");
        }

        Log.w(tag, message);
    }

    public static void loge(@NonNull final String tag, final String message)
            throws IllegalArgumentException {
        if (tag == null) {
            throw new IllegalArgumentException("null tag");
        }

        Log.e(tag, message);
    }

    @Nullable
    public static Intent getLatestStickyIntentOrNull(@NonNull final Context context,
                                                     @NonNull final IntentFilter f)
            throws IllegalArgumentException {
        if (context == null || f == null) {
            throw new IllegalArgumentException("Context and IntentFilter can't be null.");
        }

        return context.registerReceiver(null, f);
    }

    private Util() {
        // Intentionally empty
    }
}
