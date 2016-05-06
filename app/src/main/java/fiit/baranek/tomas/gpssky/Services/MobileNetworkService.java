package fiit.baranek.tomas.gpssky.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.TelephonyManager;

public class MobileNetworkService extends Service {
    public MobileNetworkService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public String getQualityOfInternetConection(Context context){
        ConnectivityManager conectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conectivityManager.getActiveNetworkInfo();
        if(networkInfo != null) {

            return networkInfo.getSubtypeName();
        }
        else return "";

        }
}
