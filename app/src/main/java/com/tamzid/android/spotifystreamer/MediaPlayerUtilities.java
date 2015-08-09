package com.tamzid.android.spotifystreamer;

import java.util.concurrent.TimeUnit;

/**
 * Created by Tamzid on 09/08/2015.
 */
public class MediaPlayerUtilities {

    public static String formatMillisToString(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }

}
