package fiit.baranek.tomas.gpssky.Settings;

/**
 * Created by TomasPC on 26.3.2016.
 */
public class SharingSettings {

    private String EventID = "";
    private Boolean Altitude = false;
    private Boolean Photo = false;
    private Boolean BatteryStatus = false;

    public SharingSettings(String eventID, Boolean altitude, Boolean photo, Boolean batteryStatus, Boolean dataNetwork) {
        EventID = eventID;
        Altitude = altitude;
        Photo = photo;
        BatteryStatus = batteryStatus;
        DataNetwork = dataNetwork;
    }

    private Boolean DataNetwork;

    public SharingSettings() {
    }


    public String getEventID() {
        return EventID;
    }

    public void setEventID(String eventID) {
        EventID = eventID;
    }

    public Boolean getAltitude() {
        return Altitude;
    }

    public void setAltitude(Boolean altitude) {
        Altitude = altitude;
    }

    public Boolean getPhoto() {
        return Photo;
    }

    public void setPhoto(Boolean photo) {
        Photo = photo;
    }

    public Boolean getBatteryStatus() {
        return BatteryStatus;
    }

    public void setBatteryStatus(Boolean batteryStatus) {
        BatteryStatus = batteryStatus;
    }

    public Boolean getDataNetwork() {
        return DataNetwork;
    }

    public void setDataNetwork(Boolean dataNetwork) {
        DataNetwork = dataNetwork;
    }


}
