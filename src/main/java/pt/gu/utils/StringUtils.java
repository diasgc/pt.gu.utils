package pt.gu.utils;

import android.text.format.DateUtils;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

public class StringUtils {

    @Nullable
    public static String[] split(@Nullable String source, String regex){
        return source != null ? source.split(regex) : null;
    }

    public static String bytesToString(byte[] bytes, char nonAsciiChar) {
        char[] chars = new char[bytes.length];
        for (int i = 0; i < chars.length; i++)
            if (bytes[i] < 0x20 || bytes[i] > 0x7e)
                chars[i] = nonAsciiChar;
            else
                chars[i] = (char) bytes[i];
        return new String(chars);
    }

    public static String bytesToString(byte[] bytes, int offset, int len, char nonAsciiChar) {
        char[] chars = new char[bytes.length];
        for (int i = offset; i < offset + len ; i++)
            if (bytes[i] < 0x20 || bytes[i] > 0x7e)
                chars[i] = nonAsciiChar;
            else
                chars[i] = (char) bytes[i];
        return new String(chars);
    }

    public static String bytesToHex(byte[] bytes, int offset, int len, boolean groupBytes, int groupSize) {
        StringBuilder sb = new StringBuilder();
        final int gszf = groupSize -1;
        for (int j = offset; j < offset + len; j++) {
            int v = bytes[j] & 0xFF;
            sb.append(hexArray[v >>> 4]).append(hexArray[v & 0x0F]);
            if (groupBytes) sb.append(' ');
            if (j % groupSize == gszf) sb.append(' ');
        }
        return sb.toString().trim();
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    static String BYPE_SEP = " ";
    static String GROUP_SEP = " ";

    public static String bytesToHex(byte[] bytes){
        return bytesToHex(bytes," "," ");
    }

    public static String bytesToHex(byte[] bytes, final String byteSep, final String groupSep) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            sb.append(hexArray[v >>> 4]).append(hexArray[v & 0x0F]).append(byteSep);
            if (j % 4 == 3) sb.append(GROUP_SEP);
        }
        return sb.toString().trim();
    }

    public static String bytesToHex(byte[] bytes, boolean groupBytes, int groupSize) {
        StringBuilder sb = new StringBuilder();
        final int gszf = groupSize -1;
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            sb.append(hexArray[v >>> 4]).append(hexArray[v & 0x0F]);
            if (groupBytes) sb.append(' ');
            if (j % groupSize == gszf) sb.append(' ');
        }
        return sb.toString().trim();
    }

    public static String bytesToHex(byte[] bytes, int group){
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < bytes.length; j++) {
            int v = 0;
            int jg = j*group;
            for (int k = 0 ; k < group ; k++)
                v = (v << 8) | (bytes[jg + k] & 0xFF);
            sb.append(Integer.toHexString(v).toUpperCase()).append(" ");
            if (j % 4 == 3) sb.append(" ");
        }
        return sb.toString().trim();
    }

    public static String join(String[] strings, String sep) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings)
            sb.append(s).append(sep);
        return sb.toString();
    }

    public static int lenghtOf(CharSequence s){
        return s == null ? 0 : s.length();
    }

    public static <T> StringBuilder appendIf(StringBuilder sb, boolean condition, T value){
        if (condition)
            sb.append(value);
        return sb;
    }

    @NonNull
    public static String join(CharSequence sep, CharSequence... values) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : values)
            if (s != null && s.length() > 0)
                sb.append(s).append(sep);
        return sb.toString();
    }

    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence s) {
        return s != null && s.length() > 0;
    }

    public static boolean areNotEmpty(CharSequence... charSequences) {
        for (CharSequence s0 : charSequences)
            if (s0 == null || s0.length() == 0)
                return false;
        return true;
    }

    public static String readStream(InputStream src){
        return readStream(src,"UTF-8",true);
    }

    public static String readStream(InputStream src, String encoding, boolean unixLineBreak) {
        StringBuilder sb = new StringBuilder();
        final String lineBreak = unixLineBreak ? "\n" : "\r\n";
        final BufferedReader br = new BufferedReader(new InputStreamReader(src, Charset.forName(encoding)));
        String read;
        try {
            while (null != (read = br.readLine()))
                sb.append(read).append(lineBreak);
        } catch (Exception ignore){ }
        IoUtils.closeQuietly(br);
        return sb.toString();
    }

    public static String toHexString(byte[] array, boolean toLowCase, @Nullable String byteSep){
        final char[] HEX_ARRAY = (toLowCase ? "0123456789abcdef" : "0123456789ABCDEF").toCharArray();
        if (byteSep == null)
            byteSep = "";
        StringBuilder sb = new StringBuilder();
        int v;
        for (byte b : array)
            sb.append(HEX_ARRAY[(v = b & 0xff) >>> 4]).append(v & 0xf).append(byteSep);
        return sb.toString();
    }

    public static String toHexString(int[] array, int width, boolean toLowCase, @Nullable String sep){
        final int mask = (1 << (width * 8)) - 1;
        final boolean appendSep = sep != null && sep.length() > 0;
        StringBuilder sb = new StringBuilder();
        String v;
        for (int i : array){
            v = Integer.toHexString(i & mask);
            if ((v.length() % 2) == 1)
                sb.append("0");
            sb.append(v);
            if (appendSep)
                sb.append(sep);
        }
        return sb.toString();
    }

    private static final String[] SIZE_UNITS = {"B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
    private static final int SIZE_UNITS_LEN = SIZE_UNITS.length;

    public static String formatSize(long size) {
        int i = 0;
        double s = size;
        while (s > 1024 && i < SIZE_UNITS_LEN) {
            s /= 1024;
            i++;
        }
        return String.format(Locale.US,"%.01f %s",s, SIZE_UNITS[i]);
    }

    public static String fromSize(Size size){
        return String.format(Locale.getDefault(),"%d x %d",size.getWidth(), size.getHeight());
    }

    public static String formatTime(long time){
        if (time < DateUtils.MINUTE_IN_MILLIS)
            return new SimpleDateFormat("s's'").format(time);
        if (time < DateUtils.HOUR_IN_MILLIS)
            return new SimpleDateFormat("m'm' s 's'").format(time);
        if (time < DateUtils.DAY_IN_MILLIS)
            return new SimpleDateFormat("H'h' m'm'").format(time);
        int d = (int)(time / DateUtils.DAY_IN_MILLIS);
        return ""+d+"d "+new SimpleDateFormat("H'h'").format(time % DateUtils.DAY_IN_MILLIS);
    }

    public static CharSequence[] toCharSequence(String[] array) {
        if (array == null || array.length == 0)
            return new CharSequence[0];
        CharSequence[] charSequences = new CharSequence[array.length];
        int i = 0;
        while (i < array.length)
            charSequences[i] = array[i++];
        return charSequences;
    }

    public static String fromMap(Map<String, String> map, String sep) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> e : map.entrySet())
            sb.append(e.getKey()).append(sep).append(e.getValue()).append("\n");
        return sb.toString();
    }

    public static String ellipsize(String text, int i, int where) {
        if (text.length() > i){
            switch (where){
                case 0:
                    return "…"+text.substring(text.length()-i-1);
                default:
                    return text.substring(0,i/2)+"…"+text.substring(text.length()-i/2);
                case 2:
                    return text.substring(0,i-1) + "…";
            }
        }
        return text;
    }

    public static String nonNull(@Nullable String str, String def) {
        return str == null ? def : str;
    }

    public static class StringPrinter extends PrintWriter {


        private StringWriter sw;

        public static StringPrinter get(){
            return new StringPrinter(new StringWriter());
        }

        private StringPrinter(StringWriter sw){
            super(sw);
            this.sw = sw;
        }

        public StringPrinter printf(String fmt, Object... args){
            printf(Locale.getDefault(), fmt, args);
            return this;
        }

        public StringPrinter printStream(InputStream is, boolean autoclose) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            for (String line; (line = br.readLine()) != null;)
                println(line);
            if (autoclose)
                IoUtils.closeQuietly(br);
            return this;
        }

        public boolean printStreamSafe(InputStream is, boolean autoclose) {
            try {
                printStream(is, autoclose);
                return true;
            } catch (IOException e){
                Log.e("StringUtils", "StringPrinter.printStreamSafe: " + e.toString());
            }
            return false;
        }

        @NonNull
        @Override
        public String toString() {
            return sw.toString();
        }
    }

}
