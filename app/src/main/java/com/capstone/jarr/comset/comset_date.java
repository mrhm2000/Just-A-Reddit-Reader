package com.capstone.jarr.comset;

/**
 * Created on 09/09/2018.
 */
public class comset_date {
    public static String convert(long millis) {
        long difference = (System.currentTimeMillis() / 1000) - (millis / 1000);

        long minutes = difference / 60;
        if (minutes < 60) {
            return (int) minutes + "m";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return (int) hours + "h";
        }

        long days = hours / 24;
        if (days < 30) {
            return (int) days + "d";
        }

        long months = days / 30;
        if (months < 12) {
            return (int) months + "mo";
        }

        return (int) months / 12 + "y";

    }
}
