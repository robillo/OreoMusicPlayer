package com.robillo.dancingplayer.utils;

import java.util.Locale;

public class ApplicationUtils {

    public ApplicationUtils() {
    }

    public String formatStringOutOfSeconds(int duration) {
        int mins = duration/60;
        int seconds = duration%60;

        String lhs, rhs;

        if(mins < 10) lhs = "0" + String.valueOf(mins);
        else lhs = String.valueOf(mins);

        if(seconds < 10) rhs = "0" + String.valueOf(seconds);
        else rhs = String.valueOf(seconds);

        return lhs + " : " + rhs;
    }

    public String formatSizeKBtoMB(float size) {

        return String.format("%s MB", String.format(Locale.ENGLISH, "%.2f", size/1024));
    }
}
