package org.monxef.mpunishments.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

    public static Date parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile("(\\d+)([smhdwMy])");
        Matcher matcher = pattern.matcher(duration);

        Calendar calendar = Calendar.getInstance();
        boolean matched = false;

        while (matcher.find()) {
            matched = true;
            int amount = Integer.parseInt(matcher.group(1));
            char unit = matcher.group(2).charAt(0);

            switch (unit) {
                case 's':
                    calendar.add(Calendar.SECOND, amount);
                    break;
                case 'm':
                    calendar.add(Calendar.MINUTE, amount);
                    break;
                case 'h':
                    calendar.add(Calendar.HOUR_OF_DAY, amount);
                    break;
                case 'd':
                    calendar.add(Calendar.DAY_OF_MONTH, amount);
                    break;
                case 'w':
                    calendar.add(Calendar.WEEK_OF_YEAR, amount);
                    break;
                case 'M':
                    calendar.add(Calendar.MONTH, amount);
                    break;
                case 'y':
                    calendar.add(Calendar.YEAR, amount);
                    break;
            }
        }

        if (!matched) {
            return null;
        }

        return calendar.getTime();
    }
}