package fiit.baranek.tomas.gpssky;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    boolean AltitudeInfoSharing = false;
    boolean PhotoInfoSharing = false;
    boolean BatteryStatusInfoSharing = false;
    boolean DataNetworkInfoSharing = false;
    boolean AltitudeInfoSMS = false;
    boolean BatteryStatusInfoSMS = false;
    boolean DataNetworkInfoSMS = false;
    int i = 0;
    String BatteryStatus = "";
    String Logitude = "";
    String Latitude = "";
    String Altitude = "";
    Camera camera;
    private int cameraId = 0;
    private CallbackManager callbackManager;
    String mCurrentPhotoPath;

    private void getBatteryPercentage() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        System.out.println("Send battery Status " + BatteryStatus);

        Bundle params = new Bundle();
        i++;
        params.putString("message", BatteryStatus + "Facebook ma zablokoval :D :D");
/* make the API call */
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {

                        FacebookRequestError error = response.getError();
                        if (error != null) {
                            System.out.println("CHyba:" + error.toString());

                        } else {
                            System.out.println("Alest okey");
                        }
                    }
                }
        ).executeAsync();
    }


    private void sendAltitude() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        Bundle params = new Bundle();
        params.putString("message", "Altitude: " + Altitude + " Logitude: " + Logitude + " Latitude: "+ Latitude );
/* make the API call */
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {

                        FacebookRequestError error = response.getError();
                        if (error != null) {
                            System.out.println("CHyba:" + error.toString());

                        } else {
                            System.out.println("Alest okey");
                        }
                    }
                }
        ).executeAsync();
    }


    public void sendPhoto(){
        safeCameraOpen(cameraId);
        SurfaceView view = new SurfaceView(this);
        try{
            camera.setPreviewDisplay(view.getHolder());
        } catch (IOException e){
            e.printStackTrace();
        }
        camera.startPreview();

        callbackManager = CallbackManager.Factory.create();


        camera.stopPreview();
        Camera.Parameters params = camera.getParameters();
        System.out.println("Parametre fotťáku: " + params.flatten());
        //params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        params.setJpegQuality(100);
        //params.getFocalLength(Camera.Parameters.AUT)
        //params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        params.setPictureSize(4608, 3456);
        params.set("orientation", "portrait");
        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        params.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        params.setPictureFormat(ImageFormat.JPEG);

        List<String> supportedFocusModes = params.getSupportedFocusModes();

        if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        camera.setParameters(params);
        camera.startPreview();
        //Camera.Size size = getOptimalSize();
        //params.setPreviewSize(size.width, size.height);
        setDisplayOrientation(camera, 90);
        camera.takePicture(null, null, mCall);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            releaseCamera();
            camera = Camera.open(id);
            qOpened = (camera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
        return qOpened;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "SKUSKA_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    Camera.PictureCallback mCall = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //decode the data obtained by the camera into a Bitmap
            //display.setImageBitmap(photo);
            Bitmap bitmapPicture
                    = BitmapFactory.decodeByteArray(data, 0, data.length);

            File destination = null;
            try {
                destination = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // set file out stream
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(destination);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // set compress format quality and stream
            bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 50, out);

           /* byte[] fotecka = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            fotecka = baos.toByteArray();

            AccessToken token = AccessToken.getCurrentAccessToken();
            String path = "me/feed";
            Bundle parametre = new Bundle();
            parametre.putString("message", "Fotečka");
            parametre.putString("description", "topic share");
            parametre.putByteArray("picture", fotecka);


            GraphRequest request = new GraphRequest(token, path, parametre, HttpMethod.POST, new GraphRequest.Callback() {

                @Override
                public void onCompleted(GraphResponse response) {
                    JSONObject obj = response.getJSONObject();
                    if (obj != null) {
                        Toast.makeText(MainActivity.this, "id : " + obj.optString("id"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "errof : " + response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                        System.out.println(response.getError().getErrorMessage());
                    }
                }
            });

            request.executeAsync();
*/

            //Message.obtain(threadHandler, AutomaticPhotoActivity.NEXT, "").sendToTarget();
            //Log.v("MyActivity","Length: "+data.length);
        }
    };


    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public void BasicSettings(View v) {
        Intent intent = new Intent(this, BasicSettingsActivity.class);
        startActivity(intent);
    }

    public void SMSSettings(View v) {
        Intent intent = new Intent(this, SMSsettingsActivity.class);
        startActivity(intent);
    }

    public void SharingSettings(View v) {
        onPause();
        Intent intent = new Intent(this, SharingSettingsActivity.class);
        startActivity(intent);

    }


    String StatusSetting = "";

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            BatteryStatus = "Battery Level Remaining: " + String.valueOf(level) + "%";
        }
    };

    public void Start(View v) {
/*

        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        final Location localization = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (localization != null) {
            Altitude = String.valueOf(localization.getAltitude());
            Latitude = String.valueOf(localization.getLatitude());
            Logitude = String.valueOf(localization.getLongitude());

        } else {
            Altitude = "noData";
            Logitude = "noData";
            Latitude = "noData";
        }

        LocationListener locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Altitude = String.valueOf(localization.getAltitude());
                Latitude = String.valueOf(localization.getLatitude());
                Logitude = String.valueOf(localization.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };*/

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {

            //setting of SHARING
            if(bundle.getBoolean("AltitudeInfoSharing")) {
                AltitudeInfoSharing = bundle.getBoolean("AltitudeInfoSharing");
                System.out.println("Atlitude info is seting for Sharing: " +AltitudeInfoSharing);
            }
            if(bundle.getBoolean("PhotoInfoSharing")) {
                PhotoInfoSharing = bundle.getBoolean("PhotoInfoSharing");
                System.out.println("PhotoInfoSharing info is seting for Sharing: " +PhotoInfoSharing);
            }
            if(bundle.getBoolean("BatteryStatusInfoSharing")) {
                BatteryStatusInfoSharing = bundle.getBoolean("BatteryStatusInfoSharing");
                System.out.println("BatteryStatusInfoSharing info is seting for Sharing: " +BatteryStatusInfoSharing);
            }
            if(bundle.getBoolean("DataNetworkInfoSharing")) {
                DataNetworkInfoSharing = bundle.getBoolean("DataNetworkInfoSharing");
                System.out.println("DataNetworkInfoSharing info is seting for Sharing: " +DataNetworkInfoSharing);
            }


            if(bundle.getString("PhoneNumber")!=null) {
                String PhoneNumber = bundle.getString("PhoneNumber");
                System.out.println(PhoneNumber);
            }
            if(bundle.getString("IntervalOfSharing")!=null) {
                String IntervalOfSharing = bundle.getString("IntervalOfSharing");
                System.out.println("Interval sťahovania je: " +IntervalOfSharing);
            }


            if(BatteryStatusInfoSharing){
                getBatteryPercentage();
            }

            if(AltitudeInfoSharing){
                sendAltitude();
            }

            if(PhotoInfoSharing){

                sendPhoto();
            }
        }


        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
               System.out.println("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                System.out.println("done!");
            }
        }.start();






        /*if(SharingInformationSet==false) StatusSetting="Sharing";
        if(SMSInformationSet==false && StatusSetting.equals(""))   StatusSetting="SMS";
            else if (SMSInformationSet==false)  StatusSetting=StatusSetting+", sms";

        if(BasicInformationSet==false && StatusSetting.equals(""))   StatusSetting="Basic";
        else if (SMSInformationSet==false)  StatusSetting=StatusSetting +", basic";

        if(StatusSetting.equals("")) Toast.makeText(MainActivity.this, "Letíme do vesmíru", Toast.LENGTH_SHORT).show();
        else Toast.makeText(MainActivity.this, StatusSetting + " information not set", Toast.LENGTH_SHORT).show();
        */
    }


    protected void setDisplayOrientation(Camera camera, int angle){
        Method downPolymorphic;
        try
        {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[] { angle });
        }
        catch (Exception e1)
        {
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }




}
