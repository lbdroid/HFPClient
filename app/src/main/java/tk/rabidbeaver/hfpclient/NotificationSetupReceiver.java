package tk.rabidbeaver.hfpclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationSetupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        Intent service = new Intent(context, HFPNotificationService.class);
        service.setAction("load");
        context.startService(service);
    }
}
