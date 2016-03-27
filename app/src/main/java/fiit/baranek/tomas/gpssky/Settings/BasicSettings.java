package fiit.baranek.tomas.gpssky.Settings;

import java.io.Serializable;

/**
 * Created by TomasPC on 26.3.2016.
 */
public class BasicSettings implements Serializable {

    private int IntervalOfSending = 0;
    private Boolean Save = false;
    private String FileName = "";

    public BasicSettings(int intervalOfSending, Boolean save, String fileName) {
        IntervalOfSending = intervalOfSending;
        Save = save;
        FileName = fileName;
    }

    public BasicSettings() {
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

}
