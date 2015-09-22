package se.johanmagnusson.android.spotifystreamer;

/**
 * Created by Johan on 2015-09-20.
 */
public class Utility {

    public static String formatTrackTime(int timeSec) {
        int sec = timeSec % 60;
        int min = (timeSec / 60) % 60;

        return String.format("%02d:%02d", min, sec);
    }
}
