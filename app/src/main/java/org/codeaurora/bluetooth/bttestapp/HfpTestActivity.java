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

import android.app.ActionBar;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import wrapper.android.bluetooth.BluetoothHeadsetClient;
import wrapper.android.bluetooth.BluetoothHeadsetClientCall;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import wrapper.android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.codeaurora.bluetooth.bttestapp.CallHistoryDialogFragment.CallHistoryDialogListener;
import tk.rabidbeaver.hfpclient.R;
import org.codeaurora.bluetooth.bttestapp.util.Logger;
import org.codeaurora.bluetooth.bttestapp.util.MonkeyEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;

public class HfpTestActivity extends MonkeyActivity implements IBluetoothConnectionObserver,
        CallHistoryDialogListener {

    private final String TAG = "HfpTestActivity";

    private DialpadFragment mDialpadFragment = null;

    private IndicatorsFragment mIndicatorsFragment = null;

    private CallsListFragment mCallsListFragment = null;

    private final ArrayList<String> mCallHistory = new ArrayList<String>();

    private ActionBar mActionBar = null;

    private Hashtable<Integer, BluetoothHeadsetClientCall> mCalls;

   // this should be visible for fragments
    BluetoothHeadsetClient mBluetoothHeadsetClient;
    ProfileService mProfileService = null;
    BluetoothDevice mDevice;

    // this should be done in a more elegant way //
    public boolean mFeatVtag;
    public boolean mFeatVoiceRecognition;
    public boolean mFeatThreeWayCalling;
    public boolean mFeatEnhancedCallControl;

    public boolean mFeatReject;
    public boolean mFeatMerge;
    public boolean mFeatMergeDetach;
    public boolean mFeatReleaseAndAccept;
    public boolean mFeatAcceptHeldOrWaiting;
    public boolean mFeatReleaseHeldOrWaiting;

    private final BroadcastReceiver mHfpClientReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = (BluetoothDevice)
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (action.equals(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)) {

                int prevState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, 0);
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
                onReceiveActionConnectionStateChanged(device, prevState, state, intent.getExtras());
                mCallsListFragment.onConnStateChanged(state, prevState);
                mIndicatorsFragment.onConnStateChanged(state, prevState);

                if (state == BluetoothProfile.STATE_CONNECTED) {
                    Logger.v(TAG, "device " + device + " mDevice " + mDevice);
                    if (!device.equals(mDevice)) {
                        mDevice = device;
                    }
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                                if (mBluetoothHeadsetClient == null || mDevice == null) {
                                    Logger.v(TAG, "Profile is disconnected");
                                    return;
                                }
                                for (BluetoothHeadsetClientCall call :
                                        mBluetoothHeadsetClient.getCurrentCalls(mDevice)) {
                                    Logger.v(TAG, "Updating call controls");
                                    mCallsListFragment.onCallChanged(call);
                                }
                            }
                    }, 500);
                }

                // Send MonkeyEvent
                new MonkeyEvent("hfp-connection-state-changed", true)
                        .addReplyParam("state", state)
                        .addReplyParam("prevState", prevState)
                        .send();
            } else if (action.equals(BluetoothHeadsetClient.ACTION_AG_EVENT)) {

                mIndicatorsFragment.onAgEvent(intent.getExtras());

            } else if (action.equals(BluetoothHeadsetClient.ACTION_CALL_CHANGED)) {

                BluetoothHeadsetClientCall call = new BluetoothHeadsetClientCall((Object) intent.
                        getParcelableExtra(BluetoothHeadsetClient.EXTRA_CALL));
                onCallChanged(call);
                mCallsListFragment.onCallChanged(call);

                // Send MonkeyEvent
                new MonkeyEvent("hfp-call-changed", true)
                        .addExtReply(callToJson(call))
                        .send();
            } else if (action.equals(BluetoothHeadsetClient.ACTION_AUDIO_STATE_CHANGED)) {

                // I don't like putting application specific hacks here, but this is a very specific and very simple
                // broadcast. If the receiver for this broadcast doesn't exist, then it just doesn't do anything.
                Log.d(TAG, "AUDIO_STATE_CHANGED");
                int astate = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
                if (astate == BluetoothHeadsetClient.STATE_AUDIO_CONNECTED){
                    Intent i = new Intent();
                    i.setAction("tk.rabidbeaver.bd37033controller.PHONE_ON");
                    sendBroadcast(i);
                } else if (astate != BluetoothHeadsetClient.STATE_AUDIO_CONNECTING){
                    Intent i = new Intent();
                    i.setAction("tk.rabidbeaver.bd37033controller.PHONE_OFF");
                    sendBroadcast(i);
                }

                int prevState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, 0);
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
                onReceiveAudioStateChange(device, prevState, state);
                mIndicatorsFragment.onAudioStateChanged(state, prevState);
                if (state == BluetoothHeadsetClient.STATE_AUDIO_CONNECTED) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                                if (mBluetoothHeadsetClient == null || mDevice == null) {
                                    Logger.v(TAG, "Profile is disconnected");
                                    return;
                                }
                                for (BluetoothHeadsetClientCall call :
                                        mBluetoothHeadsetClient.getCurrentCalls(mDevice)) {
                                    Logger.v(TAG, "Updating call controls");
                                    mCallsListFragment.onCallChanged(call);
                                }
                            }
                    }, 500);
                }

                // Send MonkeyEvent
                new MonkeyEvent("hfp-audio-state-changed", true)
                        .addReplyParam("state", state)
                        .addReplyParam("prevState", prevState)
                        .send();
            } else if(action.equals(BluetoothHeadsetClient.ACTION_RESULT)) {
                int result = intent.getIntExtra(
                        BluetoothHeadsetClient.EXTRA_RESULT_CODE, -1);
                int cme = intent.getIntExtra(
                        BluetoothHeadsetClient.EXTRA_CME_CODE, -1);

                onReceiveResult(result, cme);

                // Send MonkeyEvent
                new MonkeyEvent("hfp-receive-result", true)
                        .addReplyParam("result", result)
                        .addReplyParam("cme", cme)
                        .send();
            } else if (action.equals(BluetoothHeadsetClient.ACTION_LAST_VTAG)) {

                String number = intent.getStringExtra(BluetoothHeadsetClient.EXTRA_NUMBER);
                Toast.makeText(HfpTestActivity.this, "Phone number received: " + number,
                        Toast.LENGTH_LONG).show();
                // Send MonkeyEvent
                new MonkeyEvent("hfp-receive-phonenumber", true)
                        .addReplyParam("number", number)
                        .send();
            }
        }

        private void onCallChanged(BluetoothHeadsetClientCall call) {
            int state = call.getState();
            String number = call.getNumber().trim();

            new MonkeyEvent("hfp-call-changed", true).addExtReply(callToJson(call)).send();

            if (number.isEmpty()) {
                return;
            }

            // we add any "new" call to list and put in at the beginning
            if (state == BluetoothHeadsetClientCall.CALL_STATE_ALERTING ||
                    state == BluetoothHeadsetClientCall.CALL_STATE_DIALING ||
                    state == BluetoothHeadsetClientCall.CALL_STATE_INCOMING ||
                    state == BluetoothHeadsetClientCall.CALL_STATE_WAITING) {
                mCallHistory.remove(number);
                mCallHistory.add(0, number);

                if (mCallHistory.size() > 20) {
                    mCallHistory.remove(20);
                }
            }
        }

        private void onReceiveResult(int result, int cme) {
            Logger.v(TAG, "onReceiveResult (result: " + result + " cme: " +
                    cme);

            Toast.makeText(HfpTestActivity.this,
                    resultCodeToString(result, cme),
                    Toast.LENGTH_SHORT).show();
        }

        private synchronized void onReceiveAudioStateChange(BluetoothDevice device,
                int prevState, int state) {
            Logger.v(TAG, "onReceiveAudioStateChange (" + prevState +
                    " -> " + state + ")");

            invalidateOptionsMenu();
        }

        private void onReceiveActionConnectionStateChanged(BluetoothDevice device,
                int prevState, int state, Bundle features) {
            Logger.v(TAG, "onReceiveActionConnectionStateChanged: " +
                    device.getAddress() + " (" +
                    String.valueOf(prevState) + " -> " +
                    String.valueOf(state) + ")");

            switch (state) {
                case BluetoothProfile.STATE_CONNECTED:
                    updateAgFeatures(features);
                    Toast.makeText(HfpTestActivity.this, R.string.msg_connection_success,
                            Toast.LENGTH_SHORT).show();

                    new MonkeyEvent("hfp-connected", true).send();

                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    new MonkeyEvent("hfp-disconnected", true).send();

                    Toast.makeText(HfpTestActivity.this, R.string.msg_disconnection_success,
                            Toast.LENGTH_SHORT).show();

                    break;
            }

            invalidateOptionsMenu();
        }
    };

    // HFP Service Connection.
    private final ServiceConnection mHfpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.v(TAG, "onServiceConnected()");
            mProfileService = ((ProfileService.LocalBinder) service).getService();
            mBluetoothHeadsetClient = mProfileService.getHfpClient();

            // TODO: This is "ok". Requires setting the preferred device in the MainActivity, then
            String addr = getSharedPreferences("bluetoothDevices", MODE_PRIVATE).getString(MainActivity.PREF_DEVICE, null);
            try {
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr);
            } catch (IllegalArgumentException e) {}

            if (mBluetoothHeadsetClient != null) {
                int connState = mBluetoothHeadsetClient.getConnectionState(mDevice);

                if (connState == BluetoothProfile.STATE_CONNECTED) {
                    // trigger refresh of calls list
                    for (BluetoothHeadsetClientCall call : mBluetoothHeadsetClient
                            .getCurrentCalls(mDevice)) {
                        mCallsListFragment.onCallChanged(call);
                    }

                    // get supported AG features
                    updateAgFeatures(mBluetoothHeadsetClient.getCurrentAgFeatures(mDevice));
                } else if (connState == BluetoothProfile.STATE_DISCONNECTED){
                    mBluetoothHeadsetClient.connect(mDevice);
                }

                // trigger refresh of indicators
                mIndicatorsFragment.onAudioStateChanged(
                        mBluetoothHeadsetClient.getAudioState(mDevice), 0);
                mIndicatorsFragment.onConnStateChanged(connState, 0);
                mIndicatorsFragment.onAgEvent(mBluetoothHeadsetClient.getCurrentAgEvents(mDevice));
            }

            invalidateOptionsMenu();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.v(TAG, "onServiceDisconnected()");
            mProfileService = null;
            mBluetoothHeadsetClient = null;

            invalidateOptionsMenu();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.v(TAG, "onCreate()");

        ActivityHelper.initialize(this, R.layout.activity_hfp_test);
        BluetoothConnectionReceiver.registerObserver(this);
        mCalls = new Hashtable<Integer, BluetoothHeadsetClientCall>();

        // bind to app service
        Intent intent = new Intent(this, ProfileService.class);
        bindService(intent, mHfpServiceConnection, BIND_AUTO_CREATE);

        prepareActionBar();

        mIndicatorsFragment = (IndicatorsFragment) getFragmentManager().findFragmentById(
                R.id.indicators);
        mCallsListFragment = (CallsListFragment) getFragmentManager().findFragmentById(
                R.id.calls_list);
        mDialpadFragment = (DialpadFragment) getFragmentManager().findFragmentById(R.id.dialpad);

        setVolumeControlStream(AudioManager.STREAM_BLUETOOTH_SCO);
    }

    @Override
    protected void onDestroy() {
        Logger.v(TAG, "onDestroy");

        super.onDestroy();

        unbindService(mHfpServiceConnection);
        BluetoothConnectionReceiver.removeObserver(this);
    }

    @Override
    protected void onResume() {
        Logger.v(TAG, "onResume");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothHeadsetClient.ACTION_AG_EVENT);
        filter.addAction(BluetoothHeadsetClient.ACTION_CALL_CHANGED);
        filter.addAction(BluetoothHeadsetClient.ACTION_AUDIO_STATE_CHANGED);
        filter.addAction(BluetoothHeadsetClient.ACTION_RESULT);
        filter.addAction(BluetoothHeadsetClient.ACTION_LAST_VTAG);
        registerReceiver(mHfpClientReceiver, filter);
        if (mBluetoothHeadsetClient != null &&
                mBluetoothHeadsetClient.getConnectionState(mDevice) !=
                BluetoothProfile.STATE_CONNECTED) {
            Logger.v(TAG, "No device is connected");
            super.onResume();
            return;
        }
        if (mBluetoothHeadsetClient != null) {
            for (BluetoothHeadsetClientCall call : mCalls.values()) {
                call.setState(BluetoothHeadsetClientCall.CALL_STATE_TERMINATED);
                mCallsListFragment.onCallChanged(call);
                new MonkeyEvent("hfp-call-changed", true)
                        .addExtReply(callToJson(call))
                        .send();
            }
            for (BluetoothHeadsetClientCall call :
                    mBluetoothHeadsetClient.getCurrentCalls(mDevice)) {
                Logger.v(TAG, "Updating call controls");
                mCallsListFragment.onCallChanged(call);
                new MonkeyEvent("hfp-call-changed", true)
                        .addExtReply(callToJson(call))
                        .send();
            }
        } else {
            Logger.v(TAG,"mBluetoothHeadsetClient is null");
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Logger.v(TAG, "onPause");

        unregisterReceiver(mHfpClientReceiver);
        if (mBluetoothHeadsetClient != null) {
            mCalls.clear();
            Integer id = 1;
            int connState = mBluetoothHeadsetClient.getConnectionState(mDevice);
            // save all calls status
            if (connState == BluetoothProfile.STATE_CONNECTED &&
                    !mBluetoothHeadsetClient.getCurrentCalls(mDevice).isEmpty()) {
                for (BluetoothHeadsetClientCall call :
                    mBluetoothHeadsetClient.getCurrentCalls(mDevice)) {
                    mCalls.put(id, call);
                    id++;
                }
            }
        } else {
            Logger.v(TAG,"mBluetoothHeadsetClient is null");
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mActionBarMenu = menu;

        getMenuInflater().inflate(R.menu.menu_hfp_test, menu);
        menu.findItem(R.id.menu_request_phone_num).setEnabled(mFeatVtag);

        return true;
    }

    @Override
    public void onDeviceChanged(BluetoothDevice device) {
        Logger.v(TAG, "onDeviceChanged()");

        mDevice = device;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_request_phone_num:
                mBluetoothHeadsetClient.getLastVoiceTagNumber(mDevice);
                return true;

            case R.id.menu_call_history:
                CallHistoryDialogFragment chdf = new CallHistoryDialogFragment();
                chdf.setElements(mCallHistory);
                chdf.show(getFragmentManager(), "call-history");
                return true;
        }

        return false;
    }

    @Override
    public void onDeviceDisconected() {
        Logger.v(TAG, "onDeviceDisconected");

        invalidateOptionsMenu();
    }

    @Override
    public void onCallHistoryDialogPositive(DialogFragment dialog, String number) {
        mDialpadFragment.setNumber(number);
    }

    private void prepareActionBar() {
        Logger.v(TAG, "prepareActionBar()");

        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.title_hfp_test);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
    }

    private String resultCodeToString(int result, int cme) {
        Logger.v(TAG, "resultCodeToString()");

        switch (result) {
            case BluetoothHeadsetClient.ACTION_RESULT_OK:
                return getString(R.string.hfptest_result_ok_text);
            case BluetoothHeadsetClient.ACTION_RESULT_ERROR:
                return getString(R.string.hfptest_result_error_text);
            case BluetoothHeadsetClient.ACTION_RESULT_ERROR_NO_CARRIER:
                return getString(R.string.hfptest_result_no_carrier_text);
            case BluetoothHeadsetClient.ACTION_RESULT_ERROR_BUSY:
                return getString(R.string.hfptest_result_busy_text);
            case BluetoothHeadsetClient.ACTION_RESULT_ERROR_NO_ANSWER:
                return getString(R.string.hfptest_result_no_answer_text);
            case BluetoothHeadsetClient.ACTION_RESULT_ERROR_DELAYED:
                return getString(R.string.hfptest_result_delayed_text);
            case BluetoothHeadsetClient.ACTION_RESULT_ERROR_BLACKLISTED:
                return getString(R.string.hfptest_result_blacklisted_text);
            case BluetoothHeadsetClient.ACTION_RESULT_ERROR_CME:
                return getString(R.string.hfptest_result_cme_text) + " (" + cme + ")";
        }

        return getString(R.string.hfptest_call_state_unknown_text);
    }

    @Override
    protected void onMonkeyQuery(String op, Bundle params) {
        if ("getIndicators".equals(op)) {
            Bundle bundle = mBluetoothHeadsetClient.getCurrentAgEvents(mDevice);

            MonkeyEvent me = new MonkeyEvent("hfp-ag-event", true);

            if (bundle.containsKey(BluetoothHeadsetClient.EXTRA_VOICE_RECOGNITION)) {
                me.addReplyParam("vr", bundle.getInt(BluetoothHeadsetClient.EXTRA_VOICE_RECOGNITION));
            }

            if (bundle.containsKey(BluetoothHeadsetClient.EXTRA_IN_BAND_RING)) {
                me.addReplyParam("in-band", bundle.getInt(BluetoothHeadsetClient.EXTRA_IN_BAND_RING));
            }

            if (bundle.containsKey(BluetoothHeadsetClient.EXTRA_NETWORK_STATUS)) {
                me.addReplyParam("network-status",
                        bundle.getInt(BluetoothHeadsetClient.EXTRA_NETWORK_STATUS));
            }

            if (bundle.containsKey(BluetoothHeadsetClient.EXTRA_NETWORK_ROAMING)) {
                me.addReplyParam("network-roaming",
                        bundle.getInt(BluetoothHeadsetClient.EXTRA_NETWORK_ROAMING));
            }

            if (bundle.containsKey(BluetoothHeadsetClient.EXTRA_NETWORK_SIGNAL_STRENGTH)) {
                me.addReplyParam("signal-strength",
                        bundle.getInt(BluetoothHeadsetClient.EXTRA_NETWORK_SIGNAL_STRENGTH));
            }

            if (bundle.containsKey(BluetoothHeadsetClient.EXTRA_BATTERY_LEVEL)) {
                me.addReplyParam("battery-level",
                        bundle.getInt(BluetoothHeadsetClient.EXTRA_BATTERY_LEVEL));
            }

            if (bundle.containsKey(BluetoothHeadsetClient.EXTRA_OPERATOR_NAME)) {
                me.addReplyParam("operator-name",
                        bundle.getString(BluetoothHeadsetClient.EXTRA_OPERATOR_NAME));
            }

            if (bundle.containsKey(BluetoothHeadsetClient.EXTRA_SUBSCRIBER_INFO)) {
                me.addReplyParam("subscriber-info",
                        bundle.getString(BluetoothHeadsetClient.EXTRA_SUBSCRIBER_INFO));
            }

            me.send();
        }
    }

    static String callToJson(BluetoothHeadsetClientCall call) {
        JSONObject json = new JSONObject();

        try {
            json.put("id", call.getId());
            json.put("number", call.getNumber());
            json.put("state", callStateToString(call.getState()));
            json.put("outgoing", call.isOutgoing());
            json.put("multiparty", call.isMultiParty());
        } catch (JSONException e) {
            // do nothing
        }

        return json.toString();
    }

    static String callStateToString(int state) {
        switch (state) {
            case BluetoothHeadsetClientCall.CALL_STATE_ACTIVE:
                return "active";
            case BluetoothHeadsetClientCall.CALL_STATE_HELD:
                return "held";
            case BluetoothHeadsetClientCall.CALL_STATE_DIALING:
                return "dialing";
            case BluetoothHeadsetClientCall.CALL_STATE_ALERTING:
                return "alerting";
            case BluetoothHeadsetClientCall.CALL_STATE_INCOMING:
                return "incoming";
            case BluetoothHeadsetClientCall.CALL_STATE_WAITING:
                return "waiting";
            case BluetoothHeadsetClientCall.CALL_STATE_HELD_BY_RESPONSE_AND_HOLD:
                return "held_by_rnh";
            case BluetoothHeadsetClientCall.CALL_STATE_TERMINATED:
                return "terminated";
        }

        return "unknown";
    }

    private void updateAgFeatures(Bundle b) {
        // only supported AG features are being sent
        mFeatVtag = b.containsKey(BluetoothHeadsetClient.EXTRA_AG_FEATURE_ATTACH_NUMBER_TO_VT);
        mFeatVoiceRecognition = b.containsKey(BluetoothHeadsetClient.EXTRA_AG_FEATURE_VOICE_RECOGNITION);
        mFeatThreeWayCalling = b.containsKey(BluetoothHeadsetClient.EXTRA_AG_FEATURE_3WAY_CALLING);
        mFeatEnhancedCallControl = b.containsKey(BluetoothHeadsetClient.EXTRA_AG_FEATURE_ECC);
        mFeatReject = b.containsKey(BluetoothHeadsetClient.EXTRA_AG_FEATURE_REJECT_CALL);
        mFeatMerge = b.containsKey(BluetoothHeadsetClient.EXTRA_AG_FEATURE_MERGE);
        mFeatMergeDetach = b.containsKey(BluetoothHeadsetClient.EXTRA_AG_FEATURE_MERGE_AND_DETACH);
        mFeatReleaseAndAccept = b.containsKey(BluetoothHeadsetClient.EXTRA_AG_FEATURE_RELEASE_AND_ACCEPT);
        mFeatAcceptHeldOrWaiting = b.containsKey(BluetoothHeadsetClient.EXTRA_AG_FEATURE_ACCEPT_HELD_OR_WAITING_CALL);
        mFeatReleaseHeldOrWaiting = b.containsKey(BluetoothHeadsetClient.EXTRA_AG_FEATURE_RELEASE_HELD_OR_WAITING_CALL);
    }
}
