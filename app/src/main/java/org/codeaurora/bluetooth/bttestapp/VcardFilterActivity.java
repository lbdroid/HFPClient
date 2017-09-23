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

import android.os.Bundle;

import org.codeaurora.bluetooth.bttestapp.ProfileService;
import org.codeaurora.bluetooth.bttestapp.util.Logger;

import tk.rabidbeaver.hfpclient.R;

public class VcardFilterActivity extends FilterActivity {

    private final String TAG = "FilterChooserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.v(TAG, "onCreate()");
        ActivityHelper.initialize(this, R.layout.activity_vcard_filter);
        ActivityHelper.setActionBarTitle(this, R.string.title_vcard_filter);
    }

    @Override
    protected void fillParameters() {
        addParameter(0, R.id.filter_chooser_version);
        addParameter(1, R.id.filter_chooser_fn);
        addParameter(2, R.id.filter_chooser_n);
        addParameter(3, R.id.filter_chooser_photo);
        addParameter(4, R.id.filter_chooser_bday);
        addParameter(5, R.id.filter_chooser_adr);
        addParameter(6, R.id.filter_chooser_label);
        addParameter(7, R.id.filter_chooser_tel);
        addParameter(8, R.id.filter_chooser_email);
        addParameter(9, R.id.filter_chooser_mailer);
        addParameter(10, R.id.filter_chooser_tz);
        addParameter(11, R.id.filter_chooser_geo);
        addParameter(12, R.id.filter_chooser_title);
        addParameter(13, R.id.filter_chooser_role);
        addParameter(14, R.id.filter_chooser_logo);
        addParameter(15, R.id.filter_chooser_agent);
        addParameter(16, R.id.filter_chooser_org);
        addParameter(17, R.id.filter_chooser_note);
        addParameter(18, R.id.filter_chooser_rev);
        addParameter(19, R.id.filter_chooser_sound);
        addParameter(20, R.id.filter_chooser_url);
        addParameter(21, R.id.filter_chooser_uid);
        addParameter(22, R.id.filter_chooser_key);
        addParameter(23, R.id.filter_chooser_nickname);
        addParameter(24, R.id.filter_chooser_categories);
        addParameter(25, R.id.filter_chooser_proid);
        addParameter(26, R.id.filter_chooser_class);
        addParameter(27, R.id.filter_chooser_sort_string);
        addParameter(28, R.id.filter_chooser_x_irmc_call_datetime);
        addParameter(39, R.id.filter_chooser_filter);
    }
}
