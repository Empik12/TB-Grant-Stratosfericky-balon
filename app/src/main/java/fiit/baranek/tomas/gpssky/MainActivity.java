package fiit.baranek.tomas.gpssky;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import fiit.baranek.tomas.gpssky.Services.GPS;
import fiit.baranek.tomas.gpssky.Services.SMS;
import fiit.baranek.tomas.gpssky.Settings.BasicSettings;
import fiit.baranek.tomas.gpssky.Settings.SMSSettings;
import fiit.baranek.tomas.gpssky.Settings.SharingSettings;

public class MainActivity extends Activity {
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
    CameraDevice mCameraDevice;
    Timer timer;
    MyTask task;
    TextView ProviderText;
    TextView LogitudeText;
    TextView LatitudeText;
    TextView AtitudeText;

    private static final int REQUEST_CODE_BASIC_SETTINGS = 100;
    private static final int REQUEST_CODE_SHARING_SETTINGS = 200;
    private static final int REQUEST_CODE_SMS_SETTINGS = 300;
    private BasicSettings basicSeting = new BasicSettings();
    private SharingSettings sharingSeting = new SharingSettings();
    private SMSSettings smsSettings = new SMSSettings();

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    Button btnShowLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());



        LogitudeText = (TextView) findViewById(R.id.Longitude);
        LatitudeText = (TextView) findViewById(R.id.Latitude);
        AtitudeText = (TextView) findViewById(R.id.Atitude);
        ProviderText = (TextView) findViewById(R.id.Provider);

        //serviceIntent = new Intent(this, GPS.class);
        //this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_CODE_BASIC_SETTINGS){
            if(resultCode==RESULT_OK){

                basicSeting.setFileName(data.getStringExtra("file_name"));
                basicSeting.setSave(data.getBooleanExtra("save", false));
                basicSeting.setIntervalOfSending(data.getIntExtra("interval_of_sending", 5));
            }

        } else if(requestCode == REQUEST_CODE_SHARING_SETTINGS){
            if(resultCode == RESULT_OK){
                sharingSeting.setEventID(data.getStringExtra("event_id"));
                sharingSeting.setAltitude(data.getBooleanExtra("altitude", false));
                sharingSeting.setBatteryStatus(data.getBooleanExtra("battery_status", false));
                sharingSeting.setDataNetwork(data.getBooleanExtra("data_network", false));
                sharingSeting.setPhoto(data.getBooleanExtra("photo",false));
            }

        } else if(requestCode == REQUEST_CODE_SMS_SETTINGS){
            if(resultCode == RESULT_OK) {
                smsSettings.setPhoneNumber(data.getStringExtra("phone_number"));
                smsSettings.setAltitude(data.getBooleanExtra("altitude", false));
                smsSettings.setBatteryStatus(data.getBooleanExtra("battery_status", false));
                smsSettings.setDataNetwork(data.getBooleanExtra("data_network",false));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    GPS gps;
    Intent serviceIntent;
    SMS sms = new SMS();
    public void Start(View v) {
        gps = new GPS(MainActivity.this);


        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            String provider = gps.getProvider();
            double altitude = gps.getAltitude();

            sms.sendSMS("+421919277176","Logitude:" + String.valueOf(longitude) + "\nLatitude:" + String .valueOf(latitude) + "\nAltitude:" + String .valueOf(altitude) + "\nProvider:" + String .valueOf(provider),getApplicationContext());

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("+421919277176", null, "Logitude:" + String.valueOf(longitude) + "\nLatitude:" + String .valueOf(latitude) + "\nAltitude:" + String .valueOf(altitude) + "\nProvider:" + String .valueOf(provider), null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();


            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {


            gps.showSettingsAlert();
        }
    }


    public void BasicSettings(View v) {
        Intent intent = new Intent(getApplicationContext(),BasicSettingsActivity.class);
        intent.putExtra("file_name", basicSeting.getFileName());
        intent.putExtra("interval_of_sending", basicSeting.getIntervalOfSending());
        intent.putExtra("save", basicSeting.getSave());

        //startActivity(intent);
        startActivityForResult(intent, REQUEST_CODE_BASIC_SETTINGS);
    }

    public void SMSSettings(View v) {
        Intent intent = new Intent(getApplicationContext(),SMSsettingsActivity.class);
        intent.putExtra("phone_number", smsSettings.getPhoneNumber());
        intent.putExtra("altitude", smsSettings.getAltitude());
        intent.putExtra("battery_status", smsSettings.getBatteryStatus());
        intent.putExtra("data_network", smsSettings.getDataNetwork());

        //startActivity(intent);
        startActivityForResult(intent, REQUEST_CODE_SMS_SETTINGS);
    }

    public void SharingSettings(View v) {
        Intent intent = new Intent(getApplicationContext(),SharingSettingsActivity.class);
        intent.putExtra("event_id", sharingSeting.getEventID());
        intent.putExtra("altitude", sharingSeting.getAltitude());
        intent.putExtra("battery_status", sharingSeting.getBatteryStatus());
        intent.putExtra("data_network", sharingSeting.getDataNetwork());
        intent.putExtra("photo", sharingSeting.getPhoto());

        //startActivity(intent);
        startActivityForResult(intent, REQUEST_CODE_SHARING_SETTINGS);

    }


    //Take a foto with new API CAMERA2
    private void TakeFotoCamera2() throws CameraAccessException {
        openCamera();
        System.out.println("mBtnShot clicked");
        timer = new Timer();
        task = new MyTask();
        timer.schedule(task, 5000, 5000);
    }

    class MyTask extends TimerTask {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    sendAltitude();
                    //takePicture();

                }
            });
        }

    }

    protected void takePicture() {
        System.out.println("takePicture");
        if (null == mCameraDevice) {
            System.out.println("mCameraDevice is null, return");
            return;
        }


        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());

            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(1);
            outputSurfaces.add(reader.getSurface());
            //outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "SKUSKA_" + timeStamp;

            final File file = new File(Environment.getExternalStorageDirectory() + "/DCIM", imageFileName + ".jpg");

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(ImageReader reader) {

                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                        Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        Bitmap.createScaledBitmap(b, 960, 540, false).compress(Bitmap.CompressFormat.PNG, 0, stream);
                        byte[] byteArray = stream.toByteArray();

                        AccessToken token = AccessToken.getCurrentAccessToken();
                        String path = "/795930057178952/photos";
                        Bundle parametre = new Bundle();
                        parametre.putString("message", "Poletíme do stratosféry. ;-) Zatiaľ len testujem");
                        parametre.putString("description", "topic share");
                        parametre.putByteArray("picture", byteArray);


                        GraphRequest request2 = new GraphRequest(token, path, parametre, HttpMethod.POST, new GraphRequest.Callback() {

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

                        request2.executeAsync();
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }

            };

            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {

                    super.onCaptureCompleted(session, request, result);

                    Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    //startPreview();
                }

            };

            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {

                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroudHandler);
                    } catch (CameraAccessException e) {

                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, backgroudHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void openCamera() {

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        System.out.println("openCamera E");
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        System.out.println("openCamera X");
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {

            System.out.println("onOpened");
            mCameraDevice = camera;
            //startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

            System.out.println("onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {

            System.out.println("onError");
        }

    };

    //metode for getBattery status
    private void getBatteryPercentage() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        System.out.println("Send battery Status " + BatteryStatus);

        Bundle params = new Bundle();
        i++;
        params.putString("message", String.valueOf(i) + BatteryStatus + "Facebook ma zablokoval :D :D");
/* make the API call */
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/795930057178952/feed",
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
                "/795930057178952/feed",
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

            byte[] fotecka = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            fotecka = baos.toByteArray();



            AccessToken token = AccessToken.getCurrentAccessToken();
            String path = "/795930057178952/photos";
            Bundle parametre = new Bundle();
            parametre.putString("message", "Poletíme do stratosféry. ;-) Zatiaľ len testujem");
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

        }
    };


    String StatusSetting = "";

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            BatteryStatus = "Battery Level Remaining: " + String.valueOf(level) + "%";
        }
    };





}
