package fiit.baranek.tomas.gpssky.Models;

/**
 * This method has been modified by us
 * Source: https://techspread.wordpress.com/2014/04/07/write-read-geotag-jpegs-exif-data-in-android-gps/
 */
public class GPSexif {

    private static StringBuilder sb = new StringBuilder(20);

    public static String latitudeRef(final double latitude) {
        return latitude < 0.0d ? "S" : "N";
    }

    public static String longitudeRef(final double longitude) {
        return longitude < 0.0d ? "W" : "E";
    }

    public static final String convert(double latitude) {
        latitude = Math.abs(latitude);
        final int degree = (int)latitude;
        latitude *= 60;
        latitude -= degree * 60.0d;
        final int minute = (int)latitude;
        latitude *= 60;
        latitude -= minute * 60.0d;
        final int second = (int)(latitude * 1000.0d);
        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000,");
        return sb.toString();
    }

    public static String getFormattedLocationInDegree(double latitude, double longitude) {
        try {
            int latSeconds = (int) Math.round(latitude * 3600);
            int latDegrees = latSeconds / 3600;
            latSeconds = Math.abs(latSeconds % 3600);
            int latMinutes = latSeconds / 60;
            latSeconds %= 60;

            int longSeconds = (int) Math.round(longitude * 3600);
            int longDegrees = longSeconds / 3600;
            longSeconds = Math.abs(longSeconds % 3600);
            int longMinutes = longSeconds / 60;
            longSeconds %= 60;
            String latDegree = latDegrees >= 0 ? "N" : "S";
            String lonDegrees = latDegrees >= 0 ? "E" : "W";

            return  Math.abs(latDegrees) + "°" + latMinutes + "'" + latSeconds
                    + "\"" + latDegree +" "+ Math.abs(longDegrees) + "°" + longMinutes
                    + "'" + longSeconds + "\"" + lonDegrees;
        } catch (Exception e) {

            return ""+ String.format("%8.5f", latitude) + "+"
                    + String.format("%8.5f", longitude) ;
        }

    }
}

