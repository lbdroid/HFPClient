/*
 * Copyright (c) 2013, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *            notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *            notice, this list of conditions and the following disclaimer in the
 *            documentation and/or other materials provided with the distribution.
 *        * Neither the name of The Linux Foundation nor
 *            the names of its contributors may be used to endorse or promote
 *            products derived from this software without specific prior written
 *            permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT ARE DISCLAIMED.    IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.codeaurora.bluetooth.bttestapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.codeaurora.bluetooth.bttestapp.util.Logger;

import java.util.ArrayList;

public class BluetoothConnectionReceiver extends BroadcastReceiver {

    private final static String TAG = "BluetoothConnectionReceiver";

    public static final String ACTION_NEW_BLUETOOTH_DEVICE = "org.codeaurora.bluetooth.action.NEW_BLUETOOTH_DEVICE";

    public static final String EXTRA_DEVICE_ADDRESS = "org.codeaurora.bluetooth.extra.DEVICE_ADDRESS";

    private static ArrayList<IBluetoothConnectionObserver> observers = new ArrayList<IBluetoothConnectionObserver>();

    private static BluetoothDevice selectedDevice = null;

    public static void registerObserver(IBluetoothConnectionObserver observer) {
        observers.add(observer);

        if (selectedDevice != null) {
            observer.onDeviceChanged(selectedDevice);
        }
    }

    public static void removeObserver(IBluetoothConnectionObserver observer) {
        observers.remove(observer);
    }

    private void notifyObserversDeviceChanged(BluetoothDevice device) {
        for (IBluetoothConnectionObserver observer : observers) {
            observer.onDeviceChanged(device);
        }
    }

    private void notifyObserversDeviceDisconected() {
        for (IBluetoothConnectionObserver observer : observers) {
            observer.onDeviceDisconected();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_NEW_BLUETOOTH_DEVICE.equals(intent.getAction())) {
            Logger.v(TAG, "Receive new bluetooth device.");

            String address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);

            if (address != null) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                selectedDevice = adapter.getRemoteDevice(address);
                notifyObserversDeviceChanged(selectedDevice);
            } else {
                Logger.e(TAG, "Received NULL address!");
            }
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (device.equals(selectedDevice)) {
                Logger.v(TAG, "Received bluetooth disconected.");

                notifyObserversDeviceDisconected();
            }
        } else {
            Logger.w(TAG, "Unknown intent received with action: " + intent.getAction());
        }
    }
}
