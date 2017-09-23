package wrapper.android.bluetooth;

import java.lang.reflect.InvocationTargetException;

public class BluetoothHeadsetClientCall {

    public Object hfpClientCallInstance;

    public BluetoothHeadsetClientCall(Object hfpcci){
        hfpClientCallInstance = hfpcci;
    }

    public static final int CALL_STATE_ACTIVE = 0;
    public static final int CALL_STATE_HELD = 1;
    public static final int CALL_STATE_DIALING = 2;
    public static final int CALL_STATE_ALERTING = 3;
    public static final int CALL_STATE_INCOMING = 4;
    public static final int CALL_STATE_WAITING = 5;
    public static final int CALL_STATE_HELD_BY_RESPONSE_AND_HOLD = 6;
    public static final int CALL_STATE_TERMINATED = 7;

    public int getState() {
        try {
            return (int)hfpClientCallInstance.getClass().getMethod("getState", new Class[0]).invoke(hfpClientCallInstance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return 0;
    }

    public String getNumber() {
        try {
            return (String)hfpClientCallInstance.getClass().getMethod("getNumber", new Class[0]).invoke(hfpClientCallInstance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return null;
    }

    public int getId() {
        try {
            return (int)hfpClientCallInstance.getClass().getMethod("getId", new Class[0]).invoke(hfpClientCallInstance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isOutgoing() {
        try {
            return (boolean)hfpClientCallInstance.getClass().getMethod("isOutgoing", new Class[0]).invoke(hfpClientCallInstance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean isMultiParty() {
        try {
            return (boolean)hfpClientCallInstance.getClass().getMethod("isMultiParty", new Class[0]).invoke(hfpClientCallInstance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return false;
    }

    public void setState(int state) {
        try {
            hfpClientCallInstance.getClass().getMethod("setState", new Class[]{int.class}).invoke(hfpClientCallInstance, state);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }
}
