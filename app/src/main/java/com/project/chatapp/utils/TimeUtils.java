package com.project.chatapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String getTimeAgo(String timestamp) {
        try {
            Date past;

            // Nếu timestamp là chuỗi số dạng millis (chỉ chứa chữ số)
            if (timestamp.matches("\\d+")) {
                long timeMillis = Long.parseLong(timestamp);
                past = new Date(timeMillis);
            } else {
                // Nếu timestamp là chuỗi ISO 8601
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                past = isoFormat.parse(timestamp);
            }

            if (past == null) return formatCurrentTime();

            Date now = new Date();
            long diffInMillis = now.getTime() - past.getTime();

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
