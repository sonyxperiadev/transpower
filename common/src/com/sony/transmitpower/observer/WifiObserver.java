/*
 * Licensed under the LICENSE.
 * Copyright 2017, Sony Mobile Communications Inc.
 */
package com.sony.transmitpower.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.telephony.ServiceState;

import com.sony.transmitpower.util.OemPowerConsts;
import com.sony.transmitpower.util.Util;

public final class WifiObserver
        extends PowerObserverBase
        implements TelephonyStateObserver.Listener {
    private static final String TAG = WifiObserver.class.getCanonicalName();

    private final WifiManager mWifiManager;
    private final BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
            @Override
            public synchronized void onReceive(Context context, Intent intent) {
                if (context == null || intent == null) return;

                final String action = intent.getAction();
                int wifiApP2pValue = mValueOff;

                if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                    wifiApP2pValue = getWifiApP2pValueByState(getApState());
                    if (wifiApP2pValue == OemPowerConsts.INVALID_VALUE) {
                        return;
                    }
                } else if (WifiP2pManager
                           .WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                    wifiApP2pValue = getWifiApP2pValueByDiscovery(getApState(), intent);
                } else if (WifiP2pManager
                           .WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                    wifiApP2pValue = getWifiApP2pValueByConnection(getApState(), intent);
                } else {
                    Util.logw(TAG, "Unknown action: " + action);
                    return;
                }

                transmitPower(wifiApP2pValue);
            }
        };

    public WifiObserver(Context context,
                        int key,
                        int valueOn,
                        int valueOff,
                        int initValue) {
        super(context, key, valueOn, valueOff, initValue);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        final IntentFilter intentFilter = new IntentFilter(WifiManager
                                                     .WIFI_AP_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mContext.registerReceiver(mWifiStateReceiver, intentFilter);
    }

    @Override
    public void clean() {
        mContext.unregisterReceiver(mWifiStateReceiver);
        super.clean();
    }

    @Override
    public synchronized void update(int value) {
        super.update(value);

        int wifiApP2pValue = getWifiApP2pValueByState(getApState());
        if (wifiApP2pValue == mValueOn) {
            transmitPower(mValueOn);
            return;
        }

        final Intent p2pDiscoveryIntent = Util.getLatestStickyIntentOrNull(mContext,
                new IntentFilter(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION));
        wifiApP2pValue = getWifiApP2pValueByDiscovery(getApState(), p2pDiscoveryIntent);
        if (wifiApP2pValue == mValueOn) {
            transmitPower(mValueOn);
            return;
        }

        final Intent p2pConnectionIntent = Util.getLatestStickyIntentOrNull(mContext,
                new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION));
        wifiApP2pValue = getWifiApP2pValueByConnection(getApState(), p2pConnectionIntent);
        if (wifiApP2pValue == mValueOn) {
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

    private int getWifiApP2pValueByState(final int apState) {
        // Judge if AP is enable or not
        if (apState == WifiManager.WIFI_AP_STATE_ENABLED) {
            return mValueOn;
        }

        if (apState == WifiManager.WIFI_AP_STATE_DISABLED) {
            return mValueOff;
        }

        return OemPowerConsts.INVALID_VALUE;
    }

    private int getWifiApP2pValueByDiscovery(final int apState,
                                             final Intent p2pDiscoveryIntent) {
        if (p2pDiscoveryIntent == null) {
            return getWifiApP2pValueByState(apState);
        }

        final int p2pDiscoveryState = p2pDiscoveryIntent
                .getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE,
                             WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);
        if (p2pDiscoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
            return mValueOn;
        }

        return mValueOff;
    }

    private int getWifiApP2pValueByConnection(final int apState,
                                  final Intent p2pConnectionIntent) {
        if (p2pConnectionIntent == null) {
            return getWifiApP2pValueByState(apState);
        }

        final NetworkInfo info = (NetworkInfo) p2pConnectionIntent
                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (info == null || !info.isConnected()) {
            return mValueOff;
        }

        return mValueOn;
    }

    private int getApState() {
        return mWifiManager.getWifiApState();
    }
}

