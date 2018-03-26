/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.service;

import android.annotation.NonNull;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;

import com.sony.transmitpower.observer.TelecommObserver;
import com.sony.transmitpower.util.Util;

/**
 * A service bound by phone app (well...). Invokes callbacks to handle
 * new calls and changes in current call's audio state, e.g. audio
 * routing change such as switch between builtin earpiece and speaker
 * (in our case dispatch message to subscribed listeners). The point
 * is to report a state only during a phone call taken through the
 * earpiece. If audio is routed through speaker, headset or bluetooth,
 * it's more than likely that the device is not close to user's head.
 *
 * <rant>
 * This functionality should have been possible by using
 * MediaRouter callbacks, but MediaRouter only handles playing
 * music and videos and represents audio routing through
 * earpiece and speaker during phone calls as Phone.
 * </rant>
 */
public final class InCallObserverService extends InCallService {
    private static final String TAG = InCallObserverService.class.getCanonicalName();

    private final Call.Callback mCallback = new Call.Callback() {
            @Override
            public void onStateChanged(Call call, int state) {
                // don't care about call, just the state
                Intent intent = createStateChangedIntent(mContext,
                                                         state == Call.STATE_ACTIVE);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        };
    private final Context mContext;

    public static final String ACTION_INCALL_STATE_CHANGED =
            "com.sony.transmitpower.intent.action.INCALL_STATE_CHANGED";
    public static final String EXTRA_IS_CALL_ACTIVE =
            "com.sony.transmitpower.intent.extra.IS_CALL_ACTIVE";
    public static final String ACTION_INCALL_AUDIO_STATE_CHANGED =
            "com.sony.transmitpower.intent.action.INCALL_AUDIO_STATE_CHANGED";
    public static final String EXTRA_IS_SPEAKER =
            "com.sony.transmitpower.intent.extra.IS_SPEAKER";

    public InCallObserverService() {
        mContext = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        final Intent sendIntent = createStateChangedIntent(mContext, false);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(sendIntent);

        return super.onUnbind(intent);
    }

    @Override
    public void onCallAudioStateChanged(CallAudioState state) {
        super.onCallAudioStateChanged(state);

        // check if builtin speaker or not
        final int audioRoute = state.getRoute();
        final boolean builtinSpeaker = audioRoute == CallAudioState.ROUTE_EARPIECE
                || audioRoute == CallAudioState.ROUTE_WIRED_OR_EARPIECE;

        final Intent intent = createAudioStateChangedIntent(mContext, builtinSpeaker);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        call.registerCallback(mCallback);

        if (Util.DEBUG) {
            Util.logd(TAG, "Call added");
        }
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        call.unregisterCallback(mCallback);

        if (Util.DEBUG) {
            Util.logd(TAG, "Call removed");
        }
    }

    private Intent createStateChangedIntent(@NonNull final Context context,
                                            final boolean isCallActive)
            throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("Null context supplied");
        }

        return new Intent(ACTION_INCALL_STATE_CHANGED)
            .putExtra(EXTRA_IS_CALL_ACTIVE, isCallActive)
            .setClass(context, TelecommObserver.class);
    }

    private Intent createAudioStateChangedIntent(@NonNull final Context context,
                                                 final boolean isBuiltinSpeaker)
            throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("Null context supplied");
        }

        return new Intent(ACTION_INCALL_AUDIO_STATE_CHANGED)
            .putExtra(EXTRA_IS_SPEAKER, isBuiltinSpeaker)
            .setClass(context, TelecommObserver.class);
    }
}
