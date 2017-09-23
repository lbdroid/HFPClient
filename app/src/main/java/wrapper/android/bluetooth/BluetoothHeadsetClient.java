package wrapper.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class BluetoothHeadsetClient {
    public BluetoothProfile hfpClientInstance;

    public BluetoothHeadsetClient(BluetoothProfile bp){
        hfpClientInstance = bp;
    }

    public static final String ACTION_CONNECTION_STATE_CHANGED =
            "android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_AG_EVENT =
            "android.bluetooth.headsetclient.profile.action.AG_EVENT";
    public static final String ACTION_CALL_CHANGED =
            "android.bluetooth.headsetclient.profile.action.AG_CALL_CHANGED";
    public static final String EXTRA_CALL =
            "android.bluetooth.headsetclient.extra.CALL";
    public static final String ACTION_AUDIO_STATE_CHANGED =
            "android.bluetooth.headsetclient.profile.action.AUDIO_STATE_CHANGED";
    public static final String ACTION_RESULT =
            "android.bluetooth.headsetclient.profile.action.RESULT";
    public static final String EXTRA_RESULT_CODE =
            "android.bluetooth.headsetclient.extra.RESULT_CODE";
    public static final String EXTRA_CME_CODE =
            "android.bluetooth.headsetclient.extra.CME_CODE";
    public static final String ACTION_LAST_VTAG =
            "android.bluetooth.headsetclient.profile.action.LAST_VTAG";
    public static final String EXTRA_NUMBER =
            "android.bluetooth.headsetclient.extra.NUMBER";

    public static final int STATE_AUDIO_DISCONNECTED = 0;
    public static final int STATE_AUDIO_CONNECTING = 1;
    public static final int STATE_AUDIO_CONNECTED = 2;

    public final static int ACTION_RESULT_OK = 0;
    public final static int ACTION_RESULT_ERROR = 1;
    public final static int ACTION_RESULT_ERROR_NO_CARRIER = 2;
    public final static int ACTION_RESULT_ERROR_BUSY = 3;
    public final static int ACTION_RESULT_ERROR_NO_ANSWER = 4;
    public final static int ACTION_RESULT_ERROR_DELAYED = 5;
    public final static int ACTION_RESULT_ERROR_BLACKLISTED = 6;
    public final static int ACTION_RESULT_ERROR_CME = 7;

    public static final String EXTRA_VOICE_RECOGNITION =
            "android.bluetooth.headsetclient.extra.VOICE_RECOGNITION";
    public static final String EXTRA_IN_BAND_RING =
            "android.bluetooth.headsetclient.extra.IN_BAND_RING";
    public static final String EXTRA_NETWORK_STATUS =
            "android.bluetooth.headsetclient.extra.NETWORK_STATUS";
    public static final String EXTRA_NETWORK_ROAMING =
            "android.bluetooth.headsetclient.extra.NETWORK_ROAMING";
    public static final String EXTRA_NETWORK_SIGNAL_STRENGTH =
            "android.bluetooth.headsetclient.extra.NETWORK_SIGNAL_STRENGTH";
    public static final String EXTRA_BATTERY_LEVEL =
            "android.bluetooth.headsetclient.extra.BATTERY_LEVEL";
    public static final String EXTRA_OPERATOR_NAME =
            "android.bluetooth.headsetclient.extra.OPERATOR_NAME";
    public static final String EXTRA_SUBSCRIBER_INFO =
            "android.bluetooth.headsetclient.extra.SUBSCRIBER_INFO";

    /**
     * AG feature: three way calling.
     */
    public final static String EXTRA_AG_FEATURE_3WAY_CALLING =
            "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_3WAY_CALLING";
    /**
     * AG feature: voice recognition.
     */
    public final static String EXTRA_AG_FEATURE_VOICE_RECOGNITION =
            "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_VOICE_RECOGNITION";
    /**
     * AG feature: fetching phone number for voice tagging procedure.
     */
    public final static String EXTRA_AG_FEATURE_ATTACH_NUMBER_TO_VT =
            "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_ATTACH_NUMBER_TO_VT";
    /**
     * AG feature: ability to reject incoming call.
     */
    public final static String EXTRA_AG_FEATURE_REJECT_CALL =
            "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_REJECT_CALL";
    /**
     * AG feature: enhanced call handling (terminate specific call, private consultation).
     */
    public final static String EXTRA_AG_FEATURE_ECC =
            "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_ECC";
    /**
     * AG feature: response and hold.
     */
    public final static String EXTRA_AG_FEATURE_RESPONSE_AND_HOLD =
            "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_RESPONSE_AND_HOLD";
    /**
     * AG call handling feature: accept held or waiting call in three way calling scenarios.
     */
    public final static String EXTRA_AG_FEATURE_ACCEPT_HELD_OR_WAITING_CALL =
            "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_ACCEPT_HELD_OR_WAITING_CALL";
    /**
     * AG call handling feature: release held or waiting call in three way calling scenarios.
     */
    public final static String EXTRA_AG_FEATURE_RELEASE_HELD_OR_WAITING_CALL =
            "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_RELEASE_HELD_OR_WAITING_CALL";
    /**
     * AG call handling feature: release active call and accept held or waiting call in three way
     * calling scenarios.
     */
    public final static String EXTRA_AG_FEATURE_RELEASE_AND_ACCEPT =
            "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_RELEASE_AND_ACCEPT";
    /**
     * AG call handling feature: merge two calls, held and active - multi party conference mode.
     */
    public final static String EXTRA_AG_FEATURE_MERGE =
            "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_MERGE";
    /**
     * AG call handling feature: merge calls and disconnect from multi party
     * conversation leaving peers connected to each other.
     * Note that this feature needs to be supported by mobile network operator
     * as it requires connection and billing transfer.
     */
    public final static String EXTRA_AG_FEATURE_MERGE_AND_DETACH =
            "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_MERGE_AND_DETACH";

    public static final int CALL_ACCEPT_NONE = 0;
    public static final int CALL_ACCEPT_HOLD = 1;
    public static final int CALL_ACCEPT_TERMINATE = 2;


    public List<BluetoothHeadsetClientCall> getCurrentCalls(BluetoothDevice device) {
        try {
            List<Object> calls = (List<Object>)hfpClientInstance.getClass().getMethod("getCurrentCalls", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
            List<BluetoothHeadsetClientCall> wrappedCalls = new ArrayList<>();

            for (Object call : calls){
                wrappedCalls.add(new BluetoothHeadsetClientCall(call));
            }
            return wrappedCalls;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return null;
    }

    public int getConnectionState(BluetoothDevice device) {
        try {
            return (int)hfpClientInstance.getClass().getMethod("getConnectionState", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return 0;
    }

    public Bundle getCurrentAgFeatures(BluetoothDevice device) {
        try {
            return (Bundle)hfpClientInstance.getClass().getMethod("getCurrentAgFeatures", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return null;
    }

    public int getAudioState(BluetoothDevice device) {
        try {
            return (int)hfpClientInstance.getClass().getMethod("getAudioState", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return 0;
    }

    public Bundle getCurrentAgEvents(BluetoothDevice device) {
        try {
            return (Bundle)hfpClientInstance.getClass().getMethod("getCurrentAgEvents", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean getLastVoiceTagNumber(BluetoothDevice device) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("getLastVoiceTagNumber", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("connect", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean disconnect(BluetoothDevice device) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("disconnect", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        try {
            return (List<BluetoothDevice>)hfpClientInstance.getClass().getMethod("getConnectedDevices", new Class[0]).invoke(hfpClientInstance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean acceptCall(BluetoothDevice device, int flag) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("acceptCall", new Class[]{BluetoothDevice.class, int.class}).invoke(hfpClientInstance, device, flag);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean holdCall(BluetoothDevice device) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("holdCall", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean rejectCall(BluetoothDevice device) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("rejectCall", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean terminateCall(BluetoothDevice device, int index) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("terminateCall", new Class[]{BluetoothDevice.class, int.class}).invoke(hfpClientInstance, device, index);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean enterPrivateMode(BluetoothDevice device, int index) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("enterPrivateMode", new Class[]{BluetoothDevice.class, int.class}).invoke(hfpClientInstance, device, index);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean explicitCallTransfer(BluetoothDevice device) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("explicitCallTransfer", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean sendDTMF(BluetoothDevice device, byte code) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("sendDTMF", new Class[]{BluetoothDevice.class, byte.class}).invoke(hfpClientInstance, device, code);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean redial(BluetoothDevice device) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("redial", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean dial(BluetoothDevice device, String number) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("dial", new Class[]{BluetoothDevice.class, String.class}).invoke(hfpClientInstance, device, number);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean dialMemory(BluetoothDevice device, int location) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("dialMemory", new Class[]{BluetoothDevice.class, int.class}).invoke(hfpClientInstance, device, location);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean connectAudio() {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("connectAudio", new Class[0]).invoke(hfpClientInstance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean disconnectAudio() {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("disconnectAudio", new Class[0]).invoke(hfpClientInstance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean startVoiceRecognition(BluetoothDevice device) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("startVoiceRecognition", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean stopVoiceRecognition(BluetoothDevice device) {
        try {
            return (boolean)hfpClientInstance.getClass().getMethod("stopVoiceRecognition", new Class[]{BluetoothDevice.class}).invoke(hfpClientInstance, device);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }
}