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


package org.codeaurora.bluetooth.bttestapp.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MonkeyEvent {

    private static final String TAG = "MonkeyEventStream";

    private static final String SEP = "~~";

    private final String mName;

    private final boolean mPass;

    private HashMap<String, String> mParams = null;

    private ArrayList<String> mExtReply = null;

    public MonkeyEvent(String name, boolean pass) {
        mName = name.toLowerCase();
        mPass = pass;
        mParams = new HashMap<String, String>();
    }

    public MonkeyEvent addReplyParam(String name, int value) {
        addReplyParam(name, Integer.toString(value));
        return this;
    }

    public MonkeyEvent addReplyParam(String name, long value) {
        addReplyParam(name, Long.toString(value));
        return this;
    }

    public MonkeyEvent addReplyParam(String name, String value) {
        name = name.toLowerCase();
        mParams.put(name, value);
        return this;
    }

    public MonkeyEvent addExtReply(String s) {
        if (s != null) {
            if (mExtReply == null) {
                mExtReply = new ArrayList<String>();
            }

            mExtReply.add(s);
        }

        return this;
    }

    public MonkeyEvent addExtReply(Collection<?> p) {
        if (p != null && p.size() != 0) {
            if (mExtReply == null) {
                mExtReply = new ArrayList<String>();
            }

            for (Object obj : p.toArray()) {
                mExtReply.add(obj.toString());
            }
        }

        return this;
    }

    public void send() {
        StringBuilder sb = new StringBuilder();

        if (mExtReply == null) {
            sb.append("EVENT");
        } else {
            sb.append("BEGINEXTEVENT");
        }

        sb.append(SEP);
        sb.append(mName);
        sb.append(SEP);
        sb.append(mPass ? 1 : 0);

        for (Map.Entry<String, String> entry : mParams.entrySet()) {
            sb.append(SEP);
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }

        Log.i(TAG, sb.toString());

        if (mExtReply != null) {
            for (String s : mExtReply) {
                Log.i(TAG, s);
            }

            Log.i(TAG, "ENDEXTEVENT");
        }
    }
}
