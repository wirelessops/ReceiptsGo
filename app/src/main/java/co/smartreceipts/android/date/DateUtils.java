package co.smartreceipts.android.date;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.text.format.Time;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {

    public static final String DEFAULT_SEPARATOR = "/";
    private static String separator;

    private DateUtils() {
        throw new RuntimeException("This class uses static calls only. It cannot be instantianted");
    }

    public static String getDateSeparator(Context context) {
        if (separator == null) {
            String dateString = DateFormat.getDateFormat(context).format(new java.util.Date());
            Matcher matcher = Pattern.compile("[^\\w]").matcher(dateString);
            if (!matcher.find()) {
                separator = DEFAULT_SEPARATOR;
            }
            else {
                separator = matcher.group(0);
            }
        }
        return separator;
    }

    public static String getCurrentDateAsYYYY_MM_DDString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    public static boolean isToday(@NonNull Date date) {

        // Build a calendar for the start of today
        final Time now = new Time();
        now.setToNow();
        final Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(now.toMillis(false));
        startCalendar.setTimeZone(TimeZone.getDefault());
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        // Build a calendar for the end date
        final Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(now.toMillis(false));
        endCalendar.setTimeZone(TimeZone.getDefault());
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endCalendar.set(Calendar.MILLISECOND, 999);

        // Set the timers
        final long startTime = startCalendar.getTimeInMillis();
        final long endTime = endCalendar.getTimeInMillis();
        final long testTime = date.getTime();

        return startTime <= testTime && testTime <= endTime;
    }

    public static long getDays(@NonNull Date date) {
        return TimeUnit.MILLISECONDS.toDays(date.getTime());
    }
}
