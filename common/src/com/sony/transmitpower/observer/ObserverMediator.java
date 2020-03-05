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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sony.transmitpower.Transmitter;
import com.sony.transmitpower.feature.IFeature;
import com.sony.transmitpower.util.OemPowerConsts;
import com.sony.transmitpower.util.TransmitPowerConsts;
import com.sony.transmitpower.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Central node in the star topology of the observers. Used as entry point
 * for all observers - both from the executable container (Service) to control
 * the life of observers ({@see init} and {@see clean}) AND from observers
 * to communicate their states to the outside world, i.e. send a message via
 * a local Intent to the modem about the observed state of the world within
 * their respective domain.
 *
 * This design is chosen both for soft architectural reasons (readability and
 * separation of concerns) as well as for a specific future need - addition of
 * new observers and modularization.
 * Namely, the features should be separated in individual libraries per two axes:
 * <ul>
 *   <li> SoC implementation of power transmition to the modem
 *   <li> existence of particular observers
 * </ul>
 */
public final class ObserverMediator {
    private static final String TAG = ObserverMediator.class.getCanonicalName();

    private final TelephonyStateObserver mTelephonyStateObserver =
            new TelephonyStateObserver();
    private final TelecommObserver mTelecommObserver = new TelecommObserver();
    private final ScreenObserver mScreenObserver = new ScreenObserver();
    private final List<PowerObserverBase> mPowerObservers = new ArrayList<>();
    private final List<IFeature> mFeatures = new ArrayList<>();
    private Transmitter transmitter = null;

    private final BroadcastReceiver mTransmitPowerStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Util.logw(TAG,
                        "TransmitPowerStateReceiver NULL intent");
                return;
            }

            final String action = intent.getAction();
            if (!PowerObserverBase.ACTION_TRANSMIT_POWER_CHANGED.equals(action)) {
                Util.logw(TAG,
                          "TransmitPowerStateReceiver unknown action:" + action);
                return;
            }

            final int key = intent.getIntExtra(PowerObserverBase.TRANSMIT_POWER_KEY,
                          OemPowerConsts.INVALID_KEY);
            final int value = intent.getIntExtra(PowerObserverBase.TRANSMIT_POWER_VALUE,
                          OemPowerConsts.INVALID_VALUE);
            transmitter.transmitPower(key, value);
        }
    };

    private static class SingletonHolder {
        static final ObserverMediator INSTANCE = new ObserverMediator();
    }

    public static ObserverMediator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Does nothing but instantiate object.
     * Call {@see init} to start observers.
     */
    private ObserverMediator() {
        // Intentionally empty.
    }

    /**
     * Inititialize observers from context.
     * All observers initialization routines will be invoked here and
     * effectively started.
     *
     * @param context The Context owning the state (should be Service).
     */
    public void init(@NonNull final Context context)
            throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("null context supplied");
        }

        transmitter = new Transmitter(context);

        // Register broadcast intent for transmit power
        final IntentFilter intentFilter =
                new IntentFilter(PowerObserverBase.ACTION_TRANSMIT_POWER_CHANGED);
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(mTransmitPowerStateReceiver, intentFilter);

        // initialize global state observers
        mTelephonyStateObserver.init(context);
        mTelecommObserver.init(context);
        mScreenObserver.init(context);

        // create power modifying observers
        PowerObserverBase batteryObserver = new BatteryObserver(context,
                                        OemPowerConsts.BATTERY_EXT,
                                        OemPowerConsts.BATTERY_EXT,
                                        OemPowerConsts.VALUE_OFF,
                                        OemPowerConsts.INVALID_VALUE);
        mPowerObservers.add(batteryObserver);
        mTelephonyStateObserver.addListener((BatteryObserver) batteryObserver);

        PowerObserverBase wifiObserver = new WifiObserver(context,
                                        OemPowerConsts.WIFI_EXT,
                                        OemPowerConsts.WIFI_EXT,
                                        OemPowerConsts.VALUE_OFF,
                                        OemPowerConsts.INVALID_VALUE);
        mPowerObservers.add(wifiObserver);
        mTelephonyStateObserver.addListener((WifiObserver) wifiObserver);

        PowerObserverBase voiceCallObserver = new VoiceCallObserver(context,
                                        OemPowerConsts.VOICECALL_EXT,
                                        OemPowerConsts.VOICECALL_EXT,
                                        OemPowerConsts.VALUE_OFF,
                                        OemPowerConsts.INVALID_VALUE);
        mPowerObservers.add(voiceCallObserver);
        mTelephonyStateObserver.addListener((VoiceCallObserver) voiceCallObserver);
        mTelecommObserver.addListener((VoiceCallObserver) voiceCallObserver);

        // initialize additional features if any
        final Intent intent = new Intent(TransmitPowerConsts.ACTION_FEATURE);
        context.sendBroadcast(intent, TransmitPowerConsts.PERMISSION_FEATURE);
    }

    public void addFeature(@NonNull IFeature feature) throws IllegalArgumentException {
        if (feature == null) {
            throw new IllegalArgumentException("null feature supplied");
        }

        mFeatures.add(feature);
    }

    public void addPowerObserver(@NonNull PowerObserverBase observer)
            throws IllegalArgumentException {
        if (observer == null) {
            throw new IllegalArgumentException("null observer supplied");
        }
        mPowerObservers.add(observer);
    }

    public void addScreenListener(@NonNull ScreenObserver.Listener listener)
            throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("null listener supplied");
        }
        mScreenObserver.addListener(listener);
    }

    public void addTelephonyStateListener(@NonNull TelephonyStateObserver.Listener listener)
            throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("null listener supplied");
        }
        mTelephonyStateObserver.addListener(listener);
    }

    public void addTelecommListener(@NonNull TelecommObserver.Listener listener)
            throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("null listener supplied");
        }
        mTelecommObserver.addListener(listener);
    }

    /**
     * Clean all the observers using Service context, e.g. unregister
     * receivers and listeners.
     *
     * @param context Context used to initialize observers (should be Service).
     */
    public void clean(@NonNull final Context context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("null context supplied.");
        }

        LocalBroadcastManager.getInstance(context)
            .unregisterReceiver(mTransmitPowerStateReceiver);

        // clean observers
        int n = mPowerObservers.size();
        for (int i = 0; i < n; i++) {
            mPowerObservers.get(i).clean();
        }
        mPowerObservers.clear();
        mScreenObserver.clean(context);
        mTelephonyStateObserver.clean();
        mTelecommObserver.clean(context);

        // clean features
        n = mFeatures.size();
        for (int i = 0; i < n; i++) {
            mFeatures.get(i).clean(context);
        }
    }
}
