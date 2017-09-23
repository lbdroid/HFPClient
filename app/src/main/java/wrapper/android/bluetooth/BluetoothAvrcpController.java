package wrapper.android.bluetooth;

import android.bluetooth.*;
import android.bluetooth.BluetoothDevice;

import java.util.List;

public class BluetoothAvrcpController {

    public android.bluetooth.BluetoothProfile avrcpControllerInstance;

    public BluetoothAvrcpController(android.bluetooth.BluetoothProfile bp){
        avrcpControllerInstance = bp;
    }

    public static final String ACTION_CONNECTION_STATE_CHANGED =
            "android.bluetooth.acrcp-controller.profile.action.CONNECTION_STATE_CHANGED";

    public int getConnectionState(BluetoothDevice device) {
        //TODO: reflect this to the real implementation
        return 0;
    }

    public void sendPassThroughCmd(BluetoothDevice device, int keyCode, int keyState) {
        //TODO: reflect this to the real implementation
    }

    public List<BluetoothDevice> getConnectedDevices() {
        return null;
    }
}
