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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

public class MonkeyActivity extends Activity {

    private static final String TAG = "MonkeyActivity";

    public static final String ACTION_MONKEY = "org.codeaurora.bluetooth.action.MONKEY";

    public static final String ACTION_MONKEY_QUERY = "org.codeaurora.bluetooth.action.MONKEY_QUERY";

    public static final String EXTRA_OP = "extra.op";
    public static final String EXTRA_DIALOG_TAG = "extra.dialogTag";
    public static final String EXTRA_ID = "extra.id";
    public static final String EXTRA_TEXT = "extra.text";

    public static final String OP_DUMP = "dump";
    public static final String OP_SELECT_TAB = "selectTab";
    public static final String OP_CLICK = "click";
    public static final String OP_LONG_CLICK = "longClick";
    public static final String OP_SET_TEXT = "setText";
    public static final String OP_SELECT_BY_TEXT = "selectByText";
    public static final String OP_CLICK_BUTTON = "clickButton";
    public static final String OP_CLICK_ACTION_BAR_MENU_BY_TEXT = "clickActionBarMenuByText";

    protected Menu mActionBarMenu = null;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "received " + action);

            if (ACTION_MONKEY.equals(action)) {
                String op = intent.getStringExtra(EXTRA_OP);
                String dialogTag = intent.getStringExtra(EXTRA_DIALOG_TAG);
                String id = intent.getStringExtra(EXTRA_ID);
                String text = intent.getStringExtra(EXTRA_TEXT);

                if (OP_DUMP.equals(op)) {
                    getViewById(dialogTag, null);
                } else if (OP_SELECT_TAB.equals(op)) {
                    monkeySelectTab(text);
                } else if (OP_CLICK.equals(op)) {
                    monkeyClick(dialogTag, id, false);
                } else if (OP_LONG_CLICK.equals(op)) {
                    monkeyClick(dialogTag, id, true);
                } else if (OP_SET_TEXT.equals(op)) {
                    monkeySetText(dialogTag, id, text);
                } else if (OP_SELECT_BY_TEXT.equals(op)) {
                    monkeySelectByText(dialogTag, id, text);
                } else if (OP_CLICK_BUTTON.equals(op)) {
                    monkeyClickButton(dialogTag, id);
                } else if (OP_CLICK_ACTION_BAR_MENU_BY_TEXT.equals(op)) {
                    monkeyClickActionBarMenuByText(text);
                }
            } else if (ACTION_MONKEY_QUERY.equals(action)) {
                String op = intent.getStringExtra(EXTRA_OP);

                if (op != null) {
                    onMonkeyQuery(op, intent.getExtras());
                }
            }
        }
    };

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_MONKEY);
        filter.addAction(ACTION_MONKEY_QUERY);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();

        unregisterReceiver(mReceiver);
    }

    private View getViewById(View view, String id, String tag, int nest) {
        if (view == null) {
            return null;
        }

        String tid = null;

        final int vid = view.getId();
        final Resources res = view.getResources();
        if (vid != View.NO_ID && vid != 0 && res != null) {
            if ((vid & 0xff000000) == 0x7f000000) {
                tid = res.getResourceEntryName(vid);

                if (id != null) {
                    boolean match = false;
                    if (id.equals(tid)) {
                        if (tag == null) {
                            match = true;
                        } else {
                            Object ttag = view.getTag();
                            match = (ttag != null && ttag.toString().equals(tag));
                        }
                    }

                    if (match) {
                        return view;
                    }
                }
            }
        }

        if (id == null) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < nest; i++) {
                sb.append("  ");
            }

            sb.append(view.getClass().getSimpleName());

            if (view instanceof TextView) {
                String txt = ((TextView) view).getText().toString();

                if (txt.length() > 0) {
                    sb.append(" \"").append(txt).append("\"");
                }
            }

            if (tid != null) {
                sb.append(" [").append(tid);
                if (view.getTag() != null) {
                    sb.append("#").append(view.getTag().toString());
                }
                sb.append("]");
            }

            Log.v("DUMP", sb.toString());
        }

        View ret = null;

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;

            int count = vg.getChildCount();
            for (int idx = 0; idx < count && ret == null; idx++) {
                ret = getViewById(vg.getChildAt(idx), id, tag, nest + 1);
            }
        }

        return ret;
    }

    private View getViewById(String dialogTag, String id) {
        View root;
        String tag = null;

        if (dialogTag != null) {
            Fragment frag = getFragmentManager().findFragmentByTag(dialogTag);

            if (!(frag instanceof DialogFragment)) {
                return null;
            }

            root = ((DialogFragment) frag).getDialog().findViewById(android.R.id.content);
        } else {
            root = findViewById(android.R.id.content);
        }

        if (id != null) {
            int hashPos = id.lastIndexOf("#");
            if (hashPos > 0) {
                tag = id.substring(hashPos + 1);
                id = id.substring(0, hashPos);
            }
        }

        return getViewById(root, id, tag, 0);
    }

    private void monkeySelectTab(String text) {
        ActionBar ab = getActionBar();

        if (ab == null) {
            Log.w(TAG, "selectTab not found");
            return;
        }

        int count = ab.getTabCount();
        for (int idx = 0; idx < count; idx++) {
            ActionBar.Tab tab = ab.getTabAt(idx);
            if (tab.getText().toString().toLowerCase().equals(text.toLowerCase())) {
                ab.selectTab(tab);
                return;
            }
        }
    }

    private void monkeyClick(String dialogTag, String id, boolean longClick) {
        View view = getViewById(dialogTag, id);

        if (view == null) {
            Log.w(TAG, "click(" + dialogTag + ", " + id + ") not found");
            return;
        }

        if (longClick) {
            view.performLongClick();
        } else {
            view.performClick();
        }
    }

    private void monkeySetText(String dialogTag, String id, String text) {
        View view = getViewById(dialogTag, id);

        if (view == null || !(view instanceof TextView)) {
            Log.w(TAG, "setText(" + dialogTag + ", " + id + ") not found");
            return;
        }

        TextView txt = (TextView) view;
        txt.setText(text);
    }

    private void monkeySelectByText(String dialogTag, String id, String text) {
        View view = getViewById(dialogTag, id);

        if (view == null || !(view instanceof AdapterView)) {
            Log.w(TAG, "selectByText(" + dialogTag + ", " + id + ") not found");
            return;
        }

        @SuppressWarnings("unchecked")
        AdapterView<Adapter> adv = (AdapterView<Adapter>) view;

        int count = adv.getCount();
        for (int idx = 0; idx < count; idx++) {
            if (adv.getItemAtPosition(idx).toString().equals(text)) {
                adv.setSelection(idx);
                return;
            }
        }
    }

    private void monkeyClickButton(String dialogTag, String id) {
        if (dialogTag == null) {
            return;
        }

        int which;

        try {
            which = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return;
        }

        Fragment frag = getFragmentManager().findFragmentByTag(dialogTag);

        if (!(frag instanceof DialogFragment)) {
            return;
        }

        Dialog dlg = ((DialogFragment) frag).getDialog();

        if (!(dlg instanceof AlertDialog)) {
            return;
        }

        Button btn = ((AlertDialog) dlg).getButton(which);

        btn.performClick();
    }

    private void monkeyClickActionBarMenuByText(String text) {
        if (mActionBarMenu == null || text == null) {
            return;
        }

        for (int idx = 0; idx < mActionBarMenu.size(); idx++) {
            MenuItem item = mActionBarMenu.getItem(idx);

            String title = item.getTitle().toString();

            if (title.toLowerCase().equals(text.toLowerCase())) {
                mActionBarMenu.performIdentifierAction(item.getItemId(), 0);
                break;
            }
        }
    }

    protected void onMonkeyQuery(String op, Bundle params) {
        // do nothing
    }
}
