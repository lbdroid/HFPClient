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

import tk.rabidbeaver.hfpclient.HFPNotificationService;
import wrapper.android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import wrapper.android.bluetooth.BluetoothUuid;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.TextView;
import android.widget.Toast;

import tk.rabidbeaver.hfpclient.R;
import java.util.ArrayList;

public class MainActivity extends MonkeyActivity {

    private final String TAG = "MainActivity";

    public static final String PREF_DEVICE = "device";
    public static final String PREF_SERVICES = "services";

    public static final String PICKER_ACTION = "android.bluetooth.devicepicker.action.LAUNCH";
    public static final String PICKER_SELECTED = "android.bluetooth.devicepicker.action.DEVICE_SELECTED";

    private BluetoothA2dpSink ma2dpSink = null;

    BluetoothDevice mDevice;

    ProfileService mProfileService;

    private boolean mIsBound = false;

    private boolean mDiscoveryInProgress = false;

    private ServicesFragment mServicesFragment = null;

    private final BroadcastReceiver mPickerReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.v(TAG, "mPickerReceiver got " + action);

            if (PICKER_SELECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                updateDevice(device);
                unregisterReceiver(this);

                mServicesFragment.removeService(null);
                mServicesFragment.persistServices();
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.v(TAG, "mReceiver got " + action);

            if (BluetoothDevice.ACTION_UUID.equals(action)) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!dev.equals(mDevice)) {
                    return;
                }

                Parcelable uuids[] = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

                if (uuids != null) {
                    for (Parcelable uuid : uuids) {
                         Log.v(TAG, "Received UUID: " + uuid.toString());
                       /* if (BluetoothUuid.PBAP_PSE.equals(uuid)) {
                            mServicesFragment.addService(ServicesFragment.Service.Type.PBAP, null);
                        } else */if (BluetoothUuid.Handsfree_AG.equals(uuid)) {
                            mServicesFragment.addService(ServicesFragment.Service.Type.HFP);
                        } else if (BluetoothUuid.AvrcpTarget.equals(uuid) ||
                                    BluetoothUuid.AudioSource.equals(uuid) ||
                                    BluetoothUuid.AudioSink.equals(uuid)) {
                            Log.v(TAG, "Adding AVRCP");
                            mServicesFragment.addService(ServicesFragment.Service.Type.AVRCP);
                        }
                    }
                }

                mServicesFragment.persistServices();

                /*if (mDiscoveryInProgress) {
                    Log.v(TAG, "Searching MAS instances");
                    //mDevice.sdpSearch(BluetoothUuid.MAS);
                    wrapper.android.bluetooth.BluetoothDevice.sdpSearch(mDevice, BluetoothUuid.MAS);
                }*/

            } else if (action.equals(wrapper.android.bluetooth.BluetoothDevice.ACTION_SDP_RECORD)){
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!dev.equals(mDevice)) {
                    return;
                }

                ParcelUuid uuid = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
                Log.v(TAG, "Received UUID: " + uuid.toString());
                Log.v(TAG, "expected UUID: " +
                        BluetoothUuid.MAS.toString());
                Log.v(TAG, "mDiscoveryInProgress: " + mDiscoveryInProgress);
                /*if(uuid.equals(BluetoothUuid.MAS)){
                    SdpMasRecord masrec = intent.getParcelableExtra(wrapper.android.bluetooth.BluetoothDevice.EXTRA_SDP_RECORD);
                    Log.v(TAG, "masrec: " + masrec);

                    if (masrec != null) {
                        mProfileService.setMasInstances(masrec);
                        mServicesFragment.addService(ServicesFragment.Service.Type.MAP, masrec);
                    }

                    mServicesFragment.persistServices();

                    mDiscoveryInProgress = false;
                }*/
            }
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mProfileService = ((ProfileService.LocalBinder) service).getService();
            mProfileService.setDevice(mDevice);
            mServicesFragment.restoreServices();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mProfileService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "onCreate");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_main);

        mServicesFragment = (ServicesFragment) getFragmentManager().findFragmentById(
                R.id.services_list);

        Intent intent = new Intent(this, ProfileService.class);
        startService(intent);

        if (bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
            mIsBound = true;
        }
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(getApplicationContext(),
                                            ma2dpSinkServiceListener, wrapper.android.bluetooth.BluetoothProfile.A2DP_SINK);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.v(TAG, "onStart");

        String addr = getSharedPreferences("bluetoothDevices", MODE_PRIVATE).getString(PREF_DEVICE, null);

        try {
            BluetoothDevice dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr);
            updateDevice(dev);
        } catch (IllegalArgumentException e) {
            // just leave device unset
        }

        Intent service = new Intent(getApplicationContext(), HFPNotificationService.class);
        service.setAction("load");
        getApplicationContext().startService(service);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v(TAG, "onResume");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_UUID);
        filter.addAction(wrapper.android.bluetooth.BluetoothDevice.ACTION_SDP_RECORD);
        registerReceiver(mReceiver, filter);

        if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
            Button b = (Button) findViewById(R.id.discover_services);
            b.setEnabled(false);

            b = (Button) findViewById(R.id.select_device);
            b.setEnabled(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v(TAG, "onPause");

        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.v(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mIsBound) {
            unbindService(mConnection);
        }

        Log.v(TAG, "onDestroy");
    }

    public void onButtonClick(View v) {
        if (v.getId() == R.id.select_device) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(PICKER_SELECTED);
            registerReceiver(mPickerReceiver, filter);

            Intent intent = new Intent(PICKER_ACTION);
            intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);

        } else if (v.getId() == R.id.discover_services) {
            if (mDevice == null || mDiscoveryInProgress) {
                return;
            }

            mServicesFragment.removeService(null);

            Log.v(TAG, "fetching UUIDs");
            mDiscoveryInProgress = mDevice.fetchUuidsWithSdp();
        }
    }

    private BluetoothProfile.ServiceListener ma2dpSinkServiceListener =
                              new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothA2dpSink.A2DP_SINK) {
                Log.v(TAG, "onServiceDisconnected ");
                ma2dpSink = null;
            }

        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == wrapper.android.bluetooth.BluetoothProfile.A2DP_SINK) {
                Log.v(TAG, "onServiceConnected ");
                ma2dpSink = new BluetoothA2dpSink(proxy);
            }

        }
    };

    public void onToggleClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        Log.v(TAG, "onToggleClicked is_on: " + on);
    }

    private void updateDevice(BluetoothDevice device) {
        SharedPreferences.Editor prefs = getSharedPreferences("bluetoothDevices", MODE_PRIVATE).edit();

        TextView name = (TextView) findViewById(R.id.device_name);
        TextView addr = (TextView) findViewById(R.id.device_address);

        if (device == null) {
            name.setText(R.string.blank);
            addr.setText(R.string.blank);

            prefs.remove(PREF_DEVICE);
        } else {
            Intent intent = new Intent(BluetoothConnectionReceiver.ACTION_NEW_BLUETOOTH_DEVICE);
            intent.putExtra(BluetoothConnectionReceiver.EXTRA_DEVICE_ADDRESS, device.getAddress());
            sendBroadcast(intent);

            name.setText(device.getName());
            addr.setText(device.getAddress());

            prefs.putString(PREF_DEVICE, device.getAddress());

            Intent service = new Intent(getApplicationContext(), HFPNotificationService.class);
            service.setAction("load");
            getApplicationContext().startService(service);
        }

        prefs.commit();

        mDevice = device;

        if (mProfileService != null) {
            mProfileService.setDevice(mDevice);
        }
    }
}
