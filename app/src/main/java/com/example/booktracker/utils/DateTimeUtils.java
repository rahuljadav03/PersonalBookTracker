package com.example.booktracker.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

    public static String formatIst(long epochMillis) {
        if (epochMillis <= 0) {
            return "-";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String formatted = sdf.format(new Date(epochMillis));
        return formatted.replace("am", "AM").replace("pm", "PM");
    }
}
