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

import tk.rabidbeaver.hfpclient.R;
import org.codeaurora.bluetooth.bttestapp.util.Logger;

public class MessageFilterActivity extends FilterActivity {

    private final String TAG = "MessageFilterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.v(TAG, "onCreate()");
        ActivityHelper.initialize(this, R.layout.activity_message_filter);
        ActivityHelper.setActionBarTitle(this, R.string.title_message_filter);
    }

    @Override
    protected void fillParameters() {
        addParameter(0, R.id.param_subject);
        addParameter(1, R.id.param_datatime);
        addParameter(2, R.id.param_sender_name);
        addParameter(3, R.id.param_sender_addressing);
        addParameter(4, R.id.param_receipent_name);
        addParameter(5, R.id.param_receipent_addressing);
        addParameter(6, R.id.param_type);
        addParameter(7, R.id.param_size);
        addParameter(8, R.id.param_reception_status);
        addParameter(9, R.id.param_text);
        addParameter(10, R.id.param_attachment_size);
        addParameter(11, R.id.param_priority);
        addParameter(12, R.id.param_read);
        addParameter(13, R.id.param_sent);
        addParameter(14, R.id.param_protected);
        addParameter(15, R.id.param_replayto_addressing);
    }
}
