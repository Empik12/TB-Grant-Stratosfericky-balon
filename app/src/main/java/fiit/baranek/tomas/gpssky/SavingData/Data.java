package fiit.baranek.tomas.gpssky.SavingData;

/**
 * Created by TomasPC on 30.3.2016.
 */
public class Data {

    private int id;
    private double Longitude;
    private double Latitude;
    private double Altitude;
    private double Battery;
    private String NetworkConnection;
    private String PhotoPath;

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    private String Time;

    public Data() {
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getAltitude() {
        return Altitude;
    }

    public void setAltitude(double altitude) {
        Altitude = altitude;
    }

    public double getBattery() {
        return Battery;
    }

    public void setBattery(double battery) {
        Battery = battery;
    }

    public String getNetworkConnection() {
        return NetworkConnection;
    }

    public void setNetworkConnection(String networkConnection) {
        NetworkConnection = networkConnection;
    }

    public String getPhotoPath() {
        return PhotoPath;
    }

    public void setPhotoPath(String photoPath) {
        PhotoPath = photoPath;
    }

    public Data(String time, int id, double longitude, double latitude, double altitude, double battery, String networkConnection, String photoPath) {
        Time = time;
        this.id = id;
        Longitude = longitude;
        Latitude = latitude;
        Altitude = altitude;
        Battery = battery;
        NetworkConnection = networkConnection;
        PhotoPath = photoPath;
    }
}
