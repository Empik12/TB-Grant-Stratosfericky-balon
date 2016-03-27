package fiit.baranek.tomas.gpssky.SettingsDatabase;

/**
 * Created by TomasPC on 25.3.2016.
 */
public class Settings {


    private int ID;
    //basic settings
    private int IntervalOfSending = 0;
    private Boolean Save = false;
    private String FileName = "";
    //sharing settings
    private Boolean SharingAltitude = false;
    private Boolean SharingPhoto = false;
    private Boolean SharingBatteryStatus = false;
    private Boolean SharingDataNetwork = false;
    //sms setting
    private String PhoneNumber = "";
    private Boolean SMSAltitude = false;
    private Boolean SMSBatteryStatus = false;
    private Boolean SMSDataNetwork = false;

    public Settings(int ID, int intervalOfSending, Boolean save, String fileName, Boolean sharingAltitude, Boolean sharingPhoto, Boolean sharingBatteryStatus, Boolean sharingDataNetwork, String phoneNumber, Boolean SMSAltitude, Boolean SMSBatteryStatus, Boolean SMSDataNetwork) {
        this.ID = ID;
        IntervalOfSending = intervalOfSending;
        Save = save;
        FileName = fileName;
        SharingAltitude = sharingAltitude;
        SharingPhoto = sharingPhoto;
        SharingBatteryStatus = sharingBatteryStatus;
        SharingDataNetwork = sharingDataNetwork;
        PhoneNumber = phoneNumber;
        this.SMSAltitude = SMSAltitude;
        this.SMSBatteryStatus = SMSBatteryStatus;
        this.SMSDataNetwork = SMSDataNetwork;
    }

    public Settings() {
    }


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getIntervalOfSending() {
        return IntervalOfSending;
    }

    public void setIntervalOfSending(int intervalOfSending) {
        IntervalOfSending = intervalOfSending;
    }

    public Boolean getSave() {
        return Save;
    }

    public void setSave(Boolean save) {
        Save = save;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public Boolean getSharingAltitude() {
        return SharingAltitude;
    }

    public void setSharingAltitude(Boolean sharingAltitude) {
        SharingAltitude = sharingAltitude;
    }

    public Boolean getSharingPhoto() {
        return SharingPhoto;
    }

    public void setSharingPhoto(Boolean sharingPhoto) {
        SharingPhoto = sharingPhoto;
    }

    public Boolean getSharingBatteryStatus() {
        return SharingBatteryStatus;
    }

    public void setSharingBatteryStatus(Boolean sharingBatteryStatus) {
        SharingBatteryStatus = sharingBatteryStatus;
    }

    public Boolean getSharingDataNetwork() {
        return SharingDataNetwork;
    }

    public void setSharingDataNetwork(Boolean sharingDataNetwork) {
        SharingDataNetwork = sharingDataNetwork;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public Boolean getSMSAltitude() {
        return SMSAltitude;
    }

    public void setSMSAltitude(Boolean SMSAltitude) {
        this.SMSAltitude = SMSAltitude;
    }

    public Boolean getSMSBatteryStatus() {
        return SMSBatteryStatus;
    }

    public void setSMSBatteryStatus(Boolean SMSBatteryStatus) {
        this.SMSBatteryStatus = SMSBatteryStatus;
    }

    public Boolean getSMSDataNetwork() {
        return SMSDataNetwork;
    }

    public void setSMSDataNetwork(Boolean SMSDataNetwork) {
        this.SMSDataNetwork = SMSDataNetwork;
    }


}
