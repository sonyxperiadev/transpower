/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.observer;

import android.content.Context;
import android.telephony.ServiceState;

import com.sony.transmitpower.util.OemPowerConsts;
import com.sony.transmitpower.util.Util;

/**
 * Observes states of a voice call and reports them to the
 * modem. ({@see TelecommObserver}, {@see TelephonyStateObserver}
 */
public final class VoiceCallObserver
        extends PowerObserverBase
        implements TelephonyStateObserver.Listener,
            TelecommObserver.Listener {
    private static final String TAG = VoiceCallObserver.class.getCanonicalName();

    public VoiceCallObserver(Context context,
                             int key,
                             int valueOn,
                             int valueOff,
                             int initValue) {
        super(context, key, valueOn, valueOff, initValue);
    }

    @Override
    public void clean() {
        super.clean();
    }

    @Override
    public synchronized void update(int value) {
        super.update(value);
    }

    @Override
    public synchronized void onCallAudioStateChanged(boolean isBuiltinSpeaker) {
        transmitPower(isBuiltinSpeaker ? mValueOn : mValueOff);
    }

    @Override
    public synchronized void onCallStateChanged(boolean isCallActive,
                                                boolean isBuiltinSpeaker) {
        if (isCallActive && isBuiltinSpeaker) {
            transmitPower(mValueOn);
            return;
        }

        transmitPower(mValueOff);
    }

    @Override
    public void onServiceStateChanged(int state) {
        if (state == ServiceState.STATE_IN_SERVICE
                || state == ServiceState.STATE_EMERGENCY_ONLY) {
            if (Util.DEBUG) {
                Util.logd(TAG, "Telephony is turned on:"
                          + " initialiazing observers to current values");
            }

            update(OemPowerConsts.INVALID_VALUE);
        }
    }

    @Override
    public void onDataStateChanged() {
        // TODO
    }
}
