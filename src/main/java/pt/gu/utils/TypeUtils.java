package pt.gu.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeUtils {

    private static final String TAG = TypeUtils.class.getSimpleName();

    private static final Pattern DOUBLE_UNITS_PATTERN = Pattern.compile("([0-9]+\\.[0-9]+)\\s*([Y|Z|E|P|T|G|M|k|c|m|µ|n|p|f])([a-zA-z]+)");
    private static final ArrayMap<String,Double> UNITS = new ArrayMap<>();
    static {
        UNITS.put("Z",1.0E21);
        UNITS.put("E",1.0E18);
        UNITS.put("P",1.0E15);
        UNITS.put("T",1.0E12);
        UNITS.put("G",1.0E9);
        UNITS.put("M",1.0E6);
        UNITS.put("k",1.0E3);
        UNITS.put("c",1.0E-2);
        UNITS.put("m",1.0E-3);
        UNITS.put("µ",1.0E-6);
        UNITS.put("n",1.0E-9);
        UNITS.put("p",1.0E-12);
        UNITS.put("f",1.0E-15);
    };

    /**
     *
     * @param s string to be evaluated, may be null (resultIfError value returned)
     * @param unit accepted unit in 's' otherwise returns 'resultIfError';
     *             if null, any unit will be accepted.
     * @param resultIfError returned value if s is null or invalid numberformat
     * @return double parsed value of given value-units string
     */
    public static double parseUnit(@Nullable String s, @Nullable String unit, double resultIfError){
        if (s != null && s.length() > 0) {
            Matcher m = DOUBLE_UNITS_PATTERN.matcher(s);
            double result;
            if (m.find()){
                // Get unit from 's'
                String s1 = m.group(3);
                // Return 'resultIfError' if 'unit' is defined but different from given in 's'
                if (unit != null && !unit.equals(s1))
                    return resultIfError;
                // Get value from 's'
                s1 = m.group(1);
                try {
                    // Try parse value or assign it to 0 if is null or empty
                    if ((result = s1 != null && s1.length() > 0 ? Double.parseDouble(s1) : 0) != 0){
                        // Get unit prefix from 's'
                        if ((s1 = m.group(2)) != null && UNITS.get(s1) != null) {
                            // Apply it to result, if exists
                            result *= UNITS.get(s1);
                        }
                    }
                    return result;
                } catch (Exception e){
                    Log.e(TAG, String.format("Tried to parse double with units from '%s': %s",s, e.toString()));
                }
            }
        }
        return resultIfError;
    }

    public static int parseInt(@Nullable String s, int resultIfError){
        return (int) parseLong(s,resultIfError);
    }

    public static long parseLong(@Nullable String s, long resultIfError){
        if (s != null && (s = s.trim()).length() > 0) {
            try {
                if (s.charAt(0) == '0' && s.length() > 2){
                    if (s.charAt(1) == 'x')
                        return Long.parseLong(s.substring(2),16);
                    if (s.charAt(1) == 'b')
                        return Long.parseLong(s.substring(2),2);
                    return Long.parseLong(s.substring(2),8);
                }
                return Long.parseLong(s);
            } catch (Exception e) {
                Log.e(TAG, String.format("Tried to parse long from '%s': %s",s, e.toString()));
            }
        }
        return resultIfError;
    }

    public static float parseFloat(@Nullable String s, float resultIfError){
        if (s != null && (s = s.trim()).length() > 0) {
            try {
                return Float.parseFloat(s);
            } catch (Exception e) {
                Log.e(TAG, String.format("Tried to parse float from '%s': %s",s, e.toString()));
            }
        }
        return resultIfError;
    }

    public static double parseDouble(@Nullable String s, double resultIfError){
        if (s != null && (s = s.trim()).length() > 0) {
            try {
                return Double.parseDouble(s);
            } catch (Exception e) {
                Log.e(TAG, String.format("Tried to parse double from '%s': %s",s, e.toString()));
            }
        }
        return resultIfError;
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

    public static <A> String stringOf(List<A> list, Function<A,String> formatter, String sep){
        final StringBuilder sb = new StringBuilder();
        if (list.size() > 0) {
            for (int i = 0; i < list.size() - 1; i++)
                sb.append(formatter.apply(list.get(i))).append(sep);
            sb.append(list.get(list.size() - 1));
        }
        return sb.toString();
    }

    public static <A,B> String stringOf(Pair<A,B> pair, Function<A,String> firstFmt, Function<B,String> secondFmt, String sep){
        return String.format("%s%s%s",firstFmt.apply(pair.first),sep,secondFmt.apply(pair.second));
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

    public static String formatTime(String timeFormat, long time) {
        final SimpleDateFormat sdf = new SimpleDateFormat(timeFormat,Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(time);
    }


    public static class BitBuilder {

        private final static byte[] O = {(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};
        private final static byte[] A = {(byte)0xFE,(byte)0xFD,(byte)0xFB,(byte)0xF7,(byte)0xEF,(byte)0xDF,(byte)0xBF,(byte)0x7F};

        private int bitSize;
        private byte[] data;
        private int bitPos = 0;

        enum State{
            SET, RESET, TOGGLE
        }

        public BitBuilder(int bitSize){
            this.bitSize = bitSize;
            data = new byte[bitSize % 8 > 0 ? bitSize/8 + 1 : bitSize/8];
        }

        public BitBuilder setBit(int bitIndex, State state){
            if (state == State.SET)
                data[bitIndex / 8] |= O[bitIndex % 8];
            else if (state == State.RESET)
                data[bitIndex / 8] &= A[bitIndex % 8];
            else
                data[bitIndex / 8] ^= O[bitIndex % 8];
            return this;
        }

        public BitBuilder setFlag(long flag, State state){
            int i = 0;
            if (state == State.SET) {
                while (i < 8 || flag != 0) {
                    data[i++] |= (byte)(flag & 0xFF);
                    flag >>=8;
                }
            } else if (state == State.RESET) {
                while (i < 8 || flag != 0) {
                    data[i++] &= ~(byte)(flag & 0xFF);
                    flag >>=8;
                }
            } else {
                while (i < 8 || flag != 0) {
                    data[i++] ^= (byte)(flag & 0xFF);
                    flag >>=8;
                }
            }
            return this;
        }

        public byte[] get(){
            return data;
        }

        /**
         *
         * @param bitWidth from 0 to 64 bits, other values will be clamped to this range
         * @return long array with defined bitWidth elements representation of data byte array
         */
        public long[] get(int bitWidth){
            bitWidth = MathUtils.clamp(bitWidth,0,64);
            final long[] out = new long[(int)Math.ceil(data.length / (bitWidth/8.0))];
            int k = 0;
            for (int i = 0 ; i < out.length; i++){
                for (int j = 0 ; j < bitWidth; j +=8) {
                    out[i] |= ((byte) (data[k++] & 0xFF)) << j;
                }
            }
            return out;
        }
    }

    public static boolean testFlag(int flags, int flag) {
        return (flags & flag) == flag;
    }

    public static int setFlag(int flags, int flag){
        return flags | flag;
    }

    public static int resetFlag(int flags, int flag){
        return flags & (~flag);
    }

    public static int toggleFlag(int flags, int flag){
        return flags ^ flag;
    }

    public static long setFlag(long flags, long flag){
        return flags | flag;
    }

    public static long resetFlag(long flags, long flag){
        return flags & (~flag);
    }

    public static long toggleFlag(long flags, long flag){
        return flags ^ flag;
    }



    public static class FlagBuilder {

        long flags;

        public FlagBuilder(){
            flags = 0;
        }

        public FlagBuilder(long init){
            flags = init;
        }

        public void reset(){
            flags = 0;
        }

        public FlagBuilder setIf(boolean condition, long flag){
            return condition ? set(flag) : this;
        }

        public FlagBuilder unsetIf(boolean condition, long flag){
            return condition ? unset(flag) : this;
        }

        public FlagBuilder set(long flag){
            flags |= flag;
            return this;
        }

        public FlagBuilder unset(long flag){
            flags &= ~flag;
            return this;
        }

        public int get32bFlags(){
            return (int)flags;
        }

        public long get64bFlags(){
            return flags;
        }
    }


    public static int getColor(Context context, @AttrRes int attr){
        final Resources.Theme t = context.getTheme();
        TypedValue tv = new TypedValue();
        t.resolveAttribute(attr,tv,true);
        int colorRes = tv.resourceId == 0 ? tv.data : tv.resourceId;
        return context.getColor(colorRes);
    }

    public static String getXmlColor(int color, boolean withAlpha){
        return withAlpha ? String.format("#%06X",color) :
                String.format("#%06X",color & 0xFFFFFF);
    }
}
