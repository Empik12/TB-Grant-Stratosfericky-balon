package fiit.baranek.tomas.gpssky.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;

/**
 * Created by Tomáš Baránek
 * This is Service for working with Battery status
 *
 */
    public class BatteryStatusService extends Service {

        public void BatteryStatusService(){

        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }


        public double getBatteryStatus(Context context){
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);

            double status = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            String bateria = String.valueOf(status);

            return status;
        }

    }
