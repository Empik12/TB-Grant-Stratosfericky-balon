package fiit.baranek.tomas.gpssky.SettingsDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

/**
 * Created by TomasPC on 25.3.2016.
 */
public class DBHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "tomasko";
    // Contacts table name
    private static final String TABLE_SHOPS = "settingDataAPP2";
    // Shops Table Columns names
    /*
    private int ID;
    //basic settings
    private Boolean IntervalOfSending;
    private Boolean Save;
    private String FileName;
    //sharing settings
    private Boolean SharingAltitude;
    private Boolean SharingPhoto;
    private Boolean SharingBatteryStatus;
    private Boolean SharingDataNetwork;
    //sms setting
    private String PhoneNumber;
    private Boolean SMSAltitude;
    private Boolean SMSBatteryStatus;
    private Boolean SMSDataNetwork;
     */
    private static final String KEY_ID = "id";
    private static final String KEY_INTERVAL_OF_SENDING = "interval_of_sending";
    private static final String KEY_SAVE = "save";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_SHARING_ALTITUDE = "sharing_altitude";
    private static final String KEY_SHARING_PHOTO = "sharing_photo";
    private static final String KEY_SHARING_BATTERY_STATUS = "sharing_battery_status";
    private static final String KEY_SHARING_DATA_NETWORK = "sharing_data_network";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_SMS_ALTITUDE = "sms_altitude";
    private static final String KEY_SMS_BATTERY_STATUS = "sms_battery_status";
    private static final String KEY_SMS_DATA_NETWORK = "sms_data_network";

    public DBHandler(Context context) {
        super(context,Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS)
                + File.separator+ DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        File dbfile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS)
                + File.separator+ DATABASE_NAME);
        SQLiteDatabase  dbs = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SHOPS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_INTERVAL_OF_SENDING + " INTEGER,"
                + KEY_SAVE + " INTEGER," + KEY_FILE_NAME + " TEXT," + KEY_SHARING_ALTITUDE + " INTEGER,"
                + KEY_SHARING_PHOTO + " INTEGER,"  + KEY_SHARING_BATTERY_STATUS+ " INTEGER,"
                + KEY_SHARING_DATA_NETWORK + KEY_PHONE_NUMBER + " TEX,T" + KEY_SMS_ALTITUDE + " INTEGER,"
                + KEY_SMS_BATTERY_STATUS+ " INTEGER," + KEY_SMS_DATA_NETWORK + " INTEGER" +  ")";
        dbs.execSQL(CREATE_CONTACTS_TABLE);
        System.out.println("AHOJKAAAAAAAAAJ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOPS);
        // Creating tables again
        onCreate(db);
    }
    public long insert(Settings setting){
        File dbfile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS)
                + File.separator+ DATABASE_NAME);
        SQLiteDatabase  db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
                ContentValues values = new ContentValues();

        values.put(KEY_INTERVAL_OF_SENDING, setting.getIntervalOfSending());
        if(setting.getSave())
            values.put(KEY_SAVE, 1);
        else
            values.put(KEY_SAVE, 0);
        values.put(KEY_FILE_NAME, setting.getFileName());
        if(setting.getSharingAltitude())
            values.put(KEY_SHARING_ALTITUDE, 1);
        else
            values.put(KEY_SHARING_ALTITUDE, 0);
        if(setting.getSharingPhoto())
            values.put(KEY_SHARING_PHOTO, 1);
        else
            values.put(KEY_SHARING_PHOTO, 0);
        if(setting.getSharingDataNetwork())
            values.put(KEY_SHARING_DATA_NETWORK, 1);
        else
            values.put(KEY_SHARING_DATA_NETWORK, 0);
        if(setting.getSharingBatteryStatus())
            values.put(KEY_SHARING_BATTERY_STATUS, 1);
        else
            values.put(KEY_SHARING_BATTERY_STATUS, 0);
        values.put(KEY_PHONE_NUMBER, setting.getPhoneNumber());
        if(setting.getSMSAltitude())
            values.put(KEY_SMS_ALTITUDE, 1);
        else
            values.put(KEY_SMS_ALTITUDE, 0);
        if(setting.getSMSBatteryStatus())
            values.put(KEY_SMS_BATTERY_STATUS, 1);
        else
            values.put(KEY_SMS_BATTERY_STATUS, 0);
       if(setting.getSMSDataNetwork())
            values.put(KEY_SMS_DATA_NETWORK, 1);
        else
            values.put(KEY_SMS_DATA_NETWORK, 0);


        long id = db.insert(TABLE_SHOPS, null, values);
        db.close();
        return id;
    }


   /* public int update(Settings seting) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TODO, todo.getNote());
        values.put(KEY_STATUS, todo.getStatus());

        // updating row
        return db.update(TABLE_TODO, values, KEY_ID + " = ?",
                new String[] { String.valueOf(todo.getId()) });
    }*/

}
