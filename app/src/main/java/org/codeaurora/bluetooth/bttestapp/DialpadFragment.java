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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import tk.rabidbeaver.hfpclient.R;

public class DialpadFragment extends Fragment implements OnClickListener, OnLongClickListener {

    private HfpTestActivity mActivity;

    private GridView mButtonsGrid;

    private EditText mNumberEdit;

    private ToggleButton mDtmfButton;

    private class ButtonAdapter extends BaseAdapter {

        private final String[] mButtons = {
                "1", "2", "3", "A",
                "4", "5", "6", "B",
                "7", "8", "9", "C",
                "*", "0", "#", "D"
        };

        private final String[] mButtonsShifted = {
                " ", " ", " ", " ",
                " ", " ", " ", " ",
                " ", " ", " ", " ",
                " ", "+", " ", " "
        };

        private final Context mContext;

        ButtonAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mButtons.length;
        }

        @Override
        public Object getItem(int position) {
            return new Pair<String, String>(mButtons[position], mButtonsShifted[position]);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Button btn;

            if (convertView == null) {
                btn = new Button(mContext);
                btn.setLayoutParams(new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                btn.setOnClickListener(DialpadFragment.this);
                btn.setOnLongClickListener(DialpadFragment.this);
            } else {
                btn = (Button) convertView;
            }

            @SuppressWarnings("unchecked")
            Pair<String, String> item = (Pair<String, String>) getItem(position);

            btn.setText(item.first);
            btn.setId(R.id.dialpad_button);
            btn.setTag(item.first);
            btn.setTag(R.id.dialpad_button_pos, position);

            return btn;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();

        View view = inflater.inflate(R.layout.dialpad_fragment, null);

        mNumberEdit = (EditText) view.findViewById(R.id.dialpad_number);
        mNumberEdit.setOnLongClickListener(this);

        mButtonsGrid = (GridView) view.findViewById(R.id.dialpad);
        mButtonsGrid.setAdapter(new ButtonAdapter(context));

        ((ImageButton) view.findViewById(R.id.dialpad_del)).setOnClickListener(this);
        ((ImageButton) view.findViewById(R.id.dialpad_del)).setOnLongClickListener(this);
        ((Button) view.findViewById(R.id.dialpad_dial)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.dialpad_memdial)).setOnClickListener(this);

        mDtmfButton = (ToggleButton) view.findViewById(R.id.dialpad_dtmf);
        mDtmfButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getView().findViewById(R.id.dialpad_number).setEnabled(!isChecked);
                getView().findViewById(R.id.dialpad_del).setEnabled(!isChecked);
                getView().findViewById(R.id.dialpad_dial).setEnabled(!isChecked);
                getView().findViewById(R.id.dialpad_memdial).setEnabled(!isChecked);
            }
        });

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialpad_button:
                int pos = (Integer) view.getTag(R.id.dialpad_button_pos);
                onClickDialpadButton(pos, false);
                break;

            case R.id.dialpad_del:
                onClickDelete();
                break;

            case R.id.dialpad_dial:
                onClickDial();
                break;

            case R.id.dialpad_memdial:
                onClickMemDial();
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.dialpad_button:
                int pos = (Integer) view.getTag(R.id.dialpad_button_pos);
                onClickDialpadButton(pos, true);
                return true;

            case R.id.dialpad_del:
                mNumberEdit.setText("");
                return true;

            case R.id.dialpad_number:
                onClickDialpadNumber();
                return true;
        }

        return false;
    }

    private void onClickDialpadNumber() {
        ClipboardManager cm = (ClipboardManager) mNumberEdit.getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);

        String txt = mNumberEdit.getText().toString();

        if (txt.isEmpty()) {
            ClipData clipData = cm.getPrimaryClip();
            if (clipData != null) {
                ClipData.Item item = clipData.getItemAt(0);
                mNumberEdit.setText(item.getText());
            }
        } else {
            cm.setPrimaryClip(ClipData.newPlainText(txt, txt));

            Toast.makeText(mNumberEdit.getContext(), "copied: " +
                    txt, Toast.LENGTH_SHORT).show();
        }
    }

    private void onClickDialpadButton(int position, boolean shift) {
        @SuppressWarnings("unchecked")
        Pair<String, String> bval = (Pair<String, String>) mButtonsGrid.getItemAtPosition(position);

        if (mDtmfButton.isChecked()) {
            mActivity.mBluetoothHeadsetClient.sendDTMF(mActivity.mDevice,
                    bval.first.getBytes()[0]);
        } else {
            if (shift) {
                mNumberEdit.append(bval.second.trim());
            } else {
                mNumberEdit.append(bval.first.trim());
            }
        }
    }

    private void onClickDelete() {
        String txt = mNumberEdit.getText().toString();

        if (txt.length() > 0) {
            txt = txt.substring(0, txt.length() - 1);
            mNumberEdit.setText(txt);
        }
    }

    private void onClickDial() {
        String number = mNumberEdit.getText().toString().trim();

        if (number.isEmpty()) {
            mActivity.mBluetoothHeadsetClient.redial(mActivity.mDevice);
        } else {
            mActivity.mBluetoothHeadsetClient.dial(mActivity.mDevice, mNumberEdit.getText()
                    .toString());
        }
    }

    private void onClickMemDial() {
        try {
            mActivity.mBluetoothHeadsetClient.dialMemory(mActivity.mDevice,
                    Integer.valueOf(mNumberEdit.getText().toString()));
        } catch (NumberFormatException e) {
            // just ignore
        }
    }

    public void setNumber(String number) {
        mNumberEdit.setText(number);
    }
}
