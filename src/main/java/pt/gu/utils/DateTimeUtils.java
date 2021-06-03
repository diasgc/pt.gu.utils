package pt.gu.utils;

import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
}
