package com.tamzid.android.spotifystreamer;

import android.util.Log;

/** Handle logs to logcat if debug mode is on */
public class Debug {
    public static final boolean DEBUG_MODE = true;

    public static void logD(String logTag, String logMessage) {
        if (DEBUG_MODE) {
            Log.d(logTag, logMessage);
        }
    }

}
