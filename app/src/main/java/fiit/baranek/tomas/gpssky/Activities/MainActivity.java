package fiit.baranek.tomas.gpssky.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import fiit.baranek.tomas.gpssky.R;
import fiit.baranek.tomas.gpssky.Models.Data;
import fiit.baranek.tomas.gpssky.Services.BatteryStatusService;
import fiit.baranek.tomas.gpssky.Services.FacebookPUSHService;
import fiit.baranek.tomas.gpssky.Services.FotoService;
import fiit.baranek.tomas.gpssky.Services.GPSService;
import fiit.baranek.tomas.gpssky.Models.GPSexif;
import fiit.baranek.tomas.gpssky.Services.MobileNetworkService;
import fiit.baranek.tomas.gpssky.Services.SMSService;
import fiit.baranek.tomas.gpssky.Settings.BasicSettings;
import fiit.baranek.tomas.gpssky.Settings.SMSSettings;
import fiit.baranek.tomas.gpssky.Settings.SharingSettings;

public class MainActivity extends AppCompatActivity {
    GPSService gpsService;
    SMSService smsService = new SMSService();
    BatteryStatusService batteryStatusService = new BatteryStatusService();
    MobileNetworkService mobileNetworkService = new MobileNetworkService();
    FotoService fotoServiceService;

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
    Boolean Working = false;
    private int DistanceForSendMaps = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());
        fotoServiceService = new FotoService(getApplicationContext());

        layoutMain = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutMain);
        switchFly = (Switch) findViewById(R.id.switchFly);

        gpsService = new GPSService(getApplicationContext());
        if(gpsService.isGPSenable()){
        File[] externalDirectories = getApplicationContext().getExternalFilesDirs(null);
        if(externalDirectories[1] != null) {

            if (getApplicationContext().getExternalFilesDirs(null) != null)
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

        if(externalMemory) {
            File configDirectory = new File(getApplicationContext().getExternalFilesDirs(null)[1], "config_files");
            if (!configDirectory.exists()) {
                configDirectory.mkdirs();
            }
            File configFile = new File(configDirectory, "config.xml");
            File statusFile = new File(configDirectory, "status.csv");
            if (!configFile.exists() || !statusFile.exists()) {

                if (!statusFile.exists())
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
                    prop.setProperty("LTE_photo_height", "840");
                    prop.setProperty("LTE_photo_width", "1600");
                    prop.setProperty("HSPA+_photo_height", "840");
                    prop.setProperty("HSPA+_photo_width", "1600");
                    prop.setProperty("HSPA_photo_height", "840");
                    prop.setProperty("HSPA_photo_width", "1600");
                    prop.setProperty("UMTS_photo_height", "420");
                    prop.setProperty("UMTS_photo_width", "800");
                    prop.setProperty("EDGE_photo_height", "336");
                    prop.setProperty("EDGE_photo_width", "640");
                    prop.setProperty("GPRS_photo_height", "420");
                    prop.setProperty("GPRS_photo_width", "800");

                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHMMSS");
                    String currentDateandTime = sdf.format(new Date());
                    prop.setProperty("file_name", currentDateandTime);
                    prop.setProperty("file_name_fly_data", "dataFromFly.csv");
                    prop.setProperty("Event_id", "531882530347481");

                    prop.setProperty("Timeout_of_facebook_sharing", "10000");
                    prop.setProperty("SMS_text_for_facebook_timeout", "Cas na zdielanie vyprsal. Fotka mala byt z miesta: ");
                    prop.setProperty("Phone_number", "+421919277176");
                    prop.setProperty("Initial_SMS_text", "Momentalne sme odstartovali. Miesto startu je: ");
                    prop.setProperty("Start_speed", "10");
                    prop.setProperty("Max_altitude_tolerance", "2000");
                    prop.setProperty("Interval_of_sending_SMS", "90000");
                    prop.setProperty("Interval_of_Facebook_sharing", "90000");
                    prop.setProperty("Interval_of_data_store", "500");
                    prop.setProperty("Interval_of_take_foto", "5000");

                    ConfigFileOK = true;
                    //store the properties detail into a XML file
                    FileOutputStream outputStream = new FileOutputStream(configFile);
                    prop.storeToXML(outputStream, "Config file", "UTF-8");

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Snackbar snackbar = Snackbar
                        .make(layoutMain, "Configuration file was created.", Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.RED);

                snackbar.show();
            } else {
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
                        System.out.println("Štartovacia rýchlosť je: " + StartSpeed);
                        maxAltitudeTolerance = Integer.parseInt(prop.getProperty("Max_altitude_tolerance"));
                        IntervalOfSendingSMS = Integer.parseInt(prop.getProperty("Interval_of_sending_SMS"));
                        IntervalOfFacebookSharing = Integer.parseInt(prop.getProperty("Interval_of_Facebook_sharing"));
                        IntervalOfDataStore = Integer.parseInt(prop.getProperty("Interval_of_data_store"));
                        IntervalOfTakeFoto = Integer.parseInt(prop.getProperty("Interval_of_take_foto"));
                        ConfigFileOK = true;
                    } catch (NumberFormatException e) {
                        ConfigFileOK = false;
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
        else
            showGPSDisabledAlertToUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GPSService checkGPS = new GPSService(getApplicationContext());
        if(checkGPS.isGPSenable()) {
            System.out.println("Resumnul som");
            // Logs 'install' and 'app activate' App Events.
            AppEventsLogger.activateApp(this);
        }else{
            showGPSDisabledAlertToUser();
        }
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
    Boolean ConfigFileOK = false;
    Location lastLocation = null;

    public void Start(View v) throws CameraAccessException {
        if (ConfigFileOK){
            if (!Working) {
                if (!switchFly.isChecked()) {
                    if (basicSeting != null && basicSeting.getIntervalOfSending() > 0) {


                        final File exportDir;
                        final File exportFile;

                        if (basicSeting.getSave()) {
                            exportDir = new File(getApplicationContext().getExternalFilesDirs(null)[1] + "/" + basicSeting.getFileName());
                            if (!exportDir.exists()) {
                                exportDir.mkdirs();
                            }
                            exportFile = new File(exportDir, "path.csv");
                        } else {
                            exportDir = new File(getApplicationContext().getExternalFilesDirs(null)[1] + "/" + "default_dir");
                            if (!exportDir.exists()) {
                                exportDir.mkdirs();
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHMMSS");
                            String currentDateandTime = sdf.format(new Date());
                            exportFile = new File(exportDir, currentDateandTime + ".csv");

                        }


                        smsTask = new TimerTask() {
                            public void run() {
                                if (gpsService.getCurrentLocation())
                                    if (basicSeting.getSave()) {
                                        if (isSMSnetworkAvailable()) {
                                            String message = "Longitude: " + String.valueOf(gpsService.getCurrentLongitude()) + "\n" +
                                                    "Latitude " + String.valueOf(gpsService.getCurrentLatitude()) + "\n";
                                            if (smsSettings != null) {
                                                if (smsSettings.getAltitude() != null && smsSettings.getAltitude())
                                                    message = message + "Altitude: " + String.valueOf(gpsService.getCurrentAltitude()) + "\n";
                                                if (smsSettings.getBatteryStatus() != null && smsSettings.getBatteryStatus())
                                                    message = message + "Battery status: " + String.valueOf(batteryStatusService.getBatteryStatus(getApplicationContext())) + "\n";
                                                if (smsSettings.getDataNetwork() != null && smsSettings.getDataNetwork())
                                                    message = message + "Network: " + mobileNetworkService.getQualityOfInternetConection(getApplicationContext());
                                                smsService.sendSMS(smsSettings.getPhoneNumber(), message, getApplicationContext());
                                            }
                                        }
                                        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHMMSS");
                                        String currentDateandTime = sdf.format(new Date());
                                        //https://www.google.sk/maps/place//@49.1798864,17.5965172

                                        String log = currentDateandTime + "," + String.valueOf(gpsService.getCurrentLongitude()) + "," + String.valueOf(gpsService.getCurrentLatitude()) + "," + String.valueOf(batteryStatusService.getBatteryStatus(getApplicationContext())) + "," + String.valueOf(mobileNetworkService.getQualityOfInternetConection(getApplicationContext()));
                                        Writer output;
                                        try {
                                            output = new BufferedWriter(new FileWriter(exportFile, true));
                                            output.append(log + "\n");
                                            output.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        if (isSMSnetworkAvailable()) {
                                            String message = "Longitude: " + String.valueOf(gpsService.getCurrentLongitude()) + "\n" +
                                                    "Latitude " + String.valueOf(gpsService.getCurrentLatitude()) + "\n";
                                            if (smsSettings != null) {
                                                if (smsSettings.getAltitude() != null && smsSettings.getAltitude())
                                                    message = message + "Altitude: " + String.valueOf(gpsService.getCurrentLatitude()) + "\n";
                                                if (smsSettings.getBatteryStatus() != null && smsSettings.getBatteryStatus())
                                                    message = message + "Battery status: " + String.valueOf(batteryStatusService.getBatteryStatus(getApplicationContext())) + "\n";
                                                if (smsSettings.getDataNetwork() != null && smsSettings.getDataNetwork())
                                                    message = message + "Network: " + mobileNetworkService.getQualityOfInternetConection(getApplicationContext());
                                                smsService.sendSMS(smsSettings.getPhoneNumber(), message, getApplicationContext());
                                            }
                                        }
                                    }
                            }

                        };


                        facebookTask = new TimerTask() {
                            public void run() {
                                Data point = new Data();
                                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHMMSS");
                                String currentDateandTime = sdf.format(new Date());
                                point.setTime(currentDateandTime);
                                point.setLatitude(gpsService.getCurrentLatitude());
                                point.setLongitude(gpsService.getCurrentLongitude());
                                point.setAltitude(gpsService.getCurrentAltitude());
                                point.setBattery(batteryStatusService.getBatteryStatus(getApplicationContext()));
                                point.setNetworkConnection(mobileNetworkService.getQualityOfInternetConection(getApplicationContext()));

                                System.out.println(GPSexif.getFormattedLocationInDegree(gpsService.getCurrentLatitude(), gpsService.getCurrentLongitude()));
                                if (basicSeting.getSave()) {
                                    if (isOnline()) {
                                        String message2 = "Longitude:" + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";
                                        if (sharingSeting != null) {
                                            if (sharingSeting.getAltitude() != null && sharingSeting.getAltitude())
                                                message2 = message2 + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                                            if (sharingSeting.getBatteryStatus() != null && sharingSeting.getBatteryStatus())
                                                message2 = message2 + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                                            if (sharingSeting.getDataNetwork() != null && sharingSeting.getDataNetwork())
                                                message2 = message2 + "Network connection: " + point.getNetworkConnection() + "\n";

                                        }
                                        FacebookPUSHService facebookPUSHService = new FacebookPUSHService();
                                        String GoogleMapsURL = "https://www.google.sk/maps/place/" + GPSexif.getFormattedLocationInDegree(gpsService.getCurrentLatitude(), gpsService.getCurrentLongitude());
                                        if (lastLocation != null && lastLocation.distanceTo(gpsService.getLocation()) > DistanceForSendMaps)
                                            facebookPUSHService.push(true, GoogleMapsURL, message2, sharingSeting.getEventID(), getApplicationContext());
                                        else
                                            facebookPUSHService.push(false, GoogleMapsURL, message2, sharingSeting.getEventID(), getApplicationContext());
                                    }
                                    if (isSMSnetworkAvailable() && !smsSettings.getPhoneNumber().equals("")) {
                                        String message = "Longitude: " + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                                        if (smsSettings.getPhoneNumber() != null && !smsSettings.getPhoneNumber().equals("")) {
                                            if (smsSettings.getAltitude() != null && smsSettings.getAltitude())
                                                message = message + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                                            if (smsSettings.getBatteryStatus() != null && smsSettings.getBatteryStatus())
                                                message = message + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                                            if (smsSettings.getDataNetwork() != null && smsSettings.getDataNetwork())
                                                message = message + "Network: " + point.getNetworkConnection();
                                            smsService.sendSMS(smsSettings.getPhoneNumber(), message, getApplicationContext());
                                        }
                                    }
                                    String log = String.valueOf(point.getTime()) + "," + point.getLongitude() + "," + point.getLatitude() + "," + point.getAltitude() + "," + String.valueOf(point.getBattery()) + "," + point.getNetworkConnection();
                                    Writer output;
                                    try {
                                        output = new BufferedWriter(new FileWriter(exportFile, true));
                                        output.append(log + "\n");
                                        output.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (isOnline()) {
                                        String message2 = "Longitude:" + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                                        if (sharingSeting != null) {
                                            if (sharingSeting.getAltitude() != null && sharingSeting.getAltitude())
                                                message2 = message2 + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                                            if (sharingSeting.getBatteryStatus() != null && sharingSeting.getBatteryStatus())
                                                message2 = message2 + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                                            if (sharingSeting.getDataNetwork() != null && sharingSeting.getDataNetwork())
                                                message2 = message2 + "Network connection: " + point.getNetworkConnection() + "\n";

                                        }


                                        String GoogleMapsURL = "https://www.google.sk/maps/place/" + GPSexif.getFormattedLocationInDegree(gpsService.getCurrentLatitude(), gpsService.getCurrentLongitude());
                                        FacebookPUSHService facebookPUSHService = new FacebookPUSHService();
                                        if (lastLocation != null && lastLocation.distanceTo(gpsService.getLocation()) > 100)
                                            facebookPUSHService.push(true, GoogleMapsURL, message2, sharingSeting.getEventID(), getApplicationContext());
                                        else
                                            facebookPUSHService.push(false, GoogleMapsURL, message2, sharingSeting.getEventID(), getApplicationContext());
                                    }
                                    if (!smsSettings.getPhoneNumber().equals("") && isSMSnetworkAvailable()) {
                                        String message = "Longitude: " + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                                        if (smsSettings.getPhoneNumber() != null && !smsSettings.getPhoneNumber().equals("")) {
                                            if (smsSettings.getAltitude() != null && smsSettings.getAltitude())
                                                message = message + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                                            if (smsSettings.getBatteryStatus() != null && smsSettings.getBatteryStatus())
                                                message = message + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                                            if (smsSettings.getDataNetwork() != null && smsSettings.getDataNetwork())
                                                message = message + "Network: " + point.getNetworkConnection();
                                            smsService.sendSMS(smsSettings.getPhoneNumber(), message, getApplicationContext());
                                        }
                                    }
                                }
                                lastLocation = gpsService.getLocation();
                            }

                        };


                        facebookFotoTask = new TimerTask() {
                            public void run() {
                                if (lastLocation != null)
                                    System.out.println("VzdialenosŤ:" + lastLocation.distanceTo(gpsService.getLocation()));
                                if (lastLocation != null && lastLocation.distanceTo(gpsService.getLocation()) > 100) {
                                    String GoogleMapsURL = "https://www.google.sk/maps/place/" + GPSexif.getFormattedLocationInDegree(gpsService.getCurrentLatitude(), gpsService.getCurrentLongitude());
                                    FacebookPUSHService facebookPUSHService = new FacebookPUSHService();
                                    facebookPUSHService.push(true, GoogleMapsURL, "Actual position on the map.", sharingSeting.getEventID(), getApplicationContext());
                                }
                                Data point = new Data();
                                point.setLatitude(gpsService.getCurrentLatitude());
                                point.setLongitude(gpsService.getCurrentLongitude());
                                point.setAltitude(gpsService.getCurrentAltitude());
                                point.setBattery(batteryStatusService.getBatteryStatus(getApplicationContext()));
                                point.setNetworkConnection(mobileNetworkService.getQualityOfInternetConection(getApplicationContext()));


                                String message2 = "Longitude:" + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                                //message2 = message2 + ""

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

                                if (basicSeting.getSave()) {
                                    if (isOnline()) {
                                        point.setPhotoPath(fotoServiceService.takePicture(false, TimeoutOfFacebookSharing, SMStextForFacebookTimeout, message2, exportDir.getAbsolutePath(), sharingSeting.getEventID(), point.getLatitude(), point.getLongitude(), point.getAltitude(), facebookPhotoWidth, facebookPhotoHeight, facebookPhotoWidth, facebookPhotoHeight));
                                    } else {
                                        point.setPhotoPath(fotoServiceService.takePictureWithoutFacebook(exportDir.getAbsolutePath(), point.getLongitude(), point.getLatitude(), point.getAltitude(), BestPhotoWidth, BestPhotoHeight));
                                    }
                                    String log = String.valueOf(point.getTime()) + "," + point.getLongitude() + "," + point.getLatitude() + "," + point.getAltitude() + "," + String.valueOf(point.getBattery()) + "," + point.getNetworkConnection() + "," + point.getPhotoPath();
                                    Writer output;
                                    try {
                                        output = new BufferedWriter(new FileWriter(exportFile, true));
                                        output.append(log + "\n");
                                        output.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (isOnline()) {
                                        fotoServiceService.sharePictureWithoutSave(getCacheDir().getAbsolutePath(), sharingSeting.getEventID(), message2, point.getLatitude(), point.getLongitude(), point.getAltitude(), BestPhotoWidth, BestPhotoHeight, facebookPhotoWidth, facebookPhotoHeight);
                                    }
                                }

                                System.out.println("Som tutoka");

                                if (!smsSettings.getPhoneNumber().equals("")) {
                                    String message = "Longitude: " + String.valueOf(point.getLongitude()) + "\n" + "Latitude: " + String.valueOf(point.getLatitude()) + "\n";

                                    if (smsSettings.getPhoneNumber() != null && !smsSettings.getPhoneNumber().equals("")) {
                                        if (smsSettings.getAltitude() != null && smsSettings.getAltitude())
                                            message = message + "Altitude: " + String.valueOf(point.getAltitude()) + "\n";
                                        if (smsSettings.getBatteryStatus() != null && smsSettings.getBatteryStatus())
                                            message = message + "Battery status: " + String.valueOf(point.getBattery()) + "\n";
                                        if (smsSettings.getDataNetwork() != null && smsSettings.getDataNetwork())
                                            message = message + "Network: " + point.getNetworkConnection();
                                    }
                                    System.out.println("Poslal som SMS");
                                    smsService.sendSMS(smsSettings.getPhoneNumber(), message, getApplicationContext());
                                }
                                lastLocation = gpsService.getLocation();
                            }


                        };

                        if (!smsSettings.getPhoneNumber().equals("") && sharingSeting.getEventID().equals("")) {
                            Working = true;
                            //The application was launched. You can turn off the display.
                            //start SMSService task
                            Toast.makeText(MainActivity.this, "The application was launched. You can turn off the display.", Toast.LENGTH_SHORT).show();
                            System.out.println("Som v jednotke");
                            gpsService = new GPSService(MainActivity.this);
                            smsTimer = new Timer();
                            smsTimer.scheduleAtFixedRate(smsTask, 1000, basicSeting.getIntervalOfSending() * 1000);
                        } else if (!sharingSeting.getEventID().equals("") && sharingSeting.getPhoto()) {
                            Working = true;
                            Toast.makeText(MainActivity.this, "The application was launched. You can turn off the display.", Toast.LENGTH_SHORT).show();
                            System.out.println("Som v dvojke");
                            gpsService = new GPSService(MainActivity.this);
                            facebookFotoTimer = new Timer();
                            facebookFotoTimer.scheduleAtFixedRate(facebookFotoTask, 1000, basicSeting.getIntervalOfSending() * 1000);

                        } else if (!sharingSeting.getEventID().equals("") && !sharingSeting.getPhoto()) {
                            Working = true;
                            Toast.makeText(MainActivity.this, "The application was launched. You can turn off the display.", Toast.LENGTH_SHORT).show();
                            System.out.println("Som v trojke");
                            gpsService = new GPSService(MainActivity.this);
                            facebookTimer = new Timer();
                            facebookTimer.scheduleAtFixedRate(facebookTask, 1000, basicSeting.getIntervalOfSending() * 1000);
                        }

                        if (gpsService == null) {
                            Snackbar snackbar = Snackbar
                                    .make(layoutMain, "SMS and Sharing settings not selected.", Snackbar.LENGTH_LONG);
                            View sbView = snackbar.getView();
                            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(Color.RED);

                            snackbar.show();
                        }


                    } else {
                        Snackbar snackbar = Snackbar
                                .make(layoutMain, "Not selected basic settings.", Snackbar.LENGTH_LONG);
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.RED);

                        snackbar.show();
                    }
                } else {
                    Working = true;
                    gpsService = new GPSService(MainActivity.this);

                    flySMStimer = new Timer();
                    flyFacebookTimer = new Timer();
                    flyDataTimer = new Timer();
                    flyFotoTimer = new Timer();


                    Toast.makeText(MainActivity.this, "The application was launched. You can turn off the display.", Toast.LENGTH_SHORT).show();

                    final File exportDir = new File(getApplicationContext().getExternalFilesDirs(null)[1] + "/" + flyFacebookFile);
                    if (!exportDir.exists()) {
                        exportDir.mkdirs();
                    }
                    final File fileZaloha = new File(exportDir, fileNameFlyData);

                    flyDataTask = new TimerTask() {

                        public void run() {
                            Data point = new Data();
                            point.setLatitude(gpsService.getCurrentLatitude());
                            point.setLongitude(gpsService.getCurrentLongitude());
                            point.setAltitude(gpsService.getCurrentAltitude());
                            point.setBattery(batteryStatusService.getBatteryStatus(getApplicationContext()));
                            point.setNetworkConnection(mobileNetworkService.getQualityOfInternetConection(getApplicationContext()));
                            point.setSpeed(gpsService.getCurrentSpeed());
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                            String currentDateandTime = sdf.format(new Date());
                            point.setTime(currentDateandTime);

                            SMSnetwrok = isSMSnetworkAvailable();
                            InternetNetwork = isOnline();
                            AltitudeOK = isAltitudeOK(point.getAltitude());
                            if (!SMSnetwrok && !InternetNetwork && maxAltitude == 0)
                                maxAltitude = point.getAltitude();

                            String log = String.valueOf(point.getTime()) + "," + point.getLongitude() + "," + point.getLatitude() + "," + point.getAltitude() + "," + point.getSpeed() + "," + String.valueOf(point.getBattery()) + "," + point.getNetworkConnection() + "," + point.getPhotoPath();
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
                            if (!fotoServiceService.isLock()) {
                                Data point = new Data();
                                point.setLatitude(gpsService.getCurrentLatitude());
                                point.setLongitude(gpsService.getCurrentLongitude());
                                point.setAltitude(gpsService.getCurrentAltitude());
                                fotoServiceService.takePictureWithoutFacebook(exportDir.getAbsolutePath(), gpsService.getCurrentLatitude(), gpsService.getCurrentLongitude(), gpsService.getCurrentAltitude(), BestPhotoWidth, BestPhotoHeight);
                            }
                        }
                    };

                    flyFotoTimer.scheduleAtFixedRate(flyFotoTask, 1000, IntervalOfTakeFoto);
                    flyDataTimer.scheduleAtFixedRate(flyDataTask, 1000, IntervalOfDataStore);

                    Thread waitUntilStart = new Thread() {
                        @Override
                        public void run() {
                            try {
                                while (!isSpeedGood() || !isAltitudeForStartOK()) {
                                    actualSpeed = gpsService.getCurrentSpeed();
                                    Thread.sleep(1000);
                                }
                                StartFly(gpsService.getCurrentLongitude(), gpsService.getCurrentLatitude(), gpsService.getCurrentAltitude());

                            } catch (Exception e) {
                            }
                        }
                    };
                    waitUntilStart.start();
                }
            } else {
                Snackbar snackbar = Snackbar
                        .make(layoutMain, "The program already recorded data.", Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.RED);

                snackbar.show();
            }
    }else {
            Snackbar snackbar = Snackbar
                    .make(layoutMain, "Configuration files are not available", Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.RED);

            snackbar.show();
        }
    }

    public Boolean isSpeedGood(){
        if(actualSpeed > StartSpeed)
            return true;
        else
            return false;
    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is not enabled. Enabled location service to determine your location.")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        finish();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void sendInfoSMS(){
        //task.cancel();
        //timer.cancel();
        smsService.sendSMS(PhoneNumber, InitialSMStext, getApplicationContext());
    }




    public void StartFly(double GPSlongitude, double GPSlatitude, double GPSaltitude) {
        System.out.println("Odštartovali sme ! ! !");

        String StartMessage = InitialSMStext + String.valueOf(GPSlongitude) + ", " + String.valueOf(GPSlatitude) + ", " + String.valueOf(GPSaltitude);
        smsService.sendSMS(PhoneNumber, StartMessage, getApplicationContext());
        FacebookPUSHService push = new FacebookPUSHService();
        String GoogleMapsURL ="https://www.google.sk/maps/place/" + GPSexif.getFormattedLocationInDegree(gpsService.getCurrentLatitude(),gpsService.getCurrentLongitude());
        push.push(true,GoogleMapsURL,StartMessage, EventID, getApplicationContext());

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
                        point.setLatitude(gpsService.getCurrentLatitude());
                        point.setLongitude(gpsService.getCurrentLongitude());
                        point.setAltitude(gpsService.getCurrentAltitude());
                        point.setBattery(batteryStatusService.getBatteryStatus(getApplicationContext()));
                        point.setNetworkConnection(mobileNetworkService.getQualityOfInternetConection(getApplicationContext()));

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
                        if (!fotoServiceService.isLock()) {
                            fotoServiceService.takePicture(true, TimeoutOfFacebookSharing, SMStextForFacebookTimeout, message2, exportDir.getAbsolutePath(), EventID, point.getLatitude(), point.getLongitude(), point.getAltitude(), facebookPhotoWidth, facebookPhotoHeight, facebookPhotoWidth, facebookPhotoHeight);

                        }
                    }
                }
            }
        };
        flySMStimer = new Timer();

        flySMStask = new TimerTask() {
            public void run() {
                System.out.println("SMS sieť: " + SMSnetwrok + " Internet sieť: " + InternetNetwork);
                if ((SMSnetwrok && !InternetNetwork) || AltitudeOK) {
                    if (isSMSnetworkAvailable()) {
                        System.out.println("Poslal som SMS");
                        Data point = new Data();
                        point.setLatitude(gpsService.getCurrentLatitude());
                        point.setLongitude(gpsService.getCurrentLongitude());
                        point.setAltitude(gpsService.getCurrentAltitude());
                        point.setBattery(batteryStatusService.getBatteryStatus(getApplicationContext()));
                        point.setNetworkConnection(mobileNetworkService.getQualityOfInternetConection(getApplicationContext()));

                        String message2 = "";
                        message2 = message2 + "Zemepisna dlzka: " + String.valueOf(point.getLongitude()) +
                                "\n" + "Zemepisna sirka: " + String.valueOf(point.getLatitude()) +
                                "\n" + "Zemepisna vyska: " + String.valueOf(point.getAltitude()) +
                                "\n" + "Stav baterie: " + String.valueOf(point.getBattery());
                        smsService.sendSMS(PhoneNumber,message2,getApplicationContext());
                        //fotoServiceService.takePicture(true,TimeoutOfFacebookSharing, SMStextForFacebookTimeout, message2, exportDir.getAbsolutePath(), EventID, point.getLatitude(), point.getLongitude(), point.getAltitude(), facebookPhotoWidth, facebookPhotoHeight, facebookPhotoWidth, facebookPhotoHeight);
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

    public boolean isAltitudeForStartOK(){
        return true;
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

        if(Working) {
            Boolean Ended = false;
            if (facebookTask != null) {
                facebookTask.cancel();
                Ended = true;
            }
            if (facebookFotoTimer != null) {
                facebookFotoTimer.cancel();
                Ended = true;
            }

            if (facebookFotoTask != null) {
                facebookFotoTask.cancel();
                Ended = true;
            }

            if (facebookFotoTimer != null) {
                facebookFotoTimer.cancel();
                Ended = true;
            }
            if (smsTask != null) {
                smsTask.cancel();
                Ended = true;
            }
            if (smsTimer != null) {
                smsTimer.cancel();
                Ended = true;
            }
            if (flySMStask != null) {
                flySMStask.cancel();
                Ended = true;
            }
            if (flySMStimer != null) {
                flySMStimer.cancel();
                Ended = true;
            }
            if (flyDataTask != null) {
                flyDataTask.cancel();
                Ended = true;
            }
            if (flyFacebookTimer != null) {
                Ended = true;
                flyFacebookTimer.cancel();
            }
            if (flyFotoTask != null) {
                flyFotoTask.cancel();
                Ended = true;
            }
            if (flyFotoTimer != null) {
                flyFotoTimer.cancel();
                Ended = true;
            }
            if (gpsService != null) {
                gpsService.stopUsingGPS();
                gpsService = null;
                Ended = true;
            }
            if (Ended)
                Toast.makeText(MainActivity.this, "An application terminates. You can see the results.", Toast.LENGTH_SHORT).show();
        }
        Working = false;

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
