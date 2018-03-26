/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.UserHandle;

import com.sony.transmitpower.observer.ObserverMediator;
import com.sony.transmitpower.util.Util;

/**
 * Controls the life of device state observers responsible to
 * report to modem about imminent changes in power levels. It
 * does that by initializing the mediator on service creation
 * and cleaning it up on service's (managed) destruction.
 */
public final class TransmitPowerService extends Service {
    private static final String TAG = TransmitPowerService.class.getCanonicalName();
    private final ObserverMediator mObserverMediator = ObserverMediator.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();

        // run for owner only
        if (Process.myUserHandle() != UserHandle.SYSTEM) {
            return;
        }

        mObserverMediator.init(this);
        if (Util.DEBUG) Util.logd(TAG, "created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Util.DEBUG) Util.logd(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mObserverMediator.clean(this);
        if (Util.DEBUG) Util.logd(TAG, "destroyed");

        super.onDestroy();
    }
}

