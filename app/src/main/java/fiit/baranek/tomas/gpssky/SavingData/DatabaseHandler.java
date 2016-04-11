package fiit.baranek.tomas.gpssky.SavingData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TomasPC on 30.3.2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_DATA = "data";


    /*
     private int id;
    private double Longitude;
    private double Latitude;
    private double Altitude;
    private String Provider;
    private double Battery;
    private String NetworkConnection;
    private String PhotoPath;
     */
    private static final String KEY_ID = "id";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_NETWORK_CONNECTION = "network_connection";
    private static final String KEY_PHOTO_PATH = "photo_path";
    private static final String KEY_TIME = "time";



    public DatabaseHandler(Context context,String DatabaseName){
        super(context,DatabaseName,null,DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_DATA + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT," + KEY_ALTITUDE + " TEXT,"
                + KEY_BATTERY + " REAL,"
                + KEY_NETWORK_CONNECTION + " TEXT," + KEY_PHOTO_PATH + " TEXT,"
                + KEY_TIME + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_DATA);
        onCreate(db);
    }

    public void addData(Data data){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, String.valueOf(data.getLatitude()));
        values.put(KEY_LONGITUDE, String.valueOf(data.getLongitude()));
        System.out.println("Toto tam ide: " + String.valueOf(data.getLongitude()));
        values.put(KEY_ALTITUDE, String.valueOf(data.getAltitude()));
        values.put(KEY_BATTERY, data.getBattery());
        values.put(KEY_NETWORK_CONNECTION, data.getNetworkConnection());
        values.put(KEY_PHOTO_PATH, data.getPhotoPath());
        values.put(KEY_TIME, data.getTime());
        db.insert(TABLE_DATA, null, values);
        db.close();
    }




    public List<Data> getAllData(){
        List<Data> DataList = new ArrayList<Data>();

        String selectQuery = "SELECT * FROM " + TABLE_DATA;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Data data = new Data();
                data.setId(Integer.parseInt(cursor.getString(0)));
                data.setLatitude(Double.parseDouble(cursor.getString(1)));
                data.setLongitude(Double.parseDouble(cursor.getString(2)));
                System.out.println("Tutotka longitude:" + cursor.getString(2));
                data.setAltitude(Double.parseDouble(cursor.getString(3)));
                data.setBattery(Double.parseDouble(cursor.getString(4)));
                data.setNetworkConnection(cursor.getString(5));
                data.setPhotoPath(cursor.getString(6));
                data.setTime(cursor.getString(7));
                // Adding contact to list
                DataList.add(data);
            } while (cursor.moveToNext());
        }

        // return contact list
        return DataList;
    }

    public Cursor getCursor(){
        String selectQuery = "SELECT * FROM " + TABLE_DATA;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor;
    }

    public Integer getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_DATA;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}
