package fiit.baranek.tomas.gpssky;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
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
import java.sql.Time;
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
    GPS gps;
    SMS sms = new SMS();
    BatteryStatus batteryStatus = new BatteryStatus();
    MobileNetwork mobileNetwork = new MobileNetwork();
    Foto fotoService;

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
    Boolean LostMobileInternet = false;
    private double actualSpeed = 0;
    private String PhoneNumber;
    private String InitialSMStext;
    private double StartSpeed;

    private int TimeoutOfFacebookSharing = 0;
    private String SMStextForFacebookTimeout = "";
    private int IntervalOfSendingSMS = 0;
    private int IntervalOfFacebookSharing = 0;
    private int IntervalOfDataStore = 0;
    private int IntervalOfTakeFoto = 0;
    private String flyFacebookFile = "";
    private String fileNameFlyData = "";
    private String EventID = "";
    private int maxAltitudeTolerance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());
        fotoService = new Foto(getApplicationContext());

        layoutMain = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutMain);
        switchFly = (Switch) findViewById(R.id.switchFly);


        File[] externalDirectories = getApplicationContext().getExternalFilesDirs(null);
        if(externalDirectories[1] != null) {

            if (getApplicationContext().getExternalFilesDirs(null) != null)
                externalMemory = true;
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

                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHMMSS");
                    String currentDateandTime = sdf.format(new Date());
                    prop.setProperty("file_name", currentDateandTime);
                    prop.setProperty("file_name_fly_data", "dataFromFly.csv");
                    prop.setProperty("Event_id","531882530347481");

                    prop.setProperty("Timeout_of_facebook_sharing","10000");
                    prop.setProperty("SMS_text_for_facebook_timeout","Cas na zdielanie vyprsal. Fotka mala byt z miesta: ");
                    prop.setProperty("Phone_number", "+421919277176");
                    prop.setProperty("Initial_SMS_text", "Momentalne sme odstartovali. Miesto startu je: ");
                    prop.setProperty("Start_speed","10");
                    prop.setProperty("Max_altitude_tolerance","2000");
                    prop.setProperty("Interval_of_sending_SMS","90000");
                    prop.setProperty("Interval_of_Facebook_sharing","90000");
                    prop.setProperty("Interval_of_data_store","500");
                    prop.setProperty("Interval_of_take_foto","5000");



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

                        flyFacebookFile = prop.getProperty("file_name");
                        fileNameFlyData = prop.getProperty("file_name_fly_data");
                        EventID = prop.getProperty("Event_id");

                        TimeoutOfFacebookSharing = Integer.parseInt(prop.getProperty("Timeout_of_facebook_sharing"));
                        SMStextForFacebookTimeout = prop.getProperty("SMS_text_for_facebook_timeout");
                        PhoneNumber = prop.getProperty("Phone_number");
                        InitialSMStext = prop.getProperty("Initial_SMS_text");
                        StartSpeed = Double.parseDouble(prop.getProperty("Start_speed"));
                        maxAltitudeTolerance = Integer.parseInt("Max_altitude_tolerance");
                        IntervalOfSendingSMS = Integer.parseInt(prop.getProperty("Interval_of_sending_SMS"));
                        IntervalOfFacebookSharing = Integer.parseInt(prop.getProperty("Interval_of_Facebook_sharing"));
                        IntervalOfDataStore = Integer.parseInt(prop.getProperty("Interval_of_data_store"));
                        IntervalOfTakeFoto = Integer.parseInt(prop.getProperty("Interval_of_take_foto"));

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


    Boolean SMSnetwrok = true;
    Boolean InternetNetwork = true;
    Boolean AltitudeOK = false;
    double maxAltitude = 0;

    Timer flySMStimer;
    Timer flyFacebookTimer;
    Timer flyDataTimer;
    Timer flyFotoTimer;
    Timer facebookFotoTimer;
    Timer facebookTimer;
    Timer smsTimer;

    TimerTask flySMStask;
    TimerTask flyFacebookTask;
    TimerTask flyDataTask;
    TimerTask flyFotoTask;
    TimerTask facebookFotoTask;
    TimerTask facebookTask;
    TimerTask smsTask;


    public void Start(View v) throws CameraAccessException {

        if (!switchFly.isChecked()) {
            if (basicSeting != null && basicSeting.getIntervalOfSending() > 0) {


                final File exportDir;
                final File exportFile;

                if(basicSeting.getSave()) {
                    exportDir = new File(getApplicationContext().getExternalFilesDirs(null)[1] + "/" + basicSeting.getFileName());
                    if (!exportDir.exists()) {
                        exportDir.mkdirs();
                    }
                    exportFile = new File(exportDir,"path.csv");
                }else{
                    exportDir = new File(getApplicationContext().getExternalFilesDirs(null)[1] + "/" + "default_dir");
                    if (!exportDir.exists()) {
                        exportDir.mkdirs();
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHMMSS");
                    String currentDateandTime = sdf.format(new Date());
                    exportFile = new File(exportDir,currentDateandTime + ".csv");

                }



                gps = new GPS(MainActivity.this);
                if(smsSettings != null && sharingSeting == null)
                {
                    //start SMS task
                    smsTimer.scheduleAtFixedRate(smsTask, 1000, basicSeting.getIntervalOfSending() * 1000);
                }
                if(sharingSeting != null)
                {
                    //start sharing task
                    if(sharingSeting.getPhoto())
                    {
                        facebookFotoTimer.schedule(facebookFotoTask, 1000,basicSeting.getIntervalOfSending() * 1000);
                    }else
                    {
                        facebookTimer.schedule(facebookTask, 1000, basicSeting.getIntervalOfSending() * 1000);
                    }
                }


                smsTask = new TimerTask() {
                    public void run() {
                        if (isSMSnetworkAvailable()) {
                            String message =    "Longitude: " + String.valueOf(gps.getCurrentLongitude())+ "\n"+
                                                "Latitude " + String.valueOf(gps.getCurrentLatitude()) + "\n";
                            if (smsSettings != null) {
                                if (smsSettings.getAltitude() != null && smsSettings.getAltitude())
                                    message = message + "Altitude: " + String.valueOf(gps.getCurrentLatitude()) + "\n";
                                if (smsSettings.getBatteryStatus() != null && smsSettings.getBatteryStatus())
                                    message = message + "Battery status: " + String.valueOf(batteryStatus.getBatteryStatus(getApplicationContext())) + "\n";
                                if (smsSettings.getDataNetwork() != null && smsSettings.getDataNetwork())
                                    message = message + "Network: " + mobileNetwork.getQualityOfInternetConection(getApplicationContext());
                                sms.sendSMS(smsSettings.getPhoneNumber(), message, getApplicationContext());
                            }
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHMMSS");
                        String currentDateandTime = sdf.format(new Date());
                        String log = currentDateandTime + "," + String.valueOf(gps.getCurrentLongitude()) + ","  + String.valueOf(gps.getCurrentLatitude()) + "," + String.valueOf(batteryStatus.getBatteryStatus(getApplicationContext())) + "," + String.valueOf(mobileNetwork.getQualityOfInternetConection(getApplicationContext()));
                        Writer output;
                        try {
                            output = new BufferedWriter(new FileWriter(exportFile, true));
                            output.append(log + "\n");
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                };



                facebookTask = new TimerTask() {
                    public void run() {
                        Data point = new Data();
                        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHMMSS");
                        String currentDateandTime = sdf.format(new Date());
                        point.setTime(currentDateandTime);
                        point.setLatitude(gps.getCurrentLatitude());
                        point.setLongitude(gps.getCurrentLongitude());
                        point.setAltitude(gps.getCurrentAltitude());
                        point.setBattery(batteryStatus.getBatteryStatus(getApplicationContext()));
                        point.setNetworkConnection(mobileNetwork.getQualityOfInternetConection(getApplicationContext()));


                        String message2 =  "Longitude:" + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                        if (sharingSeting != null) {
                            if (sharingSeting.getAltitude() != null && sharingSeting.getAltitude())
                                message2 = message2 + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                            if (sharingSeting.getBatteryStatus() != null && sharingSeting.getBatteryStatus())
                                message2 = message2 + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                            if (sharingSeting.getDataNetwork() != null && sharingSeting.getDataNetwork())
                                message2 = message2 + "Network connection: " + point.getNetworkConnection() + "\n";

                        }

                        String message = "Longitude: " + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                        if (smsSettings != null) {
                            if (smsSettings.getAltitude() != null && smsSettings.getAltitude())
                                message = message + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                            if (smsSettings.getBatteryStatus() != null && smsSettings.getBatteryStatus())
                                message = message + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                            if (smsSettings.getDataNetwork() != null && smsSettings.getDataNetwork())
                                message = message + "Network: " + point.getNetworkConnection();
                            sms.sendSMS(smsSettings.getPhoneNumber(),message,getApplicationContext());
                        }


                        String log = String.valueOf(point.getTime()) + "," + point.getLongitude() + "," +point.getLatitude() + "," + point.getAltitude() + "," + String.valueOf(point.getBattery()) + "," + point.getNetworkConnection();
                        Writer output;
                        try {
                            output = new BufferedWriter(new FileWriter(exportFile, true));
                            output.append(log + "\n");
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        FacebookPUSH facebookPUSH = new FacebookPUSH();
                        facebookPUSH.push(message2, EventID, getApplicationContext());
                    }

                };


                facebookFotoTask = new TimerTask() {
                    public void run() {
                        Data point = new Data();
                        point.setLatitude(gps.getCurrentLatitude());
                        point.setLongitude(gps.getCurrentLongitude());
                        point.setAltitude(gps.getCurrentAltitude());
                        point.setBattery(batteryStatus.getBatteryStatus(getApplicationContext()));
                        point.setNetworkConnection(mobileNetwork.getQualityOfInternetConection(getApplicationContext()));


                        String message2 =  "Longitude:" + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                                if (sharingSeting != null) {
                                    if (sharingSeting.getAltitude() != null && sharingSeting.getAltitude())
                                        message2 = message2 + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                                    if (sharingSeting.getBatteryStatus() != null && sharingSeting.getBatteryStatus())
                                        message2 = message2 + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                                    if (sharingSeting.getDataNetwork() != null && sharingSeting.getDataNetwork())
                                        message2 = message2 + "Network connection: " + point.getNetworkConnection() + "\n";

                                }
                                int facebookPhotoWidth = 0;
                                int facebookPhotoHeight = 0;
                                if (point.getNetworkConnection().equals("LTE")) {
                                    facebookPhotoHeight = LTEPhotoHeight;
                                    facebookPhotoWidth = LTEPhotoWidth;
                                } else if (point.getNetworkConnection().equals("HSPA+")) {
                                    facebookPhotoHeight = HSPAAPhotoHeight;
                                    facebookPhotoWidth = HSPAAPhotoWidth;
                                } else if (point.getNetworkConnection().equals("HSPA")) {
                                    facebookPhotoHeight = HSPAPhotoHeight;
                                    facebookPhotoWidth = HSPAPhotoWidth;
                                } else if (point.getNetworkConnection().equals("UMTS")) {
                                    facebookPhotoHeight = UMTSPhotoHeight;
                                    facebookPhotoWidth = UMTSPhotoWidth;
                                } else if (point.getNetworkConnection().equals("EDGE")) {
                                    facebookPhotoHeight = EDGEPhotoHeight;
                                    facebookPhotoWidth = EDGEPhotoWidth;
                                } else if (point.getNetworkConnection().equals("GRPS+")) {
                                    facebookPhotoHeight = GPRSPhotoHeight;
                                    facebookPhotoWidth = GPRSPhotoWidth;
                                }

                        String message = "Longitude: " + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                        if (smsSettings != null) {
                            if (smsSettings.getAltitude() != null && smsSettings.getAltitude())
                                message = message + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                            if (smsSettings.getBatteryStatus() != null && smsSettings.getBatteryStatus())
                                message = message + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                            if (smsSettings.getDataNetwork() != null && smsSettings.getDataNetwork())
                                message = message + "Network: " + point.getNetworkConnection();
                            sms.sendSMS(smsSettings.getPhoneNumber(),message,getApplicationContext());
                        }

                        point.setPhotoPath(fotoService.takePicture(TimeoutOfFacebookSharing, SMStextForFacebookTimeout, message2, exportDir.getAbsolutePath(), EventID, point.getLatitude(), point.getLongitude(), point.getAltitude(), facebookPhotoWidth, facebookPhotoHeight, facebookPhotoWidth, facebookPhotoHeight));
                        String log = String.valueOf(point.getTime()) + "," + point.getLongitude() + "," +point.getLatitude() + "," + point.getAltitude()  + "," + String.valueOf(point.getBattery()) + "," + point.getNetworkConnection() + "," + point.getPhotoPath();
                        Writer output;
                        try {
                            output = new BufferedWriter(new FileWriter(exportFile, true));
                            output.append(log + "\n");
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                };

            } else {
                Snackbar snackbar = Snackbar
                        .make(layoutMain, "Not selected basic settings.", Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.RED);

                snackbar.show();
            }
        }else {
            gps = new GPS(MainActivity.this);

            flySMStimer = new Timer();
            flyFacebookTimer = new Timer();
            flyDataTimer = new Timer();
            flyFotoTimer = new Timer();


            final File exportDir = new File(getApplicationContext().getExternalFilesDirs(null)[1] + "/"+ flyFacebookFile);
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }
             final File fileZaloha = new File(exportDir,fileNameFlyData);

            flyDataTask = new TimerTask() {

                public void run() {
                    Data point = new Data();
                    point.setLatitude(gps.getCurrentLatitude());
                    point.setLongitude(gps.getCurrentLongitude());
                    point.setAltitude(gps.getCurrentAltitude());
                    point.setBattery(batteryStatus.getBatteryStatus(getApplicationContext()));
                    point.setNetworkConnection(mobileNetwork.getQualityOfInternetConection(getApplicationContext()));
                    point.setSpeed(gps.getCurrentSpeed());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    String currentDateandTime = sdf.format(new Date());
                    point.setTime(currentDateandTime);

                    SMSnetwrok = isSMSnetworkAvailable();
                    InternetNetwork = isOnline();
                    AltitudeOK = isAltitudeOK(point.getAltitude());
                    if(!SMSnetwrok && !InternetNetwork && maxAltitude == 0)
                        maxAltitude = point.getAltitude();

                    String log = String.valueOf(point.getTime()) + "," + point.getLongitude() + "," +point.getLatitude() + "," + point.getAltitude() + "," + point.getSpeed() + "," + String.valueOf(point.getBattery()) + "," + point.getNetworkConnection() + "," + point.getPhotoPath();
                    Writer output;
                    try {
                        output = new BufferedWriter(new FileWriter(fileZaloha, true));
                        output.append(log + "\n");
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };



            flyFotoTask = new TimerTask() {

                public void run() {
                    Data point = new Data();
                    point.setLatitude(gps.getCurrentLatitude());
                    point.setLongitude(gps.getCurrentLongitude());
                    point.setAltitude(gps.getCurrentAltitude());
                    fotoService.takePictureWithoutFacebook(exportDir.getAbsolutePath(),gps.getCurrentLatitude(),gps.getCurrentLongitude(),gps.getCurrentAltitude(),BestPhotoWidth,BestPhotoHeight);
                }
            };

            flyFotoTimer.scheduleAtFixedRate(flyFotoTask, 1000, IntervalOfTakeFoto);
            flyDataTimer.scheduleAtFixedRate(flyDataTask, 1000, IntervalOfDataStore);

            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        //check if connected!
                        while (!isSpeedGood()) {
                            actualSpeed = gps.getCurrentSpeed();
                            //Wait to connect
                            Thread.sleep(1000);
                        }
                        StartFly(gps.getCurrentLongitude(),gps.getCurrentLatitude(),gps.getCurrentAltitude());

                    } catch (Exception e) {
                    }
                }
            };
            t.start();
        }
    }

    public Boolean isSpeedGood(){
        if(actualSpeed > StartSpeed)
            return true;
        else
            return false;
    }

    public void sendInfoSMS(){
        //task.cancel();
        //timer.cancel();
        sms.sendSMS(PhoneNumber, InitialSMStext, getApplicationContext());
    }




    public void StartFly(double GPSlongitude, double GPSlatitude, double GPSaltitude) {

        String StartMessage = InitialSMStext + String.valueOf(GPSlongitude) + ", " + String.valueOf(GPSlatitude) + ", " + String.valueOf(GPSaltitude);
        sms.sendSMS(PhoneNumber, StartMessage, getApplicationContext());
        FacebookPUSH push = new FacebookPUSH();
        push.push(StartMessage, EventID, getApplicationContext());

        final File exportDir = new File(getApplicationContext().getExternalFilesDirs(null)[1] + "/"+ flyFacebookFile);
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }


        flyFacebookTask = new TimerTask() {
            final List<String> statusy = loadStatusData();
            final Random generator = new Random();
            public void run() {
                if (InternetNetwork || AltitudeOK) {
                    if (isOnline()) {
                        Data point = new Data();
                        point.setLatitude(gps.getCurrentLatitude());
                        point.setLongitude(gps.getCurrentLongitude());
                        point.setAltitude(gps.getCurrentAltitude());
                        point.setBattery(batteryStatus.getBatteryStatus(getApplicationContext()));
                        point.setNetworkConnection(mobileNetwork.getQualityOfInternetConection(getApplicationContext()));

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

                        fotoService.takePicture(TimeoutOfFacebookSharing, SMStextForFacebookTimeout, message2, exportDir.getAbsolutePath(), EventID, point.getLatitude(), point.getLongitude(), point.getAltitude(), facebookPhotoWidth, facebookPhotoHeight, facebookPhotoWidth, facebookPhotoHeight);
                    }
                }
            }
        };
        flySMStimer = new Timer();

        flySMStask = new TimerTask() {
            public void run() {
                if ((SMSnetwrok && !InternetNetwork) || AltitudeOK) {
                    if (isSMSnetworkAvailable()) {
                        Data point = new Data();
                        point.setLatitude(gps.getCurrentLatitude());
                        point.setLongitude(gps.getCurrentLongitude());
                        point.setAltitude(gps.getCurrentAltitude());
                        point.setBattery(batteryStatus.getBatteryStatus(getApplicationContext()));
                        point.setNetworkConnection(mobileNetwork.getQualityOfInternetConection(getApplicationContext()));

                        String message2 = "";
                        message2 = message2 + "Zemepisna dlzka: " + String.valueOf(point.getLongitude()) +
                                "\n" + "Zemepísna sirka: " + String.valueOf(point.getLatitude()) +
                                "\n" + "Zemepisna vyska: " + String.valueOf(point.getAltitude()) +
                                "\n" + "Stav baterie: " + String.valueOf(point.getBattery());
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

                        fotoService.takePicture(TimeoutOfFacebookSharing, SMStextForFacebookTimeout, message2, exportDir.getAbsolutePath(), EventID, point.getLatitude(), point.getLongitude(), point.getAltitude(), facebookPhotoWidth, facebookPhotoHeight, facebookPhotoWidth, facebookPhotoHeight);
                    }
                }
            }
        };
        flyFacebookTimer.scheduleAtFixedRate(flyFacebookTask, 1000, IntervalOfSendingSMS);
        flySMStimer.scheduleAtFixedRate(flySMStask, 1000, IntervalOfFacebookSharing);
    }


    public boolean isAltitudeOK(double altitude){
        if(altitude != 0 && altitude < maxAltitude + maxAltitudeTolerance)
            return true;
        else
            return false;
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

    public boolean isSMSnetworkAvailable() {
        TelephonyManager tlm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        Boolean isSMSnetworkAvalable = false;
        List<CellInfo> listCellInfo = tlm.getAllCellInfo();
        if (listCellInfo != null && !listCellInfo.isEmpty()) {
            for (CellInfo cellInfo : listCellInfo) {
                if (cellInfo.isRegistered()) {
                    isSMSnetworkAvalable = true;
                    break;
                }
            }
        }
        return isSMSnetworkAvalable;
    }

    public void End(View v) throws IOException {


        if(facebookTask!=null)
            facebookTask.cancel();
        if(facebookFotoTimer != null)
            facebookFotoTimer.cancel();

        if(facebookFotoTask != null)
            facebookFotoTask.cancel();
        if(facebookFotoTimer != null)
            facebookFotoTimer.cancel();
        if(smsTask != null)
            smsTask.cancel();
        if(smsTimer!=null)
            smsTimer.cancel();
       if(flySMStask != null)
           flySMStask.cancel();
        if(flySMStimer != null)
            flySMStimer.cancel();
        if(flyDataTask != null)
            flyDataTask.cancel();
        if(flyFacebookTimer != null)
            flyFacebookTimer.cancel();
        if(flyFotoTask!=null)
            flyFotoTask.cancel();
        if(flyFotoTimer != null)
            flyFotoTimer.cancel();
        if(gps != null) {
            gps.stopUsingGPS();
            gps = null;
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
        Intent intent = new Intent(getApplicationContext(), SharingSettingsActivity.class);
        intent.putExtra("event_id", sharingSeting.getEventID());
        intent.putExtra("altitude", sharingSeting.getAltitude());
        intent.putExtra("battery_status", sharingSeting.getBatteryStatus());
        intent.putExtra("data_network", sharingSeting.getDataNetwork());
        intent.putExtra("photo", sharingSeting.getPhoto());

        //startActivity(intent);
        startActivityForResult(intent, REQUEST_CODE_SHARING_SETTINGS);

    }
}
