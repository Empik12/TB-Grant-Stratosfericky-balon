package fiit.baranek.tomas.gpssky.Settings;

/**
 * Created by Tomáš Baránek
 * This is model for SMSSetting
 *
 */
public class SMSSettings {


    private String PhoneNumber = "";
    private Boolean Altitude = false;
    private Boolean BatteryStatus = false;
    private Boolean DataNetwork = false;

    public SMSSettings() {
    }

    public SMSSettings(String phoneNumber, Boolean altitude, Boolean batteryStatus, Boolean dataNetwork) {

        PhoneNumber = phoneNumber;
        Altitude = altitude;
        BatteryStatus = batteryStatus;
        DataNetwork = dataNetwork;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public Boolean getAltitude() {
        return Altitude;
    }

    public void setAltitude(Boolean altitude) {
        Altitude = altitude;
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
