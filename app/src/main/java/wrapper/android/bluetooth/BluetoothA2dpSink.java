package wrapper.android.bluetooth;

import android.bluetooth.*;

public class BluetoothA2dpSink {
    public android.bluetooth.BluetoothProfile a2dpSinkInstance;

    public BluetoothA2dpSink(android.bluetooth.BluetoothProfile bp){
        a2dpSinkInstance = bp;
    }

    public static final int A2DP_SINK = 11;
}
