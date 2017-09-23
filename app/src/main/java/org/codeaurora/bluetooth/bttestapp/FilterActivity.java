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
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;

import tk.rabidbeaver.hfpclient.R;
import org.codeaurora.bluetooth.bttestapp.util.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class FilterActivity extends Activity {

    private final String TAG = "FilterActivity";

    protected Map<Integer, Integer> mParameters = new HashMap<Integer, Integer>();

    @Override
    protected void onStart() {
        super.onStart();

        long tmp = getIntent().getLongExtra("filter", 0);

        fillParameters();

        for (int i = 0; i < 64; i++) {
            if ((tmp & 0x01) == 0x01) {
                Integer id = mParameters.get(i);

                if (id != null) {
                    check(id);
                }
            }

            tmp = (tmp >> 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", getSelectedValue());
                setResult(RESULT_OK, returnIntent);
                finish();
                break;
            default:
                Logger.w(TAG, "Unknown item selected.");
                break;
        }
        return true;
    }

    protected void check(int id) {
        setChecked(id, true);
    }

    protected Boolean isChecked(int id) {
        CheckBox checkbox = (CheckBox) findViewById(id);
        if (checkbox != null) {
            return checkbox.isChecked();
        }

        Logger.w(TAG, "Check box " + id + " not found.");
        return false;
    }

    protected void setChecked(int id, Boolean check) {
        CheckBox checkbox = (CheckBox) findViewById(id);
        if (checkbox != null) {
            checkbox.setChecked(check);
        } else {
            Logger.w(TAG, "Check box " + id + " not found.");
        }
    }

    protected long getSelectedValue() {
        long result = 0;

        Iterator<Entry<Integer, Integer>> it = mParameters.entrySet().iterator();

        while (it.hasNext()) {
            Entry<Integer, Integer> pair = it.next();

            Integer key = pair.getKey();
            Integer id = pair.getValue();

            if (isChecked(id)) {
                result |= ((long) 1 << key);
            }
        }

        return result;
    }

    protected void addParameter(Integer byteNumber, Integer id) {
        mParameters.put(byteNumber, id);
    }

    abstract protected void fillParameters();

}
