package it.core.staffpanel.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhdwmo]+)");

    public static long parseTime(String input) {
        if (input == null || input.isEmpty()) {
            return -1;
        }
        
        Matcher matcher = TIME_PATTERN.matcher(input.toLowerCase());
        long totalMillis = 0;
        
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            switch (unit) {
                case "s":
                    totalMillis += value * 1000L;
                    break;
                case "m":
                    totalMillis += value * 60 * 1000L;
                    break;
                case "h":
                    totalMillis += value * 60 * 60 * 1000L;
                    break;
                case "d":
                    totalMillis += value * 24 * 60 * 60 * 1000L;
                    break;
                case "w":
                    totalMillis += value * 7 * 24 * 60 * 60 * 1000L;
                    break;
                case "mo":
                    totalMillis += value * 30L * 24 * 60 * 60 * 1000L;
                    break;
                default:
                    return -1;
            }
        }
        
        return totalMillis > 0 ? totalMillis : -1;
    }

    public static String formatDuration(long millis) {
        if (millis <= 0) {
            return "0s";
        }
        
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        
        StringBuilder result = new StringBuilder();
        
        if (months > 0) {
            result.append(months).append("mo ");
            days %= 30;
        }
        
        if (weeks > 0 && months == 0) {
            result.append(weeks).append("w ");
            days %= 7;
        }
        
        if (days > 0 && months == 0) {
            result.append(days).append("d ");
        }
        
        hours %= 24;
        if (hours > 0 && weeks == 0 && months == 0) {
            result.append(hours).append("h ");
        }
        
        minutes %= 60;
        if (minutes > 0 && days == 0 && weeks == 0 && months == 0) {
            result.append(minutes).append("m ");
        }
        
        seconds %= 60;
        if (seconds > 0 && hours == 0 && days == 0 && weeks == 0 && months == 0) {
            result.append(seconds).append("s");
        }
        
        return result.toString().trim();
    }

    public static String formatDurationLong(long millis) {
        if (millis <= 0) {
            return "0 secondi";
        }
        
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append(days == 1 ? " giorno" : " giorni");
            hours %= 24;
            if (hours > 0) {
                result.append(", ").append(hours).append(hours == 1 ? " ora" : " ore");
            }
        } else if (hours > 0) {
            result.append(hours).append(hours == 1 ? " ora" : " ore");
            minutes %= 60;
            if (minutes > 0) {
                result.append(", ").append(minutes).append(minutes == 1 ? " minuto" : " minuti");
            }
        } else if (minutes > 0) {
            result.append(minutes).append(minutes == 1 ? " minuto" : " minuti");
            seconds %= 60;
            if (seconds > 0) {
                result.append(", ").append(seconds).append(seconds == 1 ? " secondo" : " secondi");
            }
        } else {
            result.append(seconds).append(seconds == 1 ? " secondo" : " secondi");
        }
        
        return result.toString();
    }

    public static boolean isValidTimeFormat(String input) {
        return parseTime(input) > 0;
    }
}
