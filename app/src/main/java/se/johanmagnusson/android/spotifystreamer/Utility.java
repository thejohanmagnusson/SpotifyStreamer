package se.johanmagnusson.android.spotifystreamer;

import android.content.Intent;

import se.johanmagnusson.android.spotifystreamer.Models.TrackItem;

public class Utility {

    public static String formatTrackTime(int timeSec) {
        int sec = timeSec % 60;
        int min = (timeSec / 60) % 60;

        return String.format("%02d:%02d", min, sec);
    }

    public static Intent createTrackShareIntent(TrackItem track){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, track.shareUrl);
        intent.setType("text/plain");

        return intent;
    }
}
