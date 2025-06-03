package com.project.chatapp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String getTimeAgo(String timestamp) {
        try {
            long timeMillis = Long.parseLong(timestamp);
            Date past = new Date(timeMillis);
            Date now = new Date();

            long diffInMillis = now.getTime() - past.getTime();

            // If the difference is negative (message from future due to time mismatch)
            if (diffInMillis < 0) {
                return formatTime(past);
            }

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (seconds < 60) {
                return "just now";
            } else if (minutes < 60) {
                return minutes + " min";
            } else if (hours < 24) {
                return hours + " hr";
            } else if (days < 7) {
                return days + " day" + (days > 1 ? "s" : "");
            } else {
                return formatTime(past);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return formatCurrentTime();
        }
    }

    private static String formatTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault());
        return sdf.format(date);
    }

    private static String formatCurrentTime() {
        return formatTime(new Date());
    }
}
