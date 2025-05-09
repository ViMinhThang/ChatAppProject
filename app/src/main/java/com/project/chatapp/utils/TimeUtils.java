package com.project.chatapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String getTimeAgo(String isoTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date past = sdf.parse(isoTime);
            Date now = new Date();

            long diffInMillis = now.getTime() - past.getTime();

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (seconds < 60) return "just now";
            else if (minutes < 60) return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            else if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            else return days + " day" + (days > 1 ? "s" : "") + " ago";

        } catch (ParseException e) {
            e.printStackTrace();
            return "unknown time";
        }
    }
}
