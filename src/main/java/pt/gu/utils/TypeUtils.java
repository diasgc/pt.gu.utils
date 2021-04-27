package pt.gu.utils;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

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
}
