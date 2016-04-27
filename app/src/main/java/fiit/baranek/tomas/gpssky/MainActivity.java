package fiit.baranek.tomas.gpssky;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Point;
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
import android.media.Image;
import android.media.ImageReader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import fiit.baranek.tomas.gpssky.SavingData.Data;
import fiit.baranek.tomas.gpssky.SavingData.DatabaseHandler;
import fiit.baranek.tomas.gpssky.Services.BatteryStatus;
import fiit.baranek.tomas.gpssky.Services.FacebookPUSH;
import fiit.baranek.tomas.gpssky.Services.Foto;
import fiit.baranek.tomas.gpssky.Services.GPS;
import fiit.baranek.tomas.gpssky.Services.MobileNetwork;
import fiit.baranek.tomas.gpssky.Services.SMS;
import fiit.baranek.tomas.gpssky.Settings.BasicSettings;
import fiit.baranek.tomas.gpssky.Settings.SMSSettings;
import fiit.baranek.tomas.gpssky.Settings.SharingSettings;

public class MainActivity extends AppCompatActivity {
    CameraDevice mCameraDevice;
    GPS gps;
    SMS sms = new SMS();
    FacebookPUSH facebookPUSH = new FacebookPUSH();
    BatteryStatus batteryStatus = new BatteryStatus();
    MobileNetwork mobileNetwork = new MobileNetwork();
    Foto fotoService;
    String message = "";

    private static final int REQUEST_CODE_BASIC_SETTINGS = 100;
    private static final int REQUEST_CODE_SHARING_SETTINGS = 200;
    private static final int REQUEST_CODE_SMS_SETTINGS = 300;
    private BasicSettings basicSeting = new BasicSettings();
    private SharingSettings sharingSeting = new SharingSettings();
    private SMSSettings smsSettings = new SMSSettings();
    private CoordinatorLayout layoutMain;
    private Switch switchFly;
    private int BestPhotoHeight = 0;
    private int BestPhotoWidth = 0;
    private int EDGEPhotoHeight = 0;
    private int EDGEPhotoWidth = 0;
    private int UMTSPhotoWidth = 0;
    private int UMTSPhotoHeight = 0;
    private int LTEPhotoWidth = 0;
    private int LTEPhotoHeight = 0;
    private int HSPAAPhotoWidth = 0;
    private int HSPAAPhotoHeight = 0;
    private int HSPAPhotoWidth = 0;
    private int HSPAPhotoHeight = 0;
    private int GPRSPhotoWidth = 0;
    private int GPRSPhotoHeight = 0;
    private Boolean externalMemory = false;
    private int DefaultFacebookInterval = 0;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    /*static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }*/

    DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());
        fotoService = new Foto(getApplicationContext());
        System.out.println("Tutoka som");
        layoutMain = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutMain);
        switchFly = (Switch) findViewById(R.id.switchFly);


        File[] externalDirectories = getApplicationContext().getExternalFilesDirs(null);
        System.out.println("Počet externých pamätí: "  + externalDirectories.length);
        if(externalDirectories[1] != null) {

            if (getApplicationContext().getExternalFilesDirs(null) != null)
                externalMemory = true;
            System.out.println("SD cadr path: " + externalMemory);
            externalMemory = true;
        }
        else
        {
            externalMemory = false;
            Snackbar snackbar = Snackbar
                    .make(layoutMain, "SD card is not available.", Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.RED);

            snackbar.show();
        }

        if(externalMemory){
            System.out.println("Cesta na pamäťovku je: " + getApplicationContext().getExternalFilesDirs(null)[1]);
            File configDirectory = new File(getApplicationContext().getExternalFilesDirs(null)[1], "config_files");
            if(!configDirectory.exists())
            {
               configDirectory.mkdirs();
            }
            File configFile = new File(configDirectory, "config.xml");
            File statusFile = new File(configDirectory, "status.csv");
            if(!configFile.exists() || !statusFile.exists()){

                if(!statusFile.exists())
                    try {
                        statusFile.createNewFile();
                    } catch (IOException e) {
                        Snackbar snackbar = Snackbar
                                .make(layoutMain, e.toString(), Snackbar.LENGTH_LONG);
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.RED);

                        snackbar.show();
                    }

                Properties prop = new Properties();

                try {

                    //set the properties value
                    prop.setProperty("BEST_photo_height", "3936");
                    prop.setProperty("BEST_photo_width", "52488");
                    prop.setProperty("LTE_photo_height","840");
                    prop.setProperty("LTE_photo_width","1600");
                    prop.setProperty("HSPA+_photo_height","840");
                    prop.setProperty("HSPA+_photo_width","1600");
                    prop.setProperty("HSPA_photo_height","840");
                    prop.setProperty("HSPA_photo_width","1600");
                    prop.setProperty("UMTS_photo_height","420");
                    prop.setProperty("UMTS_photo_width","800");
                    prop.setProperty("EDGE_photo_height","336");
                    prop.setProperty("EDGE_photo_width","640");
                    prop.setProperty("GPRS_photo_height","420");
                    prop.setProperty("GPRS_photo_width","800");
                    prop.setProperty("predicted_time_internet_connection","150000");
                    prop.setProperty("Default_facebook_interval", "30000");

                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHMMSS");
                    String currentDateandTime = sdf.format(new Date());

                    prop.setProperty("file_name", currentDateandTime);


                    //store the properties detail into a XML file
                    FileOutputStream outputStream = new FileOutputStream(configFile);
                    prop.storeToXML(outputStream, "Config file","UTF-8");

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Snackbar snackbar = Snackbar
                        .make(layoutMain, "Configuration file was created.", Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.RED);

                snackbar.show();
            }
            else
            {
                Properties prop = new Properties();

                try {
                    //load a properties file
                    prop.loadFromXML(new FileInputStream(configFile));
                    //get the property value and print it out
                    try {
                        BestPhotoHeight = Integer.parseInt(prop.getProperty("BEST_photo_height"));
                        BestPhotoWidth = Integer.parseInt(prop.getProperty("BEST_photo_width"));
                        LTEPhotoWidth = Integer.parseInt(prop.getProperty("LTE_photo_width"));
                        LTEPhotoHeight = Integer.parseInt(prop.getProperty("LTE_photo_height"));
                        HSPAAPhotoWidth = Integer.parseInt(prop.getProperty("HSPA+_photo_width"));
                        HSPAAPhotoHeight = Integer.parseInt(prop.getProperty("HSPA+_photo_height"));
                        HSPAPhotoWidth = Integer.parseInt(prop.getProperty("HSPA_photo_width"));
                        HSPAPhotoHeight = Integer.parseInt(prop.getProperty("HSPA_photo_height"));
                        UMTSPhotoWidth = Integer.parseInt(prop.getProperty("UMTS_photo_width"));
                        UMTSPhotoHeight = Integer.parseInt(prop.getProperty("UMTS_photo_height"));
                        EDGEPhotoWidth = Integer.parseInt(prop.getProperty("EDGE_photo_width"));
                        EDGEPhotoHeight = Integer.parseInt(prop.getProperty("EDGE_photo_height"));
                        GPRSPhotoWidth = Integer.parseInt(prop.getProperty("GPRS_photo_width"));
                        GPRSPhotoHeight = Integer.parseInt(prop.getProperty("GPRS_photo_height"));
                        DefaultFacebookInterval = Integer.parseInt(prop.getProperty("Default_facebook_interval"));
                        int PredictedTimeInternetConnection = Integer.parseInt(prop.getProperty("predicted_time_internet_connection"));
                        if( loadStatusData().size() == 0)
                            flyFacebookPeriod = DefaultFacebookInterval;
                        else
                            flyFacebookPeriod = PredictedTimeInternetConnection /  loadStatusData().size();
                        flyFacebookFile = prop.getProperty("file_name");

                    } catch (NumberFormatException e) {
                        Snackbar snackbar = Snackbar
                                .make(layoutMain, "Bad format of config or status file.", Snackbar.LENGTH_LONG);
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.RED);

                        snackbar.show();
                    }
                } catch (IOException ex) {
                    Snackbar snackbar = Snackbar
                            .make(layoutMain, ex.toString(), Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.RED);

                    snackbar.show();
                }
            }
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


    int stop=0;
    TimerTask task;
    TimerTask task2;

    Timer timer;
    Timer timer2;
    File fileZaloha;
    PrintWriter printWriterZaloha = null;

    TimerTask flyFacebookTask;
    Timer flyFacebookTimer;
    int flyFacebookPeriod = 1000;
    String flyFacebookFile = "";


    public void Start(View v) throws CameraAccessException {

        if (!switchFly.isChecked()) {
            if (basicSeting != null && basicSeting.getIntervalOfSending() > 0) {


                int initialDelay = 1000;
                int period = basicSeting.getIntervalOfSending() * 1000;
                gps = new GPS(MainActivity.this);

                if(basicSeting.getSave())
                    db = new DatabaseHandler(getApplicationContext(), basicSeting.getFileName());
                else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    String currentDateandTime = sdf.format(new Date());
                    db = new DatabaseHandler(getApplicationContext(), currentDateandTime);
                }

                int initialDelay2 = 1000;
                int period2 = 200;
                /*fileZaloha = new File("/storage/sdcard1/" + "/Android/data/fiit.baranek.tomas.gpssky/" + basicSeting.getFileName(), "pathZaloha.xml");
                try {
                    fileZaloha.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                timer2 = new Timer();

                task2 = new TimerTask() {
                    public void run() {
                        Data point = new Data();
                        point.setAltitude(gps.getCurrentAltitude());
                        point.setLongitude(gps.getCurrentLongitude());
                        point.setLatitude(gps.getCurrentLatitude());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                        String currentDateandTime = sdf.format(new Date());
                        point.setTime(currentDateandTime);
                        point.setNetworkConnection(mobileNetwork.getQualityOfInternetConection(getApplicationContext()));
                        point.setBattery(batteryStatus.getBatteryStatus(getApplicationContext()));
                        db.addDataPath(point);
                        Writer output;
                        try {
                            output = new BufferedWriter(new FileWriter(fileZaloha, true));
                            output.append(String.valueOf(point.getLatitude()) + "," + String.valueOf(point.getLongitude()) + "," + String.valueOf(point.getAltitude()) + "\n");
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                timer2.scheduleAtFixedRate(task2, initialDelay2, period2);
                */

                timer = new Timer();
                task = new TimerTask() {

                    public void run() {
                        //openCamera();

                        Data point = new Data();
                        point.setLatitude(gps.getCurrentLatitude());
                        point.setLongitude(gps.getCurrentLongitude());
                        point.setAltitude(gps.getCurrentAltitude());
                        point.setBattery(batteryStatus.getBatteryStatus(getApplicationContext()));
                        point.setNetworkConnection(mobileNetwork.getQualityOfInternetConection(getApplicationContext()));
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                        String currentDateandTime = sdf.format(new Date());
                        point.setTime(currentDateandTime);
                        if (isOnline()) {
                            message = "Longitude: " + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                            if (smsSettings != null) {
                                if (smsSettings.getAltitude() != null && smsSettings.getAltitude())
                                    message = message + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                                if (smsSettings.getBatteryStatus() != null && smsSettings.getBatteryStatus())
                                    message = message + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                                if (smsSettings.getDataNetwork() != null && smsSettings.getDataNetwork())
                                    message = message + "Network: " + point.getNetworkConnection();
                            }
                            //System.out.println(message);

                            String message2 = "Longitude: " + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                            if (sharingSeting != null) {
                                if (sharingSeting.getAltitude() != null && sharingSeting.getAltitude())
                                    message2 = message2 + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                                if (sharingSeting.getBatteryStatus() != null && sharingSeting.getBatteryStatus())
                                    message2 = message2 + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                                if (sharingSeting.getDataNetwork() != null && sharingSeting.getDataNetwork())
                                    message2 = message2 + "Network: " + point.getNetworkConnection() + "\n";

                            }
                            //System.out.println(message2);
                            //String outFileName = "/storage/sdcard1/" + "/Android/data/fiit.baranek.tomas.gpssky/" + basicSeting.getFileName();
                            int facebookPhotoWidth = 0;
                            int facebookPhotoHeight = 0;
                            if (point.getNetworkConnection().equals("LTE")) {
                                facebookPhotoHeight = LTEPhotoHeight;
                                facebookPhotoWidth = LTEPhotoWidth;
                            } else if (point.getNetworkConnection().equals("HSPA+")) {
                                facebookPhotoHeight = HSPAAPhotoHeight;
                                facebookPhotoWidth = HSPAAPhotoWidth;//
                            } else if (point.getNetworkConnection().equals("HSPA")) {
                                facebookPhotoHeight = HSPAPhotoHeight;
                                facebookPhotoWidth = HSPAPhotoWidth;//
                            } else if (point.getNetworkConnection().equals("UMTS")) {
                                facebookPhotoHeight = UMTSPhotoHeight;
                                facebookPhotoWidth = UMTSPhotoWidth;//
                            } else if (point.getNetworkConnection().equals("EDGE")) {
                                facebookPhotoHeight = EDGEPhotoHeight;
                                facebookPhotoWidth = EDGEPhotoWidth;
                            } else if (point.getNetworkConnection().equals("GRPS+")) {
                                facebookPhotoHeight = GPRSPhotoHeight;
                                facebookPhotoWidth = GPRSPhotoWidth;
                            }

                            if(!basicSeting.getSave()) {
                                File file= new File(getApplicationContext().getExternalFilesDirs(null)[1].getAbsolutePath() + "/default_folder");
                                file.mkdirs();
                                basicSeting.setFileName(getApplicationContext().getExternalFilesDirs(null)[1].getAbsolutePath() + "/default_folder");
                            }
                            String path = fotoService.takePicture(message2, getApplicationContext().getExternalFilesDirs(null)[1] + "/" +basicSeting.getFileName(), "201542720228451", point.getLatitude(), point.getLongitude(), point.getAltitude(), BestPhotoWidth, BestPhotoHeight, facebookPhotoWidth, facebookPhotoHeight);


                            point.setPhotoPath(path);
                            if(smsSettings != null && smsSettings.getPhoneNumber() != null && !smsSettings.getPhoneNumber().equals(""))
                            sms.sendSMS(smsSettings.getPhoneNumber(), message, getApplicationContext());
                            db.addData(point);
                        } else {
                            if(basicSeting.getSave()) {
                                String path = fotoService.takePictureWithoutFacebook(getApplicationContext().getExternalFilesDirs(null)[1] + "/" + basicSeting.getFileName(), point.getLatitude(), point.getLongitude(), point.getAltitude(), BestPhotoWidth, BestPhotoHeight);
                                point.setPhotoPath(path);
                                db.addData(point);
                            }
                            else {
                            }
                        }

                    }
                };
                timer.scheduleAtFixedRate(task, initialDelay, period);

            } else {
                Snackbar snackbar = Snackbar
                        .make(layoutMain, "Not selected basic settings.", Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.RED);

                snackbar.show();
            }
        }else
        {
            gps = new GPS(MainActivity.this);
            flyFacebookTimer = new Timer();
            final List<String> statusy = loadStatusData();
            final Random generator = new Random();
            flyFacebookTask = new TimerTask() {

                public void run() {
                    //openCamera();
                        Data point = new Data();
                        point.setLatitude(gps.getCurrentLatitude());
                        point.setLongitude(gps.getCurrentLongitude());
                        point.setAltitude(gps.getCurrentAltitude());
                        point.setBattery(batteryStatus.getBatteryStatus(getApplicationContext()));
                        point.setNetworkConnection(mobileNetwork.getQualityOfInternetConection(getApplicationContext()));
                        sharingSeting.setDataNetwork(true);
                        sharingSeting.setAltitude(true);
                        sharingSeting.setBatteryStatus(true);


                        message = "Longitude: " + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                        if (smsSettings != null) {
                            if (smsSettings.getAltitude() != null && smsSettings.getAltitude())
                                message = message + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                            if (smsSettings.getBatteryStatus() != null && smsSettings.getBatteryStatus())
                                message = message + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                            if (smsSettings.getDataNetwork() != null && smsSettings.getDataNetwork())
                                message = message + "Network: " + point.getNetworkConnection();
                        }
                        System.out.println(message);

                        String message2 = "";
                        int i = generator.nextInt(statusy.size());
                        message2 = statusy.get(i) + "\n";

                        message2 = message2 + "Naša zemepísna dĺžka: " + String.valueOf(point.getLongitude()) + "\n" + "Zemepísna šírka: " + String.valueOf(point.getLatitude()) + "\n";

                        if (sharingSeting != null) {
                            if (sharingSeting.getAltitude() != null && sharingSeting.getAltitude())
                                message2 = message2 + "Sme vo výške: " + String.valueOf(point.getAltitude()) + "\n";
                            if (sharingSeting.getBatteryStatus() != null && sharingSeting.getBatteryStatus())
                                message2 = message2 + "Máme: " + String.valueOf(point.getBattery()) + "% batérie" + "\n";
                            if (sharingSeting.getDataNetwork() != null && sharingSeting.getDataNetwork())
                                message2 = message2 + "Aktuálna mobilná sieť: " + point.getNetworkConnection() + "\n";

                        }
                        System.out.println(message2);
                        int facebookPhotoWidth = 0;
                        int facebookPhotoHeight = 0;
                        if (point.getNetworkConnection().equals("LTE")) {
                            facebookPhotoHeight = LTEPhotoHeight;
                            facebookPhotoWidth = LTEPhotoWidth;
                        } else if (point.getNetworkConnection().equals("HSPA+")) {
                            facebookPhotoHeight = HSPAAPhotoHeight;
                            facebookPhotoWidth = HSPAAPhotoWidth;//
                        } else if (point.getNetworkConnection().equals("HSPA")) {
                            facebookPhotoHeight = HSPAPhotoHeight;
                            facebookPhotoWidth = HSPAPhotoWidth;//
                        } else if (point.getNetworkConnection().equals("UMTS")) {
                            facebookPhotoHeight = UMTSPhotoHeight;
                            facebookPhotoWidth = UMTSPhotoWidth;//
                        } else if (point.getNetworkConnection().equals("EDGE")) {
                            facebookPhotoHeight = EDGEPhotoHeight;
                            facebookPhotoWidth = EDGEPhotoWidth;
                        } else if (point.getNetworkConnection().equals("GRPS+")) {
                            facebookPhotoHeight = GPRSPhotoHeight;
                            facebookPhotoWidth = GPRSPhotoWidth;
                        }

                        String path = fotoService.takePicture(message2, flyFacebookFile, "201542720228451", point.getLatitude(), point.getLongitude(), point.getAltitude(), BestPhotoWidth, BestPhotoHeight, facebookPhotoWidth, facebookPhotoHeight);

                }
            };
            flyFacebookTimer.scheduleAtFixedRate(flyFacebookTask, 1000, flyFacebookPeriod);
        }
    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        else {
            return false;
        }
    }

    public void End(View v) throws IOException {
        /*List<Data> allData = db.getAllData();
        for (Data cn : allData) {
            String log = "Id: " + String.valueOf(cn.getId()) + ", Time STAMP: " + cn.getTime() + ", Longitude: " + String.valueOf(cn.getLongitude()) + ", Latitude: " + String.valueOf(cn.getLatitude());
            System.out.println(log);
        }*/
        if(switchFly.isChecked()) {
            flyFacebookTask.cancel();
            flyFacebookTimer.cancel();
            gps.stopUsingGPS();
            gps = null;
        }
        if(task != null && timer != null && task2 != null && timer2 != null ) {
            task.cancel();
            timer.cancel();
            task2.cancel();
            timer2.cancel();
            String outFileName = "/storage/sdcard1/" + "/Android/data/fiit.baranek.tomas.gpssky/" + basicSeting.getFileName();
            exportToKML(outFileName);
            exportDatabase(outFileName);
            gps.stopUsingGPS();
            gps = null;
        }

    }




    public boolean exportDatabase(String fileName) {
        /**First of all we check if the external storage of the device is available for writing.
         * Remember that the external storage is not necessarily the sd card. Very often it is
         * the device storage.
         */
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        }
        else {
            //We use the Download directory for saving our .csv file.
            //File sdIconStorageDir = new File(
            File exportDir = new File(fileName);
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

            File file;
            PrintWriter printWriter = null;
            try
            {
                file = new File(exportDir, "path.csv");
                file.createNewFile();
                printWriter = new PrintWriter(new FileWriter(file));

                /**This is our database connector class that reads the data from the database.
                 * The code of this class is omitted for brevity.
                 */
                //DatabaseHandler dbcOurDatabaseConnector = new Da(myContext);
                //dbcOurDatabaseConnector.openForReading(); //open the database for reading

                /**Let's read the first table of the database.
                 * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
                 * containing all records of the table (all fields).
                 * The code of this class is omitted for brevity.
                 */
                Cursor curCSV = db.getCursor();
                //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
                //printWriter.println("FIRST TABLE OF THE DATABASE");
                printWriter.println("TIME,LONGITUDE,LATITUDE,ALTITUDE,BATTERY,NETWORK,PHOTO PATH");
                /*
                values.put(KEY_LATITUDE, data.getLatitude());
        values.put(KEY_LONGITUDE, data.getLongitude());
        values.put(KEY_ALTITUDE, data.getAltitude());
        values.put(KEY_PROVIDER, data.getProvider());
        values.put(KEY_BATTERY, data.getBattery());
        values.put(KEY_NETWORK_CONNECTION, data.getNetworkConnection());
        values.put(KEY_PHOTO_PATH, data.getPhotoPath());
        values.put(KEY_TIME, data.getTime());
                 */
                List<Data> allData = db.getAllData();
                for (Data cn : allData) {
                    System.out.println("Logitutka: " + cn.getLongitude());
                    String log = String.valueOf(cn.getTime()) + "," + cn.getLongitude() + "," +cn.getLatitude() + "," + cn.getAltitude() + "," + String.valueOf(cn.getBattery()) + "," + cn.getNetworkConnection() + "," + cn.getPhotoPath();
                    System.out.println(log);
                    printWriter.println(log); //write the record in the .csv file

                }

            }

            catch(Exception exc) {
                //if there are any exceptions, return false
                return false;
            }
            finally {
                if(printWriter != null) printWriter.close();
            }

            //If there are no errors, return true.
            return true;
        }
    }

    public boolean exportToKML(String fileName) {
        /**First of all we check if the external storage of the device is available for writing.
         * Remember that the external storage is not necessarily the sd card. Very often it is
         * the device storage.
         */
        System.out.println("Cesta: " + getApplicationContext().getExternalFilesDir(null).getAbsolutePath());
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        }
        else {
            //We use the Download directory for saving our .csv file.
            //File sdIconStorageDir = new File(
            File exportDir = new File(fileName);
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

            File file;
            PrintWriter printWriter = null;
            try
            {
                file = new File(exportDir, "path.xml");
                file.createNewFile();
                printWriter = new PrintWriter(new FileWriter(file));

                /**This is our database connector class that reads the data from the database.
                 * The code of this class is omitted for brevity.
                 */
                //DatabaseHandler dbcOurDatabaseConnector = new Da(myContext);
                //dbcOurDatabaseConnector.openForReading(); //open the database for reading

                /**Let's read the first table of the database.
                 * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
                 * containing all records of the table (all fields).
                 * The code of this class is omitted for brevity.
                 */
                Cursor curCSV = db.getCursor();
                List<Data> allData = db.getAllDataPath();
                System.out.println("Velkos DB 2:" + allData.size());
                for (Data cn : allData) {
                    System.out.println("DB 2: " + cn.getLongitude());
                    String log =  cn.getLatitude() + "," +cn.getLongitude() + "," + cn.getAltitude();
                    printWriter.println(log); //write the record in the .csv file

                }

            }

            catch(Exception exc) {
                //if there are any exceptions, return false
                return false;
            }
            finally {
                if(printWriter != null) printWriter.close();
            }

            //If there are no errors, return true.
            return true;
        }
    }

    public List<String> loadStatusData() {
        List<String> list = new ArrayList<>();
        InputStream fileConfig = null;
        try {
            fileConfig = new FileInputStream(getApplicationContext().getExternalFilesDirs(null)[1].getAbsolutePath()  + "/config_files/status.csv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileConfig));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        }
        catch (IOException ex) {
            // handle exception
        }
        finally {
            try {
                fileConfig.close();
            }
            catch (IOException e) {
                // handle exception
            }
        }
        return list;
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
    String iconsStoragePath = "";
    Boolean odfotil = false;
    protected String takePicture(final String message_share, String where, final String ivent) {
        odfotil =false;
        System.out.println("takePicture");
        if (null == mCameraDevice) {
            System.out.println("mCameraDevice is null, return");
            return "";
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
            int width = 52488;
            int height = 3936;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(1);
            outputSurfaces.add(reader.getSurface());
            //outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            //captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            /*int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
*/
            String timeStamp = new SimpleDateFormat("hh_mm_ss_dd_MM_yyyy").format(new Date());
            String imageFileName = "SKUSKA_" + timeStamp;

            iconsStoragePath = where;
            File sdIconStorageDir = new File(iconsStoragePath);
            sdIconStorageDir.mkdir();

            final File file = new File(sdIconStorageDir, imageFileName + ".jpg");
            iconsStoragePath = file.getAbsolutePath();
            System.out.println("Cesta: " + iconsStoragePath);
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {


                @Override
                public void onImageAvailable(ImageReader reader) {
                    odfotil = true;
                    //Toast.makeText(MainActivity.this, "Fotka bola urobena : " , Toast.LENGTH_SHORT).show();
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

                Bitmap ShrinkBitmap(String file, int width, int height){

                    BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                    bmpFactoryOptions.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

                    int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight/(float)height);
                    int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth/(float)width);

                    if (heightRatio > 1 || widthRatio > 1)
                    {
                        if (heightRatio > widthRatio)
                        {
                            bmpFactoryOptions.inSampleSize = heightRatio;
                        } else {
                            bmpFactoryOptions.inSampleSize = widthRatio;
                        }
                    }

                    bmpFactoryOptions.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
                    return bitmap;
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                        ShrinkBitmap(file.getAbsolutePath(), 800, 420).compress(Bitmap.CompressFormat.PNG, 100, stream2);
                        byte[] byteArray2 = stream2.toByteArray();
                        AccessToken token = AccessToken.getCurrentAccessToken();
                        String path = "/"+ivent+"/photos";
                        Bundle parametre = new Bundle();
                        final String send = message_share;
                        parametre.putString("message", send);
                        parametre.putString("description", "topic share");
                        parametre.putByteArray("picture", byteArray2);


                        GraphRequest request2 = new GraphRequest(token, path, parametre, HttpMethod.POST, new GraphRequest.Callback() {

                            @Override
                            public void onCompleted(GraphResponse response) {
                                JSONObject obj = response.getJSONObject();
                                if (obj != null) {
                                    Toast.makeText(MainActivity.this, "id : " + obj.optString("id"), Toast.LENGTH_SHORT).show();
                                } else {
                                    System.out.println("Všetko je zle:" + response.getError().getErrorMessage());
                                    Toast.makeText(MainActivity.this, "errof : " + response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                                    System.out.println(response.getError().getErrorMessage());
                                }
                            }
                        });

                        //request2.executeAsync();
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

                    //Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    reader.toString();
                    //Toast.makeText(MainActivity.this, "Koniec ! ! ! ! " + reader.toString() , Toast.LENGTH_SHORT).show();
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
        return iconsStoragePath;
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
}
