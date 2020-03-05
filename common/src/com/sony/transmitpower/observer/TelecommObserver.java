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
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.collect.Sets;

import com.sony.transmitpower.service.InCallObserverService;
import com.sony.transmitpower.util.Util;

import java.util.Set;

/**
 * Observes state of a call. Receives an explicit local intent
 * from an InCallService about a state of call (active/inactive
 * or audio routing changes) and dispatches further to subscribed
 * listeners.
 */
public final class TelecommObserver extends BroadcastReceiver {
    private static final String TAG = TelecommObserver.class.getCanonicalName();

    private final Set<Listener> mListeners = Sets.newHashSet();
    private boolean mIsCallActive = false;
    private boolean mIsBuiltinSpeaker = false;

    public TelecommObserver() {
        // Intentionally empty.
    }

    public void init(@NonNull final Context context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("null context supplied.");
        }

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(InCallObserverService.ACTION_INCALL_STATE_CHANGED);
        intentFilter.addAction(InCallObserverService.ACTION_INCALL_AUDIO_STATE_CHANGED);
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(this, intentFilter);
    }

    public void clean(@NonNull final Context context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("null context supplied");
        }

        LocalBroadcastManager.getInstance(context)
            .unregisterReceiver(this);
        mListeners.clear();
    }

    public interface Listener {
        void onCallAudioStateChanged(boolean isBuiltinSpeaker);
        void onCallStateChanged(boolean isCallActive, boolean isBuiltinSpeaker);
    }

    public void addListener(@NonNull final Listener listener)
            throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("null Listener");
        }

        mListeners.add(listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            if (Util.DEBUG) {
                Util.logd(TAG, "Context:" + context + ", intent:" + intent);
            }
            return;
        }

        final String action = intent.getAction();
        if (InCallObserverService.ACTION_INCALL_STATE_CHANGED.equals(action)) {
            mIsCallActive = intent.getBooleanExtra(InCallObserverService
                                                   .EXTRA_IS_CALL_ACTIVE,
                                                   false);
            if (Util.DEBUG) {
                Util.logd(TAG, "CALL_STATE_CHANGED:"
                          + "\ncall active: " + mIsCallActive
                          + "\nspeaker: " + mIsBuiltinSpeaker);
            }

            for (Listener listener : mListeners) {
                listener.onCallStateChanged(mIsCallActive, mIsBuiltinSpeaker);
            }
        } else if (InCallObserverService.ACTION_INCALL_AUDIO_STATE_CHANGED
                       .equals(action)) {
            mIsBuiltinSpeaker = intent.getBooleanExtra(InCallObserverService
                                                       .EXTRA_IS_SPEAKER,
                                                       false);
            if (Util.DEBUG) {
                Util.logd(TAG, "AUDIO_STATE_CHANGED:"
                          + "\ncall active: " + mIsCallActive
                          + "\nspeaker: " + mIsBuiltinSpeaker);
            }

            for (Listener listener : mListeners) {
                listener.onCallAudioStateChanged(mIsBuiltinSpeaker);
            }
        } else {
            Util.logw(TAG, "Unknown intent: " + action);
        }
    }
}
