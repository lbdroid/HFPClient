package tk.rabidbeaver.hfpclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.codeaurora.bluetooth.bttestapp.HfpTestActivity;
import org.codeaurora.bluetooth.bttestapp.MainActivity;
import org.codeaurora.bluetooth.bttestapp.ProfileService;

import java.util.List;
import java.util.Set;

import wrapper.android.bluetooth.BluetoothHeadsetClient;
import wrapper.android.bluetooth.BluetoothHeadsetClientCall;

public class HFPNotificationService extends Service {
    private final String TAG = "HFPNotificationService";

    private static boolean undestroyed = true;

    private static Notification notification;
    BluetoothDevice mDevice;
    BluetoothHeadsetClient mBluetoothHeadsetClient;
    ProfileService mProfileService = null;

    private boolean connected = false;
    private boolean cellConnected = false;
    private int sigStrength = -1;
    private boolean roaming = false;
    private int cellBattery = 0;
    private boolean onCall = false;
    private String ringingNumber = "";
    private String callNumber = "";
    private int callId = 0;
    private boolean ringing = false;
    private long ringingHoldover = 0;
    private boolean audioConnected = false;
    int totalCalls = 0;

    private final BroadcastReceiver mHfpClientReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mHfpClientReceiver running onReceive");
            String action = intent.getAction();

            if (!isConnected()){
                Log.d(TAG, "Received HFP broadcast, but unable to process");
                if (mBluetoothHeadsetClient == null) Log.d(TAG, "hfpClient is NULL");
                if (mBluetoothHeadsetClient.hfpClientInstance == null) Log.d(TAG, "hfpClientInstance is NULL");
                if (mDevice == null) Log.d(TAG, "device is NULL");
                try {
                    if (mBluetoothHeadsetClient.getConnectionState(mDevice) != BluetoothProfile.STATE_CONNECTED)
                        Log.d(TAG, "device is not connected as HFP client");
                } catch (NullPointerException npe){
                    Log.d(TAG, "unknown connection state");
                }
                return;
            }

            switch (action){
                case BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
                    switch (state) {
                        case BluetoothProfile.STATE_CONNECTED:
                            updateAllCalls(mBluetoothHeadsetClient.getCurrentCalls(mDevice));
                            connected = true;
                            break;
                        case BluetoothProfile.STATE_DISCONNECTED:
                            // This case DOES NOT fire when bluetooth is shut off.
                            connected = false;
                            break;
                    }
                    break;
                case BluetoothHeadsetClient.ACTION_AG_EVENT:
                    Bundle params = intent.getExtras();
                    forceUpdateAgEvents(params, false);
                    break;
                case BluetoothHeadsetClient.ACTION_CALL_CHANGED:
                    updateAllCalls(mBluetoothHeadsetClient.getCurrentCalls(mDevice));
                    break;
                case BluetoothHeadsetClient.ACTION_AUDIO_STATE_CHANGED:
                    // I don't like putting application specific hacks here, but this is a very specific and very simple
                    // broadcast. If the receiver for this broadcast doesn't exist, then it just doesn't do anything.
                    Log.d(TAG, "AUDIO_STATE_CHANGED");
                    int astate = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
                    if (astate == BluetoothHeadsetClient.STATE_AUDIO_CONNECTED){
                        Intent i = new Intent();
                        i.setAction("tk.rabidbeaver.bd37033controller.PHONE_ON");
                        sendBroadcast(i);
                        audioConnected = true;
                    } else if (astate != BluetoothHeadsetClient.STATE_AUDIO_CONNECTING){
                        Intent i = new Intent();
                        i.setAction("tk.rabidbeaver.bd37033controller.PHONE_OFF");
                        sendBroadcast(i);
                        audioConnected = false;
                    }
                    break;
                default:
                    Log.d(TAG, "unhandled ACTION: "+action);
            }

            showNotification();
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(17111, notification);
        }
    };

    private void updateAllCalls(List<BluetoothHeadsetClientCall> calls){
        totalCalls = 0;

        if (calls == null || calls.size() == 0) {
            onCall = false;
            callNumber = "";
            callId = 0;
            ringingNumber = "";
            ringing = false;
            return;
        }

        for (BluetoothHeadsetClientCall call : calls) {
            Log.v(TAG, "Updating call controls");
            int state = call.getState();
            switch(state){
                case BluetoothHeadsetClientCall.CALL_STATE_ACTIVE:
                    onCall = true;
                    callNumber = call.getNumber();
                    callId = call.getId();
                    totalCalls++;
                    break;
                case BluetoothHeadsetClientCall.CALL_STATE_HELD:
                case BluetoothHeadsetClientCall.CALL_STATE_HELD_BY_RESPONSE_AND_HOLD:
                    totalCalls++;
                    break;
                case BluetoothHeadsetClientCall.CALL_STATE_DIALING:
                    totalCalls++;
                    break;
                case BluetoothHeadsetClientCall.CALL_STATE_ALERTING:
                    totalCalls++;
                    break;
                case BluetoothHeadsetClientCall.CALL_STATE_INCOMING:
                case BluetoothHeadsetClientCall.CALL_STATE_WAITING:
                    ringingNumber = call.getNumber();
                    ringing = true;
                    totalCalls++;
                    break;
                case BluetoothHeadsetClientCall.CALL_STATE_TERMINATED:
                    // don't really care if its terminated. Just go away.
                    break;
            }
        }
    }

    private void forceUpdateAgEvents(Bundle params, boolean notify){
        if (params == null){
            cellConnected = false;
            roaming = false;
            sigStrength = -1;
            cellBattery = 0;
        } else for (String param : params.keySet()) {
            switch(param){
                case BluetoothHeadsetClient.EXTRA_IN_BAND_RING:
                    Log.d(TAG, "EXTRA_IN_BAND_RING");
                    break;
                case BluetoothHeadsetClient.EXTRA_OPERATOR_NAME:
                    Log.d(TAG, "EXTRA_OPERATOR_NAME");
                    break;
                case BluetoothHeadsetClient.EXTRA_NETWORK_STATUS:
                    cellConnected = params.getInt(param, -1) > 0;
                    break;
                case BluetoothHeadsetClient.EXTRA_NETWORK_ROAMING:
                    roaming = params.getInt(param, -1) > 0 ;
                    break;
                case BluetoothHeadsetClient.EXTRA_NETWORK_SIGNAL_STRENGTH:
                    sigStrength = params.getInt(param, -1);
                    break;
                case BluetoothHeadsetClient.EXTRA_BATTERY_LEVEL:
                    cellBattery = params.getInt(param, -1);
                    break;
                case BluetoothHeadsetClient.EXTRA_SUBSCRIBER_INFO:
                    Log.d(TAG, "EXTRA_SUBSCRIBER_INFO");
                    break;
                default:
                    Log.d(TAG, "Unexpected AG EXTRA: "+param);
            }
        }

        connected = (mBluetoothHeadsetClient != null && mDevice != null
                && mBluetoothHeadsetClient.getConnectionState(mDevice) == BluetoothProfile.STATE_CONNECTED);

        if (notify){
            showNotification();
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(17111, notification);
        }
    }

    // HFP Service Connection.
    private final ServiceConnection mHfpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mProfileService = ((ProfileService.LocalBinder) service).getService();
            mBluetoothHeadsetClient = mProfileService.getHfpClient();

            if (isConnected()) {
                updateAllCalls(mBluetoothHeadsetClient.getCurrentCalls(mDevice));
                forceUpdateAgEvents(mBluetoothHeadsetClient.getCurrentAgEvents(mDevice), true);
            } else {
                updateAllCalls(null);
                forceUpdateAgEvents(null, true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mProfileService = null;
            mBluetoothHeadsetClient = null;

            updateAllCalls(null);
            forceUpdateAgEvents(null, true);
        }
    };

    private boolean isConnected(){
        return (mDevice != null
                && mBluetoothHeadsetClient != null
                && mBluetoothHeadsetClient.hfpClientInstance != null
                && mBluetoothHeadsetClient.getConnectionState(mDevice) == BluetoothProfile.STATE_CONNECTED);
    }

    private void connectAudio(){
        if (isConnected())
            while (mBluetoothHeadsetClient.getAudioState(mDevice) != BluetoothHeadsetClient.STATE_AUDIO_CONNECTED
                    && (ringingHoldover > System.currentTimeMillis() - 10000 || onCall)) {
                if (mBluetoothHeadsetClient.getAudioState(mDevice) != BluetoothHeadsetClient.STATE_AUDIO_CONNECTING)
                    mBluetoothHeadsetClient.connectAudio();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie){
                    ie.printStackTrace();
                }
            }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent.getAction() == null) {
            showNotification();
            startForeground(17111, notification);
        } else if (isConnected() && intent.getAction().contentEquals("accept")) {
            ringingHoldover = System.currentTimeMillis();
            showNotification();
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(17111, notification);
            mBluetoothHeadsetClient.acceptCall(mDevice, BluetoothHeadsetClient.CALL_ACCEPT_NONE);
            connectAudio();
            showNotification();
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(17111, notification);
        } else if (isConnected() && intent.getAction().contentEquals("reject")) {
            ringingHoldover = System.currentTimeMillis();
            showNotification();
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(17111, notification);
            mBluetoothHeadsetClient.rejectCall(mDevice);
            showNotification();
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(17111, notification);
        } else if (isConnected() && intent.getAction().contentEquals("hangup")) {
            mBluetoothHeadsetClient.terminateCall(mDevice, intent.getIntExtra("ID", 0));
            showNotification();
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(17111, notification);
        } else if (intent.getAction().contentEquals("load")) {
            // Get a hold on our preferred DEVICE:
            String prefDevice = getSharedPreferences("bluetoothDevices", MODE_PRIVATE).getString(MainActivity.PREF_DEVICE, null);
            BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> devices = ba.getBondedDevices();
            if (devices != null) {
                for (BluetoothDevice device : devices) {
                    if (prefDevice != null && device != null && device.getAddress() != null && prefDevice.equals(device.getAddress())) {
                        mDevice = device;
                        break;
                    }
                }
            }
            showNotification();
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(17111, notification);
        } else {
            showNotification();
            startForeground(17111, notification);
        }
        return START_STICKY;
    }

    private void showNotification() {
        Intent phoneIntent = new Intent(this, HfpTestActivity.class);
        PendingIntent phonePIntent = PendingIntent.getActivity(this, 1, phoneIntent, 0);

        Intent acceptIntent = new Intent(this, HFPNotificationService.class);
        acceptIntent.setAction("accept");
        PendingIntent acceptPIntent = PendingIntent.getService(this, 0, acceptIntent, 0);

        Intent rejectIntent = new Intent(this, HFPNotificationService.class);
        rejectIntent.setAction("reject");
        PendingIntent rejectPIntent = PendingIntent.getService(this, 0, rejectIntent, 0);

        Intent hangupIntent = new Intent(this, HFPNotificationService.class);
        hangupIntent.setAction("hangup");
        hangupIntent.putExtra("ID", callId);
        PendingIntent hangupPIntent = PendingIntent.getService(this, 0, hangupIntent, 0);

        //TODO: Signal strength coming from bluetooth is in the range of 0-5, but our
        // TODO graphics only apply to the range 0-4.
        Log.d(TAG, "SIGNAL STRENGTH: "+sigStrength);

        if (sigStrength > 4) sigStrength = 4;
        if (sigStrength < 0) sigStrength = 0;
        String signalIcon = "stat_sys_signal_";
        if (!cellConnected) signalIcon+="null";
        else {
            signalIcon+=Integer.toString(sigStrength);
            if (roaming) signalIcon+="_roam";
            else signalIcon+="_fully";
        }

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("HFP Client")
                .setContentText("BT:"+(connected?1:0)+",BAT:"+cellBattery+",AUD:"+(audioConnected?1:0))
                .setSmallIcon(getResources().getIdentifier(signalIcon, "drawable", this.getPackageName()))
                .setContentIntent(phonePIntent)
                .setOngoing(true);
        // For ringingHoldover check, stop the notification sound if answer button was pressed
        // within last 15 seconds. No need to torment the user.
        if (ringing && !onCall && ringingHoldover < System.currentTimeMillis() + 15000) {
            builder.setContentTitle("INCOMING: "+ringingNumber);
            builder.addAction(R.drawable.ic_call_white_48dp, "Accept", acceptPIntent);
            builder.addAction(R.drawable.ic_call_end_white_48dp, "Reject", rejectPIntent);
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
            builder.setPriority(Notification.PRIORITY_MAX);
        } else if (ringing){
            builder.addAction(R.drawable.ic_call_white_48dp, "Accept", acceptPIntent);
            builder.addAction(R.drawable.ic_call_end_white_48dp, "Reject", rejectPIntent);
            builder.setPriority(Notification.PRIORITY_MAX);
            builder.setContentText("INCOMING: "+ringingNumber);
        } else if (onCall){
            builder.addAction(R.drawable.ic_call_end_white_48dp, "Hangup", hangupPIntent);
            builder.setContentText("Call with: "+callNumber);
        }

        notification = builder.build();
        if (ringing) notification.flags = Notification.FLAG_INSISTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // bind to app service
        Intent intent = new Intent(this, ProfileService.class);
        bindService(intent, mHfpServiceConnection, BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothHeadsetClient.ACTION_AG_EVENT);
        filter.addAction(BluetoothHeadsetClient.ACTION_CALL_CHANGED);
        filter.addAction(BluetoothHeadsetClient.ACTION_AUDIO_STATE_CHANGED);
        filter.addAction(BluetoothHeadsetClient.ACTION_RESULT);
        filter.addAction(BluetoothHeadsetClient.ACTION_LAST_VTAG);
        registerReceiver(mHfpClientReceiver, filter);

        if (isConnected()) {
            updateAllCalls(mBluetoothHeadsetClient.getCurrentCalls(mDevice));
        } else {
            Log.v(TAG,"mBluetoothHeadsetClient is null");
        }

        undestroyed = true;
        new Thread(new Runnable(){
            public void run(){
                while (undestroyed) {
                    try {
                        Thread.sleep(10000);
                        checkConn();
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void checkConn(){
        if (mProfileService != null){
            boolean reloadNotification = false;
            if (mDevice == null){
                String prefDevice = getSharedPreferences("bluetoothDevices", MODE_PRIVATE).getString(MainActivity.PREF_DEVICE, null);
                BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
                Set<BluetoothDevice> devices = ba.getBondedDevices();
                if (devices != null) {
                    for (BluetoothDevice device : devices) {
                        if (prefDevice != null && device != null && device.getAddress() != null && prefDevice.equals(device.getAddress())) {
                            mDevice = device;
                            break;
                        }
                    }
                }
                if (mDevice == null){
                    Log.d(TAG, "checkConn: unable to obtain device, bailing out");
                    return;
                }
                mProfileService.setDevice(mDevice);
                reloadNotification = true;
            }

            if (mBluetoothHeadsetClient == null) {
                mBluetoothHeadsetClient = mProfileService.getHfpClient();
                reloadNotification = true;
            } else if (mBluetoothHeadsetClient.hfpClientInstance == null) {
                Log.d(TAG, "checkConn: UNEXPECTED: ProfileService received a null Proxy in mHfpServiceListener.onServiceConnected(...)");
            } else if (mBluetoothHeadsetClient.getConnectionState(mDevice) != BluetoothProfile.STATE_CONNECTED
                    && mBluetoothHeadsetClient.getConnectionState(mDevice) != BluetoothProfile.STATE_CONNECTING) {
                mBluetoothHeadsetClient.connect(mDevice);
                reloadNotification = true;
            }

            if (reloadNotification && isConnected()){
                updateAllCalls(mBluetoothHeadsetClient.getCurrentCalls(mDevice));
                forceUpdateAgEvents(mBluetoothHeadsetClient.getCurrentAgEvents(mDevice), true);
            } else if (reloadNotification){
                updateAllCalls(null);
                forceUpdateAgEvents(null, true);
            }
        } else {
            Log.d(TAG, "checkConn: mProfileService is null");
        }
    }

    @Override
    public void onDestroy(){
        undestroyed = false;
        super.onDestroy();
    }
}
