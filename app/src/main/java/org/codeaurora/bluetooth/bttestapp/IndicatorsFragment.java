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
import wrapper.android.bluetooth.BluetoothHeadsetClient;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.content.Intent;

import tk.rabidbeaver.hfpclient.R;

public class IndicatorsFragment extends Fragment implements OnClickListener {

    private HfpTestActivity mActivity;

    private ToggleButton mIndConnState;

    private ToggleButton mIndAudioState;

    private ToggleButton mIndVrState;

    private TextView mIndNetworkState;

    private TextView mIndRoamingState;

    private TextView mIndInbandState;

    private TextView mIndSignalLevel;

    private TextView mIndBatteryLevel;

    private TextView mIndOperator;

    private TextView mIndSubscriber;

    private int mDefaultColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.indicators_fragment, null);

        mIndConnState = (ToggleButton) view.findViewById(R.id.ind_conn_state);
        mIndConnState.setOnClickListener(this);
        mIndAudioState = (ToggleButton) view.findViewById(R.id.ind_audio_state);
        mIndAudioState.setOnClickListener(this);
        mIndVrState = (ToggleButton) view.findViewById(R.id.ind_vr_state);
        mIndVrState.setOnClickListener(this);

        mIndNetworkState = (TextView) view.findViewById(R.id.ind_network_state);
        mIndRoamingState = (TextView) view.findViewById(R.id.ind_roaming_state);
        mIndInbandState = (TextView) view.findViewById(R.id.ind_inband_state);

        mIndSignalLevel = (TextView) view.findViewById(R.id.ind_signal_level);
        mIndBatteryLevel = (TextView) view.findViewById(R.id.ind_battery_level);

        mIndOperator = (TextView) view.findViewById(R.id.ind_operator);
        mIndSubscriber = (TextView) view.findViewById(R.id.ind_subscriber);

        mDefaultColor = mIndOperator.getTextColors().getDefaultColor();

        resetIndicators(true);

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

    public void onConnStateChanged(int state, int prevState) {
        switch (state) {
            case BluetoothProfile.STATE_DISCONNECTED:
                resetIndicators(false);
                mIndConnState.setChecked(false);
                mIndConnState.setEnabled(true);

                mIndAudioState.setEnabled(false);
                mIndVrState.setEnabled(false);
                mIndVrState.setChecked(false);
                break;

            case BluetoothProfile.STATE_CONNECTING:
                mIndConnState.setChecked(true);
                mIndConnState.setEnabled(false);
                break;

            case BluetoothProfile.STATE_DISCONNECTING:
                mIndConnState.setChecked(false);
                mIndConnState.setEnabled(false);
                break;

            case BluetoothProfile.STATE_CONNECTED:
                mIndConnState.setChecked(true);
                mIndConnState.setEnabled(true);

                mIndAudioState.setEnabled(true);
                mIndVrState.setEnabled(mActivity.mFeatVoiceRecognition);

                resetIndicators(false);
                break;
        }
    }

    public void onAudioStateChanged(int state, int prevState) {

        switch (state) {
            case BluetoothHeadsetClient.STATE_AUDIO_DISCONNECTED:

                mIndAudioState.setChecked(false);
                mIndAudioState.setEnabled(true);
                break;

            case BluetoothHeadsetClient.STATE_AUDIO_CONNECTING:

                mIndAudioState.setChecked(true);
                mIndAudioState.setEnabled(false);
                break;

            case BluetoothHeadsetClient.STATE_AUDIO_CONNECTED:

                mIndAudioState.setChecked(true);
                mIndAudioState.setEnabled(true);
                break;
        }
    }

    public void onAgEvent(Bundle params) {
        if (params == null) {
            // it can happen when querying for indicators after connection and
            // it's ok to ignore
            return;
        }

        for (String param : params.keySet()) {

            TextView colorInd = null;
            // TextView valueInd = null;
            TextView barInd = null;

            if (param.equals(BluetoothHeadsetClient.EXTRA_VOICE_RECOGNITION)) {

                boolean enabled = (params.getInt(param) != 0);
                mIndVrState.setChecked(enabled);
                mIndVrState.setEnabled(mActivity.mFeatVoiceRecognition);

            } else if (param.equals(BluetoothHeadsetClient.EXTRA_IN_BAND_RING)) {

                colorInd = mIndInbandState;

            } else if (param.equals(BluetoothHeadsetClient.EXTRA_OPERATOR_NAME)) {

                setOperator(params.getString(param));

            } else if (param.equals(BluetoothHeadsetClient.EXTRA_NETWORK_STATUS)) {

                colorInd = mIndNetworkState;

            } else if (param.equals(BluetoothHeadsetClient.EXTRA_NETWORK_ROAMING)) {

                colorInd = mIndRoamingState;

            } else if (param.equals(BluetoothHeadsetClient.EXTRA_NETWORK_SIGNAL_STRENGTH)) {

                barInd = mIndSignalLevel;

            } else if (param.equals(BluetoothHeadsetClient.EXTRA_BATTERY_LEVEL)) {

                barInd = mIndBatteryLevel;

            } else if (param.equals(BluetoothHeadsetClient.EXTRA_SUBSCRIBER_INFO)) {

                setSubscriber(params.getString(param));

            }

            if (colorInd != null) {
                if (params.getInt(param, -1) != -1) {
                    if (params.getInt(param) != 0) {
                        colorInd.setBackgroundColor(getColor(R.color.ind_on));
                    } else {
                        colorInd.setBackgroundColor(getColor(R.color.ind_off));
                    }
                    colorInd.setVisibility(View.VISIBLE);
                } else {
                    // ignore
                }
            }

            // replaced by barInd, but can be used in future
            // if (valueInd != null) {
            // if (params.getInt(param, -1) != -1) {
            // valueInd.setText(Integer.toString(params.getInt(param)));
            // } else {
            // valueInd.setText("");
            // }
            // }

            if (barInd != null) {
                int val = params.getInt(param);
                int color;

                if (val < 2) {
                    color = getColor(R.color.ind_bar_low);
                } else if (val < 4) {
                    color = getColor(R.color.ind_bar_medium);
                } else {
                    color = getColor(R.color.ind_bar_full);
                }

                SpannableString ss = new SpannableString(getResources().getString(R.string.ind_bar));
                ss.setSpan(new ForegroundColorSpan(color), 0, val, 0);
                ss.setSpan(new ForegroundColorSpan(getColor(R.color.ind_bar_off)), val,
                        ss.length(), 0);

                barInd.setText(ss);
            }
        }
    }

    private int getColor(int id) {
        return getResources().getColor(id);
    }

    private void resetIndicators(boolean all) {
        if (all) {
            mIndConnState.setChecked(false);
            mIndAudioState.setChecked(false);
            mIndVrState.setChecked(false);

            mIndConnState.setEnabled(true);
            mIndAudioState.setEnabled(false);
            mIndVrState.setEnabled(false);
        }

        mIndNetworkState.setBackgroundColor(getColor(R.color.ind_off));
        mIndRoamingState.setBackgroundColor(getColor(R.color.ind_off));
        mIndInbandState.setBackgroundColor(getColor(R.color.ind_off));

        mIndSignalLevel.setText("");
        mIndBatteryLevel.setText("");

        setOperator(null);
        setSubscriber(null);
    }

    private void setOperator(String text) {
        if (text != null) {
            mIndOperator.setText(text);
            mIndOperator.setTextColor(mDefaultColor);
        } else {
            mIndOperator.setText(R.string.ind_operator_unknown);
            mIndOperator.setTextColor(getColor(R.color.ind_text_unknown));
        }
    }

    private void setSubscriber(String text) {
        if (text != null) {
            mIndSubscriber.setText(text);
            mIndSubscriber.setTextColor(mDefaultColor);
        } else {
            mIndSubscriber.setText(R.string.ind_subscriber_unknown);
            mIndSubscriber.setTextColor(getColor(R.color.ind_text_unknown));
        }
    }

    @Override
    public void onClick(View view) {
        ///*
         //* checked state of CompoundButton is toggled automatically when clicked
         //* so since we need before-click state, it's always opposite of current
         //* state
         ///
        boolean state = !((CompoundButton) view).isChecked();

        switch (view.getId()) {
            case R.id.ind_conn_state:
                onClickConnState(state);
                break;

            case R.id.ind_audio_state:
                onClickAudioState(state);
                break;

            case R.id.ind_vr_state:
                onClickVrState(state);
                break;
        }
    }

    public void onClickConnState(boolean state) {
        if (state) {
            mActivity.mBluetoothHeadsetClient.disconnect(mActivity.mDevice);
        } else {
            mActivity.mBluetoothHeadsetClient.connect(mActivity.mDevice);
        }
    }

    public void onClickAudioState(boolean state) {
        if (state) {
            mActivity.mBluetoothHeadsetClient.disconnectAudio();
        } else {
            mActivity.mBluetoothHeadsetClient.connectAudio();
        }
    }

    public void onClickVrState(boolean state) {
        if (state) {
            mActivity.mBluetoothHeadsetClient.stopVoiceRecognition(mActivity.mDevice);
        } else {
            mActivity.mBluetoothHeadsetClient.startVoiceRecognition(mActivity.mDevice);
        }
        mIndVrState.setEnabled(false);
    }
}
