package pt.gu.utils;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TypeUtils {

    private static final SimpleDateFormat RFC_3399 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
    
    public static String dateToRFC3339(Date d) {
        return RFC_3399.format(d).replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2");
    }

    public static long parseDate(String date, String format, long resultIfError){
        final SimpleDateFormat sdf = new SimpleDateFormat(format,Locale.US);
        try {
            final Date d = sdf.parse(date);
            return d == null ? resultIfError : d.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return resultIfError;
        }
    }

    public static long parseLong(@Nullable String s, long resultIfError){
        try {
            return s == null ? resultIfError : (s = s.trim()).charAt(0) == '0' && s.length() > 2 ?
                    s.charAt(1) == 'x' ? Long.parseLong(s.substring(2),16) :
                            s.charAt(1) == 'b' ? Long.parseLong(s.substring(2),2) :
                                    Long.parseLong(s) : Long.parseLong(s);
        } catch (Exception ex){
            ex.printStackTrace();
            return resultIfError;
        }
    }

    public static int parseInt(@Nullable String s, int resultIfError){
        try {
            return s == null ? resultIfError : (s = s.trim()).charAt(0) == '0' && s.length() > 2 ?
                    s.charAt(1) == 'x' ? Integer.parseInt(s.substring(2),16) :
                            s.charAt(1) == 'b' ? Integer.parseInt(s.substring(2),2) :
                                    Integer.parseInt(s) : Integer.parseInt(s);
        } catch (Exception ex){
            ex.printStackTrace();
            return resultIfError;
        }
    }

    public static float parseFloat(@Nullable String s, float resultIfError){
        try {
            return s == null ? resultIfError : Float.parseFloat(s.trim());
        } catch (Exception ex){
            ex.printStackTrace();
            return resultIfError;
        }
    }

    public static int[] toIntArray(List<Integer> list){
        final int[] result = new int[list.size()];
        int id = 0;
        for (int i : list)
            result[id++] = i;
        return result;
    }

    public static long[] toLongArray(List<Long> list){
        final long[] result = new long[list.size()];
        int id = 0;
        for (long i : list)
            result[id++] = i;
        return result;
    }

    public static float[] toFloatArray(List<Long> list){
        final float[] result = new float[list.size()];
        int id = 0;
        for (float i : list)
            result[id++] = i;
        return result;
    }

    public static boolean areNonNull(Object... objects){
        for (Object o : objects)
            if (o == null)
                return false;
        return true;
    }

    public static int valueOf(boolean b){
        return b ? 1 : 0;
    }

    public static int boolSub(boolean a, boolean b){
        return (a ? 1 : 0) - (b ? 1 : 0);
    }

    public static int boolAdd(boolean a, boolean b){
        return (a ? 1 : 0) + (b ? 1 : 0);
    }

    public static int boolMul(boolean a, boolean b){
        return (a ? 1 : 0) + (b ? 1 : 0);
    }
}
