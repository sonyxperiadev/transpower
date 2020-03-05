/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.observer;

import androidx.annotation.NonNull;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.sony.transmitpower.util.Util;

import java.util.ArrayList;
import java.util.List;


public final class ScreenObserver {
    private static final String TAG = ScreenObserver.class.getCanonicalName();

    private final List<Listener> mListeners = new ArrayList<>();
    private final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if (context == null || intent == null) {
                    Util.logw(TAG, "ScreenStateReceiver null intent");
                    return;
                }

                final String action = intent.getAction();
                if (!Intent.ACTION_SCREEN_ON.equals(action)
                        && !Intent.ACTION_SCREEN_OFF.equals(action)) {
                    Util.logw(TAG, "ScreenStateReceiver unknown action: " + action);
                    return;
                }

                final int n = mListeners.size();
                for (int i = 0; i < n; i++) {
                    final Listener listener = mListeners.get(i);
                    listener.onScreenStateChanged(ScreenObserver.isScreenOn(context));
                }
            }
        };

    public interface Listener {
        void onScreenStateChanged(boolean isScreenOn);
    }

    public ScreenObserver() {
        // Intentionally empty.
    }

    public void init(@NonNull final Context context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("null context suplied.");
        }

        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(mScreenStateReceiver, intentFilter);
    }

    public void clean(@NonNull final Context context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("null context supplied.");
        }

        context.unregisterReceiver(mScreenStateReceiver);
        mListeners.clear();
    }

    public void addListener(@NonNull final Listener listener)
            throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("null Listener supplied.");
        }

        mListeners.add(listener);
    }

    public static boolean isScreenOn(@NonNull final Context context)
            throws IllegalArgumentException, IllegalStateException {
        if (context == null) {
            throw new IllegalArgumentException("null context supplied.");
        }

        final PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        if (pm == null) {
            throw new IllegalStateException("PowerManager is null");
        }

        return pm.isInteractive();
    }
}
