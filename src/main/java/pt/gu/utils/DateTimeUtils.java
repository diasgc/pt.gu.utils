package pt.gu.utils;

import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

    private static final String TAG = DateTimeUtils.class.getSimpleName();

    public static String dateToRFC3339(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
                .format(d)
                .replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2");
    }

    public static long parseDate(String date, String format, long resultIfError){
        if (date != null && format != null) {
            final SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
            return parseDate(date, sdf, resultIfError);
        }
        return resultIfError;
    }

    /**
     * Calculates Estimated Time of Arrival
     * @param startTime System.currentTimeMillis() when started
     * @param duration Duration of the event
     * @param time current progress time in millis (startTime =0, endTime = duration)
     * @return Estimated Time of Arrival, or 0 if not positive time
     */
    public static long eta(long startTime, long duration, long time){
        // int progress = (int) (time * 100 / duration);
        // long eta = System.currentTimeMillis() - startTime;
        // eta = eta * 100 / progress - eta;
        return time > 0 ? (System.currentTimeMillis() - startTime) * (duration - time) / time : 0;
    }

    public static long parseDate(@Nullable String date, @Nullable SimpleDateFormat sdf, long resultIfError){
        if (date != null && sdf != null) {
            try {
                final Date d = sdf.parse(date);
                return d == null ? resultIfError : d.getTime();
            } catch (Exception e) {
                Log.e(TAG,e.toString());
            }
        }
        return resultIfError;
    }

    /**
     *
     * @param tz TimeZone, may be null (default timezone)
     * @param f  Set by this order: Year, Month, Day, Hour, Minutes, Seconds and Milliseconds
     *           Further arguments are ignored
     * @return   Java long TimeInMillis
     */
    public static long getMillis(@Nullable TimeZone tz, int... f) {
        Calendar c = tz == null ? Calendar.getInstance() : Calendar.getInstance(tz);
        if (f != null && f.length > 0) {
            switch (f.length) {
                case 1:
                    c.set(f[0], 1, 1);
                    break;
                case 2:
                    c.set(f[0], f[1], 1);
                    break;
                case 3:
                    c.set(f[0], f[1], f[2]);
                    break;
                case 4:
                    c.set(f[0], f[1], f[2], f[3], 0, 0);
                    break;
                case 5:
                    c.set(f[0], f[1], f[2], f[3], f[4], 0);
                    break;
                case 6:
                    c.set(f[0], f[1], f[2], f[3], f[4], f[5]);
                    break;
                default:
                    c.set(f[0], f[1], f[2], f[3], f[4], f[5]);
                    c.set(Calendar.MILLISECOND,f[6]);
            }
        }
        return c.getTimeInMillis();
    }
}
