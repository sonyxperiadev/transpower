/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.observer;

import androidx.annotation.NonNull;
import android.content.Context;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.sony.transmitpower.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Observes state of telephony service, i.e. subscription (including
 * data) and dispatches information further to subscribed listeners.
 * Also, provides static methods to poll for current states.
 */
public final class TelephonyStateObserver {
    private static final String TAG = TelephonyStateObserver.class.getCanonicalName();

    private TelephonyManager mTelephonyManager;
    private SubscriptionManager mSubscriptionManager;
    private final Set<Listener> mListeners = new HashSet();
    private final List<PhoneStateListenerImpl> mPhoneStateListeners =
            new ArrayList<>();

    public interface Listener {
        void onServiceStateChanged(int state);
        void onDataStateChanged();
    }

    public TelephonyStateObserver() {
        // Intentionally empty.
    }

    public void init(@NonNull final Context context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("null context supplied.");
        }

        mTelephonyManager = TelephonyManager.from(context);
        mSubscriptionManager = SubscriptionManager.from(context);
        mSubscriptionManager.addOnSubscriptionsChangedListener(mSubscriptionListener);
    }

    public void clean() {
        mListeners.clear();
        mSubscriptionManager.removeOnSubscriptionsChangedListener(mSubscriptionListener);
        for (PhoneStateListenerImpl listener : mPhoneStateListeners) {
            listener.listen(PhoneStateListener.LISTEN_NONE);
        }
        mPhoneStateListeners.clear();
    }

    public void addListener(@NonNull final Listener listener)
            throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("null Listener");
        }

        mListeners.add(listener);
    }

    public static boolean isCallActive(@NonNull final Context context)
            throws IllegalArgumentException, IllegalStateException {
        if (context == null) {
            throw new IllegalArgumentException("null context supplied.");
        }

        final AudioManager am = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            throw new IllegalStateException("AudioManager is null");
        }

        return am.getMode() == AudioManager.MODE_IN_CALL;
    }

    public static boolean isDataActive(@NonNull final Context context)
            throws IllegalArgumentException, IllegalStateException {
        if (context == null) {
            throw new IllegalArgumentException("null context supplied");
        }

        final TelephonyManager tm = TelephonyManager.from(context);
        if (tm == null) {
            throw new IllegalStateException("TelephonyManager is null");
        }

        int dataActivity = tm.getDataActivity();
        return TelephonyManager.DATA_ACTIVITY_IN == dataActivity
            || TelephonyManager.DATA_ACTIVITY_OUT == dataActivity
            || TelephonyManager.DATA_ACTIVITY_INOUT == dataActivity;
    }

    // for each SIM:
    private final class PhoneStateListenerImpl extends PhoneStateListener {
        // Keep track of the relevant manager (bound to subId) for deregistration purposes.
        private final TelephonyManager mTelephonyManager;

        PhoneStateListenerImpl(TelephonyManager manager) {
            super();
            mTelephonyManager = manager;
        }

        void listen(int events) {
            mTelephonyManager.listen(this, events);
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            final int state = serviceState.getState();
            for (Listener listener : mListeners) {
                listener.onServiceStateChanged(state);
            }

            if (Util.DEBUG) {
                Util.logd(TAG, "onServiceStateChanged(): "
                          + "subId = " + mSubId
                          + ", state = " + state);
            }
        }

        @Override
        public void onDataActivity(int direction) {
            // we don't care about direction, any will do
            for (Listener listener : mListeners) {
                listener.onDataStateChanged();
            }

            if (Util.DEBUG) {
                Util.logd(TAG, "onDataActivity(): "
                          + "subId = " + mSubId
                          + ", direction = " + direction);
            }
        }
    };

    // Listen the status of SIM and register listeners to TelephonyManager
    private final SubscriptionManager.OnSubscriptionsChangedListener
            mSubscriptionListener = new SubscriptionManager
                    .OnSubscriptionsChangedListener() {
                @Override
                public void onSubscriptionsChanged() {
                    // Reset listeners because onSubscriptionsChanged()
                    // is called multiple times.
                    for (PhoneStateListenerImpl listener : mPhoneStateListeners) {
                        listener.listen(PhoneStateListener.LISTEN_NONE);
                    }
                    mPhoneStateListeners.clear();

                    // Create listeners for each SIM
                    final List<SubscriptionInfo> subInfos = mSubscriptionManager
                            .getActiveSubscriptionInfoList();
                    if (subInfos == null || subInfos.isEmpty()) {
                        // Create a listener for the default telephony manager:
                        mPhoneStateListeners.add(new PhoneStateListenerImpl(mTelephonyManager));

                        if (Util.DEBUG) {
                            Util.logd(TAG,
                                    "onSubscriptionsChanged() :"
                                    + "SIM is not inserted or unavailable");
                        }
                    } else {
                        for (SubscriptionInfo info : subInfos) {
                            final int subId = info.getSubscriptionId();
                            final TelephonyManager managerForSubId
                                    = mTelephonyManager.createForSubscriptionId(subId);
                            mPhoneStateListeners.add(new PhoneStateListenerImpl(managerForSubId));

                            if (Util.DEBUG) {
                                Util.logd(TAG, "onSubscriptionsChanged() : subId = "
                                          + subId);
                            }
                        }
                    }

                    // Register listeners to TelephonyManager
                    for (PhoneStateListenerImpl listener : mPhoneStateListeners) {
                        listener.listen(PhoneStateListener.LISTEN_SERVICE_STATE
                                        | PhoneStateListener.LISTEN_DATA_ACTIVITY);
                    }
                }
            };
}
