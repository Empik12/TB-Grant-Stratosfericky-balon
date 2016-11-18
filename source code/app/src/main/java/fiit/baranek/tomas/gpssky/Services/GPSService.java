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


/**
 * Created by Tomáš Baránek
 * This Service provide GPS info
 *
 */
public class GPSService extends Service implements LocationListener {

    public GPSService(){

    }

    private Context mContext;


    boolean isGPSEnabled = false;

    Location currentLocation;
    double latitude;
    double longitude;
    double altitude;
    double speed;


    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;


    private static final long MIN_TIME_BW_UPDATES = 1;


    protected LocationManager locationManager;

    public GPSService(Context context) {
        this.mContext = context;

        locationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);

        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGPSEnabled) {

            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
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
                    speed = currentLocation.getSpeed();
                }
            }

        }
    }



    public Boolean isGPSenable(){
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return true;
        else return false;
    }


    public void stopUsingGPS() {
        if (locationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            locationManager.removeUpdates(GPSService.this);
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

    public double getCurrentSpeed(){
        if(currentLocation != null){
            speed = currentLocation.getSpeed();
        }

        return speed;
    }

    public boolean getCurrentLocation(){
        if(currentLocation != null){
            return true;
        }
        else
            return false;
    }

    public Location getLocation(){
        return currentLocation;
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        speed = location.getSpeed();
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
