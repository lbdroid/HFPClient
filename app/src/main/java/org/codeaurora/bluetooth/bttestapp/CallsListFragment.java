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

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import wrapper.android.bluetooth.BluetoothHeadsetClient;
import wrapper.android.bluetooth.BluetoothHeadsetClientCall;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import tk.rabidbeaver.hfpclient.R;

public class CallsListFragment extends Fragment implements OnClickListener, OnItemClickListener {

    private final static String TAG = "CallsListFragment";

    private HfpTestActivity mActivity;

    private ListView mCallsList;

    private Button mActionAccept;

    private Button mActionHoldAndAccept;

    private Button mActionReleaseAndAccept;

    private Button mActionReject;

    private Button mActionTerminate;

    private Button mActionHold;

    private Button mActionRespondAndHold;

    private Button mActionPrivateMode;

    private Button mActionExplicitTransfer;

    private class CallsAdapter extends BaseAdapter {

        private final SparseArray<BluetoothHeadsetClientCall> mCalls;

        private int mSelectedId = 0;

        CallsAdapter() {
            mCalls = new SparseArray<BluetoothHeadsetClientCall>();
        }

        @Override
        public int getCount() {
            return mCalls.size();
        }

        @Override
        public Object getItem(int position) {
            return mCalls.get(mCalls.keyAt(position));
        }

        @Override
        public long getItemId(int position) {
            return mCalls.keyAt(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            BluetoothHeadsetClientCall call = (BluetoothHeadsetClientCall) getItem(position);

            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.call_view, parent, false);
            }

            if (mSelectedId == call.getId()) {
                view.setBackgroundResource(R.drawable.selected_call_bg);
            } else {
                view.setBackgroundResource(0);
            }

            TextView callId = (TextView) view.findViewById(R.id.call_id);
            callId.setText(Integer.toString(call.getId()));

            ImageView dir = (ImageView) view.findViewById(R.id.call_direction);
            dir.setImageResource(call.isOutgoing() ? R.drawable.ic_call_outgoing_holo_dark
                    : R.drawable.ic_call_incoming_holo_dark);

            ((TextView) view.findViewById(R.id.call_number)).setText(call.getNumber());

            switch (call.getState()) {
                case BluetoothHeadsetClientCall.CALL_STATE_ACTIVE:
                    callId.setBackgroundColor(getColor(R.color.call_active));
                    break;

                case BluetoothHeadsetClientCall.CALL_STATE_HELD:
                case BluetoothHeadsetClientCall.CALL_STATE_HELD_BY_RESPONSE_AND_HOLD:
                    callId.setBackgroundColor(getColor(R.color.call_held));
                    break;

                case BluetoothHeadsetClientCall.CALL_STATE_DIALING:
                case BluetoothHeadsetClientCall.CALL_STATE_ALERTING:
                case BluetoothHeadsetClientCall.CALL_STATE_INCOMING:
                case BluetoothHeadsetClientCall.CALL_STATE_WAITING:
                    callId.setBackgroundColor(getColor(R.color.call_new));
                    break;

                default:
                    callId.setBackgroundColor(getColor(R.color.call_unknown));
                    break;
            }

            setIndicator(view, R.id.call_state_active,
                    call.getState() == BluetoothHeadsetClientCall.CALL_STATE_ACTIVE);
            setIndicator(view, R.id.call_state_held,
                    call.getState() == BluetoothHeadsetClientCall.CALL_STATE_HELD);
            setIndicator(view, R.id.call_state_dialing,
                    call.getState() == BluetoothHeadsetClientCall.CALL_STATE_DIALING);
            setIndicator(view, R.id.call_state_alerting,
                    call.getState() == BluetoothHeadsetClientCall.CALL_STATE_ALERTING);
            setIndicator(view, R.id.call_state_incoming,
                    call.getState() == BluetoothHeadsetClientCall.CALL_STATE_INCOMING);
            setIndicator(view, R.id.call_state_waiting,
                    call.getState() == BluetoothHeadsetClientCall.CALL_STATE_WAITING);
            setIndicator(
                    view,
                    R.id.call_state_held_by_rnh,
                    call.getState() == BluetoothHeadsetClientCall.CALL_STATE_HELD_BY_RESPONSE_AND_HOLD);

            setIndicator(view, R.id.call_multiparty, call.isMultiParty());

            return view;
        }

        public void add(BluetoothHeadsetClientCall call) {
            mCalls.put(call.getId(), call);
            notifyDataSetChanged();
        }

        public void remove(BluetoothHeadsetClientCall call) {
            if (call.getId() == mSelectedId) {
                mSelectedId = 0;
            }
            mCalls.remove(call.getId());
            notifyDataSetChanged();
        }

        public void removeAll() {
            mCalls.clear();
            notifyDataSetChanged();
        }

        public void toggleSelected(int id) {
            if (id == mSelectedId) {
                mSelectedId = 0;
            } else {
                mSelectedId = id;
            }
            notifyDataSetChanged();
        }

        public BluetoothHeadsetClientCall getSelected() {
            return mCalls.get(mSelectedId);
        }

        public boolean hasCallsInState(int... state) {
            for (int idx = 0; idx < mCalls.size(); idx++) {
                BluetoothHeadsetClientCall call = mCalls.valueAt(idx);
                for (int s : state) {
                    if (call.getState() == s) {
                        return true;
                    }
                }
            }

            return false;
        }

        private void setIndicator(View view, int id, boolean state) {
            TextView txt = (TextView) view.findViewById(id);
            txt.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
        }

        private int getColor(int id) {
            return getResources().getColor(id);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calls_list_fragment, null);

        mCallsList = (ListView) view.findViewById(R.id.calls_list);
        mCallsList.setAdapter(new CallsAdapter());
        mCallsList.setOnItemClickListener(this);

        mActionAccept = (Button) view.findViewById(R.id.call_action_accept);
        mActionHoldAndAccept = (Button) view.findViewById(R.id.call_action_hold_and_accept);
        mActionReleaseAndAccept = (Button) view.findViewById(R.id.call_action_release_and_accept);
        mActionReject = (Button) view.findViewById(R.id.call_action_reject);
        mActionTerminate = (Button) view.findViewById(R.id.call_action_terminate);
        mActionHold = (Button) view.findViewById(R.id.call_action_hold);
        mActionRespondAndHold = (Button) view.findViewById(R.id.call_action_respond_and_hold);
        mActionPrivateMode = (Button) view.findViewById(R.id.call_action_private_mode);
        mActionExplicitTransfer = (Button) view.findViewById(R.id.call_action_explicit_transfer);

        mActionAccept.setEnabled(false);
        mActionHoldAndAccept.setEnabled(false);
        mActionReleaseAndAccept.setEnabled(false);
        mActionReject.setEnabled(false);
        mActionTerminate.setEnabled(false);
        mActionHold.setEnabled(false);
        mActionRespondAndHold.setEnabled(false);
        mActionPrivateMode.setEnabled(false);
        mActionExplicitTransfer.setEnabled(false);

        mActionAccept.setOnClickListener(this);
        mActionHoldAndAccept.setOnClickListener(this);
        mActionReleaseAndAccept.setOnClickListener(this);
        mActionReject.setOnClickListener(this);
        mActionTerminate.setOnClickListener(this);
        mActionHold.setOnClickListener(this);
        mActionRespondAndHold.setOnClickListener(this);
        mActionPrivateMode.setOnClickListener(this);
        mActionExplicitTransfer.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mActivity = (HfpTestActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(this.getClass().getSimpleName()
                    + " can be only attached to " + HfpTestActivity.class.getSimpleName());
        }
    }

    public void onCallChanged(BluetoothHeadsetClientCall call) {
        Log.v(TAG, "onCallChanged(): call=" + HfpTestActivity.callToJson(call));

        CallsAdapter adapter = (CallsAdapter) mCallsList.getAdapter();

        if (call.getState() == BluetoothHeadsetClientCall.CALL_STATE_TERMINATED) {
            adapter.remove(call);
        } else {
            // add will also update existing call with the same id
            adapter.add(call);
        }

        updateActionsState();
    }

    @Override
    public void onClick(View v) {
        BluetoothHeadsetClient cli = mActivity.mBluetoothHeadsetClient;
        BluetoothDevice device = mActivity.mDevice;
        CallsAdapter adapter = (CallsAdapter) mCallsList.getAdapter();
        BluetoothHeadsetClientCall selectedCall = adapter.getSelected();
        boolean result = true;
        if (device == null) {
            Log.d(TAG,"Device is NULL");
            List<BluetoothDevice> deviceList = cli.getConnectedDevices();
            if (deviceList.size() > 0) {
                for (int i = 0; i < deviceList.size(); i++) {
                     device = deviceList.get(i);
                     Log.d(TAG,"conncted to device " + device +
                             " state " + cli.getConnectionState(device));
                }
            } else {
                Log.e(TAG,"Connected devices for HFP Client is NULL");
            }
        }

        switch (v.getId()) {
            case R.id.call_action_accept:
                result = cli.acceptCall(device, BluetoothHeadsetClient.CALL_ACCEPT_NONE);
                break;

            case R.id.call_action_hold_and_accept:
                result = cli.acceptCall(device, BluetoothHeadsetClient.CALL_ACCEPT_HOLD);
                break;

            case R.id.call_action_release_and_accept:
                result = cli.acceptCall(device, BluetoothHeadsetClient.CALL_ACCEPT_TERMINATE);
                break;

            case R.id.call_action_reject:
                result = cli.rejectCall(device);
                break;

            case R.id.call_action_terminate:
                result = cli.terminateCall(device, selectedCall == null ? 0 : selectedCall.getId());
                break;

            case R.id.call_action_hold:
                result = cli.holdCall(device);
                break;

            case R.id.call_action_respond_and_hold:
                result = cli.holdCall(device);
                break;

            case R.id.call_action_private_mode:
                result = cli.enterPrivateMode(device, selectedCall.getId());
                break;

            case R.id.call_action_explicit_transfer:
                result = cli.explicitCallTransfer(device);
                break;
        }

        if (!result) {
            Toast.makeText(getActivity(), "\"" + ((TextView) v).getText() + "\" FAILED",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((CallsAdapter) parent.getAdapter()).toggleSelected((int) id);

        updateActionsState();
    }

    public void onConnStateChanged(int state, int prevState) {
        if (state == BluetoothProfile.STATE_DISCONNECTED) {
            ((CallsAdapter) mCallsList.getAdapter()).removeAll();

            updateActionsState();
        }
    }

    private void updateActionsState() {
        CallsAdapter adapter = (CallsAdapter) mCallsList.getAdapter();
        BluetoothHeadsetClientCall selectedCall = adapter.getSelected();

        boolean canAccept = false;
        boolean canHoldAndAccept = false;
        boolean canReleaseAndAccept = false;
        boolean canReject = false;
        boolean canTerminate = false;
        boolean canRespondAndHold = false;
        boolean canHold = false;

        boolean hasIncoming = adapter.hasCallsInState(BluetoothHeadsetClientCall.CALL_STATE_INCOMING);
        boolean hasActive = adapter.hasCallsInState(BluetoothHeadsetClientCall.CALL_STATE_ACTIVE);
        boolean hasWaiting = adapter.hasCallsInState(BluetoothHeadsetClientCall.CALL_STATE_WAITING);
        boolean hasHeld = adapter.hasCallsInState(BluetoothHeadsetClientCall.CALL_STATE_HELD,
                BluetoothHeadsetClientCall.CALL_STATE_HELD_BY_RESPONSE_AND_HOLD);

        if (hasIncoming) {
            canAccept = true;
            canReject = mActivity.mFeatReject;
            canRespondAndHold = true;

            mActionAccept.setText(R.string.call_action_accept);
            mActionReject.setText(R.string.call_action_reject);
        }

        if (hasActive || adapter.hasCallsInState(BluetoothHeadsetClientCall.CALL_STATE_ALERTING,
                                                BluetoothHeadsetClientCall.CALL_STATE_DIALING)) {
            canTerminate = true;
        }

        if (hasHeld) {
            canAccept = true;
            canReject = mActivity.mFeatReject;
            canHoldAndAccept = mActivity.mFeatAcceptHeldOrWaiting;
        }

        if (hasWaiting) {
            canReject = mActivity.mFeatReject;
            mActionAccept.setText(R.string.call_action_accept);
            mActionReject.setText(R.string.call_action_reject);

            if (hasActive) {
                canHoldAndAccept = mActivity.mFeatAcceptHeldOrWaiting;
                canReleaseAndAccept = mActivity.mFeatReleaseAndAccept;

                mActionHoldAndAccept.setText(R.string.call_action_hold_and_accept);
            } else if (hasHeld) {
                canAccept = true;
                canHoldAndAccept = false;
                canReleaseAndAccept = false;
            }
        }

        if (adapter.hasCallsInState(BluetoothHeadsetClientCall.CALL_STATE_ACTIVE) &&
                !adapter.hasCallsInState(BluetoothHeadsetClientCall.CALL_STATE_HELD,
                        BluetoothHeadsetClientCall.CALL_STATE_DIALING,
                        BluetoothHeadsetClientCall.CALL_STATE_ALERTING,
                        BluetoothHeadsetClientCall.CALL_STATE_INCOMING,
                        BluetoothHeadsetClientCall.CALL_STATE_WAITING,
                        BluetoothHeadsetClientCall.CALL_STATE_HELD_BY_RESPONSE_AND_HOLD)) {
            canHold = true;
        }

        // TODO: this should be perhaps done in some more reasonable way
        if (hasHeld && !hasWaiting) {
            if (hasActive) {
                canAccept = true;
                canReleaseAndAccept = mActivity.mFeatReleaseAndAccept;

                mActionAccept.setText(R.string.call_action_merge);
                mActionHoldAndAccept.setText(R.string.call_action_swap);
                mActionReject.setText(R.string.call_action_reject_held);
            } else {
                canAccept = false;
                mActionAccept.setText(R.string.call_action_accept);
                mActionHoldAndAccept.setText(R.string.call_action_accept_held);
                mActionReject.setText(R.string.call_action_reject);
                // Actions for incoming call when there is already a held one.
                // We cannot accept the held one now. Instead handle the incoming call first.
                if (hasIncoming) {
                    canAccept = true;
                    canHoldAndAccept = false;
                }
            }
        } else {
            mActionHoldAndAccept.setText(R.string.call_action_hold_and_accept);
        }

        mActionAccept.setEnabled(canAccept);
        mActionHoldAndAccept.setEnabled(canHoldAndAccept);
        mActionReleaseAndAccept.setEnabled(canReleaseAndAccept);
        mActionReject.setEnabled(canReject);
        mActionTerminate.setEnabled(canTerminate);
        mActionRespondAndHold.setEnabled(canRespondAndHold);

        mActionHold.setEnabled(canHold);

        mActionPrivateMode.setEnabled(mActivity.mFeatEnhancedCallControl && selectedCall != null && selectedCall.isMultiParty());

        mActionExplicitTransfer.setEnabled(mActivity.mFeatMergeDetach && (adapter.getCount() > 1));
    }
}
