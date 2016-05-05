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
        /*if(networkInfo != null) {
            if (networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                    return "WIFI";
                else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    switch (networkInfo.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                            return "1xRTT";
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                            return "CDMA";
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                            return "EDGE";
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                            return "EHRPD";
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            return "EVDO 0";
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                            return "EVDO A";
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                            return "EVDO B";
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                            return "GPRS";
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                            return "HSDPA";
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                            return "HSPA";
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return "HSPAP";
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                            return "HSUPA";
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return "IDEN";
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return "LTE";
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                            return "UMTS";
                        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                            return "UNKNOWN";

                    }
                }
            } else {
                return "Netork info is not available";
            }
        }*/
        if(networkInfo != null) {

            return networkInfo.getSubtypeName();
        }
        else return "";

        }
}
