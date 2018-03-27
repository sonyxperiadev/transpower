/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sony.transmitpower.service.TransmitPowerService;
import com.sony.transmitpower.util.Util;

/**
 * BootCompletedReceiver starts the service upon receiving
 * a BOOT_COMPLETED intent (and in later versions a
 * LOCKED_BOOT_COMPLETED intent).
 * Why was this design chosen?
 * For example, a JobScheduler may be used to schedule
 * jobs based on network but they need to be setup somewhere, and
 * in case of a service that should run always when any network is
 * started that means that the job would have to be scheduled on
 * boot completed - which is sort of what we do here, so the
 * question is when and how we should instantiate all the objects
 * needed and when the whole thing needs to die.
 * Initialization:
 * In case of service, it is on its creation when basically all
 * initialization happens and for most time the service is then
 * idle and shoots small messages around on some callbacks. So,
 * heaviest task once a boot.
 * In case of JobScheduler, initialization happens any time a
 * network (or another trigger) is available and then it continues
 * mostly idle until the job is killed. So heaviest task multiple
 * times during the life time of device and also potentially when
 * it's needed to react quickly.
 * Life:
 * In case of sticky service, it never dies or rather it's a zombie.
 * If it gets killed it will resurrect immediately. This comes in
 * handy if you have to react on every call, movement, data exchange
 * etc. with a downside that that it races for resources every time
 * it's killed (most probably when resources are scarce).
 * In case of a job, it can die whenever the system decides, even
 * if it's inconvenient e.g. in need of telling modem to power down.
 *
 * What then with resources taken by Service? It's a compromise
 * between resource taken always with less radiation always and
 * resources taken and released most of the time with less radiation
 * some times.
 */
public final class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompletedReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Util.logw(TAG, "BOOT_COMPLETE NULL intent");
            return;
        }

        final String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)) {
            Util.logw(TAG , "Boot completed receiver unknown action: " + action);
            return;
        }

        if (Util.DEBUG) {
            Util.logd(TAG, "BOOT_COMPLETE: " + intent.getAction());
        }

        intent.setClass(context, TransmitPowerService.class);
        context.startService(intent);
    }
}

