package fiit.baranek.tomas.gpssky.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SMS extends Service {
    public SMS() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    public void sendSMS(String phoneNo, String message, Context context){
        SmsManager smsManager = SmsManager.getDefault();
        //smsManager.
        //smsManager.se
        smsManager.sendTextMessage(phoneNo, null, message, null, null);
       // Toast.makeText(context, "SMS sent.", Toast.LENGTH_LONG).show();
    }
}
