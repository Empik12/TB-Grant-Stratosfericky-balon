package fiit.baranek.tomas.gpssky.Services;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.app.Service;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by Tomáš Baránek
 */
public class GPS extends Service implements LocationListener {
    private final Context mContext;


    boolean isGPSEnabled = false;

    Location currentLocation;
    double latitude;
    double longitude;
    double altitude;


    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;


    private static final long MIN_TIME_BW_UPDATES = 1;


    protected LocationManager locationManager;

    public GPS(Context context) {
        this.mContext = context;

        locationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);

        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGPSEnabled) {
            /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }*/
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            if (locationManager != null) {
                currentLocation = locationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (currentLocation != null) {
                    latitude = currentLocation.getLatitude();
                    longitude = currentLocation.getLongitude();
                    altitude = currentLocation.getAltitude();
                }
            }

        }
    }



    public void stopUsingGPS() {
        if (locationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
            }
            locationManager.removeUpdates(GPS.this);
        }
    }


    public double getCurrentLatitude() {
        if (currentLocation != null) {
            latitude = currentLocation.getLatitude();
        }

        // return latitude
        return latitude;
    }


    public double getCurrentLongitude() {
        if (currentLocation != null) {
            longitude = currentLocation.getLongitude();
        }


        return longitude;
    }


    public double getCurrentAltitude(){
        if(currentLocation != null){
            altitude = currentLocation.getAltitude();
        }

        return altitude;
    }

    public boolean getCurrentLocation(){
        if(currentLocation != null){
            return true;
        }
        else
            return false;
    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);


        alertDialog.setTitle("Warning");


        alertDialog.setMessage("Please enabled GPS from settings");


        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });


        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        currentLocation = location;

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
