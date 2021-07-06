package pt.gu.utils;

import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@SuppressWarnings("unused")
public class DateTimeUtils {

    private static final String TAG = DateTimeUtils.class.getSimpleName();
    private static final boolean DBG = false;

    private static final long LIMITDATE = DateTimeUtils.dateTimeInMillis(null,1980,1,1);
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

    public static String formatDate(long date) {
        //final long diff = new Date().getTime() - date;
        //if (diff > DateUtils.WEEK_IN_MILLIS)
        //    return formatDate(DateUtils.FORMAT_ABBREV_MONTH,date);
        return formatDate(DateUtils.FORMAT_ABBREV_RELATIVE,date);
    }

    public static String formatDate(int format, long date) {
        if (date <= LIMITDATE)
            return String.format(Locale.US,"(0x%04X)",date);
        return DateUtils.getRelativeTimeSpanString(
                date, System.currentTimeMillis(),
                DateUtils.HOUR_IN_MILLIS, format)
                .toString().toLowerCase();
    }

    /**
     * @param timeZone TimeZone, may be null (default timezone)
     * @param fields sequecially:
     *               year(>1900)
     *               month (1-12)
     *               day (1-31)
     *               hour (0-23)
     *               minutes (0-59)
     *               seconds (0-59)
     *               milliseconds (0-999)
     *               other values will be ignored
     * @return java datetime in millis
     */
    public static long dateTimeInMillis(@Nullable TimeZone timeZone, int... fields){
        int[] f = Arrays.copyOf(fields,7);
        final Calendar c = timeZone == null ? Calendar.getInstance() : Calendar.getInstance(timeZone);
        c.set(Math.max(0,f[0] - 1900), MathUtils.clamp(f[1],1,12),
                MathUtils.clamp(f[2],1,31),
                f[3] % 24, f[4] % 60, f[5] % 60);
        c.set(Calendar.MILLISECOND,f[6] % 1000);
        return c.getTimeInMillis();
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
                if (DBG) Log.e(TAG,e.toString());
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
