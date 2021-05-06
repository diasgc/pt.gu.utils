package pt.gu.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.CancellationSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Stack;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class IoUtils {

    public static ParcelFileDescriptor pipeFrom(InputStream is) throws IOException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        TransferThread.start(is,new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]));
        return pipe[0];
    }

    public static ParcelFileDescriptor pipeTo(OutputStream os) throws IOException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        TransferThread.start(new ParcelFileDescriptor.AutoCloseInputStream(pipe[0]),os);
        return pipe[1];
    }

    public static byte[] toByteArray(InputStream input, boolean autoclose) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        while ((input.read(data)) > 0)
            baos.write(data);
        baos.flush();
        byte[] res = baos.toByteArray();
        baos.close();
        if (autoclose)
            closeQuietly(input);
        return res;
    }

    public static byte[] toByteArray(InputStream input, boolean autoclose, CancellationSignal signal) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        while ((input.read(data)) > 0 && !signal.isCanceled())
            baos.write(data);
        baos.flush();
        byte[] res = baos.toByteArray();
        baos.close();
        if (autoclose)
            closeQuietly(input);
        return res;
    }

    /**
     * Same as toString(src, "UTF-8", true, autoclose)
     * @param src the source inputstream
     * @param autoClose close stream after done
     * @return String content of inputstream
     */
    public static String toString(InputStream src, boolean autoClose){
        return toString(src,"UTF-8",true, autoClose);
    }

    /**
     *
     * @param src InputStream to read
     * @param encoding String encoding, if null is 'UTF-8'
     * @param unixEOL EndOfLine (EOL) Unix CR (true) or Windows CRLF (false)
     * @param autoclose should close InputStream after read?
     * @return String of all inputstream reading
     */
    public static String toString(InputStream src, @Nullable String encoding, boolean unixEOL, boolean autoclose) {
        if (encoding == null)
            encoding = "UTF-8";
        final StringBuilder sb = new StringBuilder();
        final String lineBreak = unixEOL ? "\n" : "\r\n";
        final BufferedReader br = new BufferedReader(new InputStreamReader(src, Charset.forName(encoding)));
        try {
            for (String read; null != (read = br.readLine());)
                sb.append(read).append(lineBreak);
        } catch (Exception e){
            Log.e(IoUtils.class.getSimpleName(),"toString: " + e.toString());
        }
        if (autoclose)
            IoUtils.closeQuietly(br);
        return sb.toString();
    }

    public static File fileFrom(ParcelFileDescriptor pfd) throws IOException{
        return new File("/proc/self/fd", String.valueOf(pfd.dup().getFd()));
    }

    public static void streamPrint(InputStream is, Consumer<String> out, CancellationSignal signal) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        for (String s; !signal.isCanceled() && null != (s = br.readLine());)
            out.accept(s);
    }


    public interface ProgressListener {

        void onProgress(long bytes, String name);
        boolean isCancelled();

    }

    public static void closeQuietly(Closeable... br) {
        if (br != null) {
            for (Closeable c : br) {
                if (c != null) {
                    try {
                        c.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }

    public static void closeQuietly(boolean autoflush, Closeable... br) {
        if (br != null) {
            for (Closeable c : br) {
                if (c != null) {
                    try {
                        if (autoflush && c instanceof Flushable)
                            ((Flushable) c).flush();
                        c.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }


    public static boolean streamCopy(InputStream is, File dst, @Nullable ProgressListener listener) {
        try {
            TransferThread.start(is, new FileOutputStream(dst),listener);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean streamCopy(Context context, Uri src, Uri dst, @Nullable ProgressListener listener) {
        final ContentResolver res = context.getContentResolver();
        try {
            streamCopy(res.openInputStream(src), res.openOutputStream(dst), listener);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Nullable
    public static InputStream getInputStream(Context context, Uri src){
        if (src == null)
            return null;
        try {
            switch (src.getScheme()) {
                case "content":
                    return context == null ? null : context.getContentResolver().openInputStream(src);
                case "file":
                    return new FileInputStream(new File(src.getLastPathSegment()));
                case "http": case "https": case "ftp":

            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static boolean streamCopy(int fdSrc, int fdDst, @Nullable ProgressListener listener){
        return streamCopy(new File("/proc/self/fd", String.valueOf(fdSrc)), new File("/proc/self/fd", String.valueOf(fdDst)), listener);
    }

    public static boolean streamCopy(File src, File dst,@Nullable ProgressListener listener) {
        try {
            new TransferThread(new FileInputStream(src), new FileOutputStream(dst),listener)
                    .setStreamName(src.getName())
                    .start();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean streamCopy(File src, OutputStream os, @Nullable ProgressListener listener) {
        try {
            new TransferThread(new FileInputStream(src), os, listener)
                    .setStreamName(src.getName())
                    .start();
            return true;
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return false;
    }

    public static void streamCopy(InputStream is, OutputStream os, @Nullable ProgressListener listener) {
        TransferThread.start(is,os,listener);
    }

    public static class HttpInputStream extends InputStream {

        public interface Callback {
            void onConnectionAvailable(HttpInputStream inputStream);
        }

        private static final String TAG = HttpInputStream.class.getSimpleName();

        private InputStream is;
        private HttpURLConnection connection;
        private boolean isConnected = false;

        public static void openUrl(Uri u, Callback callback){
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    callback.onConnectionAvailable(HttpInputStream.openUrl(u));
                }
            });
        }

        @Nullable
        public static HttpInputStream openUrl(Uri u){
            try {
                URL url = new URL(u.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                return new HttpInputStream(connection);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }


        HttpInputStream(HttpURLConnection c) throws IOException{
            connection = c;
            is = c.getInputStream();
            isConnected = is != null;
        }

        public boolean isConnected(){
            return isConnected;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return is.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return is.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return is.skip(n);
        }

        @Override
        public int available() throws IOException {
            return is.available();
        }

        @Override
        public synchronized void mark(int readlimit) {
            is.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            is.reset();
        }

        @Override
        public boolean markSupported() {
            return is.markSupported();
        }

        @Override
        public int read() throws IOException {
            return is == null ? 0 : is.read();
        }

        @Override
        public void close() throws IOException {
            is.close();
            connection.disconnect();
            super.close();
        }
    }

    public static class OutputStringWriter extends OutputStream {

        private StringWriter sw;
        private String eol = "\n";

        public OutputStringWriter(){
            sw = new StringWriter();
        }

        public OutputStringWriter(StringWriter sw){
            this.sw = sw;
        }

        public void writeLine(String line){
            sw.append(line).append(eol);
        }

        public void writef(String fmt, Object... args){
            sw.append(String.format(Locale.getDefault(),fmt,args));
        }

        @Override
        public void write(int b) throws IOException {
            sw.write((char) b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            sw.write(new String(b));
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            byte[] b2 = new byte[len];
            System.arraycopy(b, off, b2,0, len);
            write(b2);
        }

        @NonNull
        @Override
        public String toString() {
            return sw.toString();
        }
    }

    public static class BinaryReader extends FileInputStream {

        private Stack<Long> mStack = new Stack<>();
        private boolean isLE = true;

        public BinaryReader(String name) throws FileNotFoundException {
            super(name);
        }

        public BinaryReader(File file) throws FileNotFoundException {
            super(file);
        }

        public BinaryReader(FileDescriptor fdObj) {
            super(fdObj);
        }

        public static void test(File file) {
            try {
                BinaryReader f = new BinaryReader(file);
                long l = file.length() / 8;
                long t0;
                t0 = System.currentTimeMillis();
                for (int i = 0 ; i < l ; i++)
                    f.readInt64Test();
                Log.i("TEST int64Test",""+(System.currentTimeMillis() - t0));
                f = new BinaryReader(file);
                t0 = System.currentTimeMillis();
                for (int i = 0 ; i < l ; i++)
                    f.readInt64();
                Log.i("TEST int64",""+(System.currentTimeMillis() - t0));
                f = new BinaryReader(file);
                t0 = System.currentTimeMillis();
                for (int i = 0 ; i < l ; i++)
                    f.readULong(8);
                Log.i("TEST ULong8 ",""+(System.currentTimeMillis() - t0));
            } catch (IOException ignore){
                ignore.printStackTrace();
            }
        }

        public synchronized long pushPosition(long newPosition) throws IOException {
            long currPos = pushPosition();
            getChannel().position(newPosition);
            return currPos;
        }

        public synchronized long pushPosition() throws IOException {
            long currPos = getChannel().position();
            mStack.push(currPos);
            return currPos;
        }

        public long getPosition() throws IOException {
            return getChannel().position();
        }

        public synchronized long popPosition() throws IOException {
            if (mStack.empty())
                return 0;
            long newPos = mStack.pop();
            getChannel().position(newPos);
            return newPos;
        }

        public boolean isLittleEndian(){
            return isLE;
        }

        public void setByteOrder(boolean littleEndian){
            this.isLE = littleEndian;
        }

        public final String readString(int len) throws IOException {
            byte[] b = new byte[len];
            int i = read(b);
            return i != len ? new String(b).substring(0,i) : new String(b);
        }

        private final short swap(short int16){
            return (short)(int16 >> 8 | int16 << 8);
        }

        private final int swap(int int32){
            return int32 >> 16 | int32 << 16;
        }


        // test reading 23527224 bytes
        // int64Test: 8201ms
        // int64: 8066ms
        // ULong8: 8098ms

        // test reading 63109780 bytes
        // int64Test: 21596ms
        // int64: 21479ms
        // ULong8: 21510ms

        private final long readULong(int byteLen) throws IOException {
            byte[] b = new byte[byteLen];
            if (read(b) == -1)
                throw new EOFException();

            long res = 0;
            if (isLE) {
                int i = 0;
                while (i < byteLen)
                    res |= (int) b[i++] << (8 * (byteLen - i));
            } else {
                int i = byteLen - 1;
                while (i < byteLen)
                    res |= (int) b[i--] << (8 * (byteLen - i));
            }
            return res;
        }

        public final long readInt64Test() throws IOException {
            byte[] b = new byte[8];
            if (read(b) == -1)
                throw new EOFException();
            return iLSL(b[0],56) | iLSL(b[1],48) |
                    iLSL(b[2],40) | iLSL(b[3],32) |
                    iLSL(b[4],24) | iLSL(b[5],16) |
                    iLSL(b[6],8) | iLSL(b[7],0);
        }

        public final long readInt64() throws IOException{
            byte[] b = new byte[8];
            if (read(b) == -1)
                throw new EOFException();
            if (isLE)
                return b[7] << 56 | b[6] << 48 |
                        b[5] << 40 | b[4] << 32 |
                        b[3] << 24 | b[2] << 16 |
                        b[1] << 8 | b[0] << 0;
            else
                return b[0] << 56 | b[1] << 48 |
                        b[2] << 40 | b[3] << 32 |
                        b[4] << 24 | b[5] << 16 |
                        b[6] << 8 | b[7] << 0;
        }

        public final long readUInt64() throws IOException{
            byte[] b = new byte[8];
            if (read(b) == -1)
                throw new EOFException();
            if (isLE)
                return (b[7] & 0xffL) << 56 | (b[6] & 0xffL) << 48 |
                        (b[5] & 0xffL) << 40 | (b[4] & 0xffL) << 32 |
                        (b[3] & 0xffL) << 24 | (b[2] & 0xffL) << 16 |
                        (b[1] & 0xffL) << 8 | (b[0] & 0xffL) << 0;
            else
                return (b[0] & 0xffL) << 56 | (b[1] & 0xffL) << 48 |
                    (b[2] & 0xffL) << 40 | (b[3] & 0xffL) << 32 |
                    (b[4] & 0xffL) << 24 | (b[5] & 0xffL) << 16 |
                    (b[6] & 0xffL) << 8 | (b[7] & 0xffL) << 0;
        }

        public final long readQWord() throws IOException {
            return readInt64();
        }

        public final long readFword() throws IOException {
            byte[] b = new byte[6];
            if (read(b) == -1)
                throw new EOFException();
            if (isLE)
                return (b[4] & 0xFFL) << 40 |
                        (b[3] & 0xFFL) << 32 |
                        (b[2] & 0xFFL) << 16 |
                        (b[1] & 0xFFL) <<  8 |
                        (b[0] & 0xFFL) <<  0;
            else
                return (b[0] & 0xFFL) << 40 |
                    (b[2] & 0xFFL) << 32 |
                    (b[3] & 0xFFL) << 16 |
                    (b[4] & 0xFFL) <<  8 |
                    (b[5] & 0xFFL) <<  0;
        }


        public final long readUInt32() throws IOException {
            byte[] b = new byte[4];
            if (read(b) == -1)
                throw new EOFException();
            if (isLE)
                return (b[3] & 0xFFL) << 24 |
                        (b[2] & 0xFFL) << 16 |
                        (b[1] & 0xFFL) << 8 |
                        (b[0] & 0xFFL) << 0;
            else
                return (b[0] & 0xFFL) << 24 |
                        (b[1] & 0xFFL) << 16 |
                        (b[2] & 0xFFL) << 8 |
                        (b[3] & 0xFFL) << 0;
        }

        private static final int iLSL(byte b, int bits){
            return (b & 0xFF) << bits;
        }

        /**
         * rate 1724 (ref.1675)
         * @param byteLen no. of bytes (must be 1-4)
         * @return n- byte integer
         * @throws IOException
         */
        private final int readUInt(int byteLen) throws IOException {
            if (byteLen == 0 || byteLen > 4)
                throw new IOException();
            byte[] b = new byte[byteLen];
            if (read(b) == -1)
                throw new EOFException();
            int i = 0;
            int res = 0;
            while (i < byteLen)
                res |= (int) b[i++] << (8*(byteLen-i));
            return res;
        }

        public final int readInt32() throws IOException {
            byte[] b = new byte[4];
            if (read(b) == -1)
                throw new EOFException();
            //return peekInt(b,0,ByteOrder.BIG_ENDIAN);
            if (isLE)
                return b[3] << 24 | b[2] << 16 | b[1] << 8 | b[0] << 0;
            else
                return b[0] << 24 | b[1] << 16 | b[2] << 8 | b[3] << 0;
        }

        public final int readUInt16() throws IOException {
            byte[] b = new byte[2];
            if (read(b) < 0)
                throw new EOFException();
            if (isLE)
                return (b[1] & 0xFF) << 8 | (b[0] & 0xFF) << 0;
            else
                return (b[0] & 0xFF) << 8 | (b[1] & 0xFF) << 0;
        }

        public final short readInt16() throws IOException {
            byte[] b = new byte[2];
            if (read(b) < 0)
                throw new EOFException();
            if (isLE)
                return (short) (b[1] << 8 | b[0] << 0);
            else
                return (short) (b[0] << 8 | b[1] << 0);
        }

        public final byte readInt8() throws IOException {
            int ch = this.read();
            if (ch < 0)
                throw new EOFException();
            return (byte)(ch);
        }

        public final int readUInt8() throws IOException {
            int ch = this.read();
            if (ch < 0)
                throw new EOFException();
            return ch;
        }

        public byte[] readInt8Array(int len) throws IOException {
            byte[] b = new byte[len];
            if (read(b) < 0)
                throw new EOFException();
            return b;
        }

        public int[] readUInt8Array(int len) throws IOException {
            final int[] b = new int[len];
            final byte[] b0 = readInt8Array(len);
            for (int i = 0 ; i < len ; i++)
                b[i] = b0[i];
            return b;
        }

        public int[] readUInt16Array(int len) throws IOException {
            int[] b = new int[len];
            byte[] bb = readInt8Array(len * 2);
            if (isLE) {
                int i = 0, j = bb.length -1;
                while (i < len)
                    b[i++] = ((bb[j--] & 0xFF ) << 8) | ((bb[j--] & 0xFF) << 0);
            } else {
                int i = 0, j = 0;
                while (i < len)
                    b[i++] =  ((bb[j++] & 0xFF) << 8) | ((bb[j++] & 0xFF) << 0);
            }
            return b;
        }

        public short[] readInt16Array(int len) throws IOException {
            short[] b = new short[len];
            byte[] bb = readInt8Array(len * 2);
            if (isLE) {
                int i = 0, j = bb.length -1;
                while (i < len)
                    b[i++] = (short)((bb[j--] << 8) | (bb[j--] << 0));
            } else {
                int i = 0, j = 0;
                while (i < len)
                    b[i++] = (short)((bb[j++] << 8) | (bb[j++] << 0));
            }
            return b;
        }

        public int readFixed() throws IOException{
            return readInt32() / (1 << 16);
        }

        public int readF2DOT14() throws IOException{
            return readInt32() / (1 << 14);
        }

        public long readLONGDATETIME() throws IOException {
            long d = readInt64();
            long t = TimeUnit.MILLISECONDS.convert(d,TimeUnit.SECONDS);
            DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm aaa", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = null;
            try {
                date = formatter.parse("1904/01/01 12:00 AM");
                return date != null ? date.getTime() + t : d;
            } catch (ParseException e) {
                e.printStackTrace();
                return t;
            }
        }

        static int peekInt(byte[] src, int offset, ByteOrder order) {
            if (order == ByteOrder.BIG_ENDIAN) {
                return (((src[offset++] & 0xff) << 24) |
                        ((src[offset++] & 0xff) << 16) |
                        ((src[offset++] & 0xff) <<  8) |
                        ((src[offset  ] & 0xff) <<  0));
            } else {
                return (((src[offset++] & 0xff) <<  0) |
                        ((src[offset++] & 0xff) <<  8) |
                        ((src[offset++] & 0xff) << 16) |
                        ((src[offset  ] & 0xff) << 24));
            }
        }

        static long peekLong(byte[] src, int offset, ByteOrder order) {
            if (order == ByteOrder.BIG_ENDIAN) {
                int h = ((src[offset++] & 0xff) << 24) |
                        ((src[offset++] & 0xff) << 16) |
                        ((src[offset++] & 0xff) <<  8) |
                        ((src[offset++] & 0xff) <<  0);
                int l = ((src[offset++] & 0xff) << 24) |
                        ((src[offset++] & 0xff) << 16) |
                        ((src[offset++] & 0xff) <<  8) |
                        ((src[offset  ] & 0xff) <<  0);
                return (((long) h) << 32L) | ((long) l) & 0xffffffffL;
            } else {
                int l = ((src[offset++] & 0xff) <<  0) |
                        ((src[offset++] & 0xff) <<  8) |
                        ((src[offset++] & 0xff) << 16) |
                        ((src[offset++] & 0xff) << 24);
                int h = ((src[offset++] & 0xff) <<  0) |
                        ((src[offset++] & 0xff) <<  8) |
                        ((src[offset++] & 0xff) << 16) |
                        ((src[offset  ] & 0xff) << 24);
                return (((long) h) << 32L) | ((long) l) & 0xffffffffL;
            }
        }


        //region deprecated

        public final long _readInt64() throws IOException{
            long ch1 = readInt32();
            long ch2 = readInt32();
            return (ch1 << 32) + (ch2 << 0);
        }

        public final short _readInt16() throws IOException {
            int ch1 = this.read();
            int ch2 = this.read();
            if ((ch1 | ch2) < 0)
                throw new EOFException();
            return (short)((ch1 << 8) + (ch2 << 0));
        }

        public final int _readUInt16() throws IOException {
            int ch1 = this.read();
            int ch2 = this.read();
            if ((ch1 | ch2) < 0)
                throw new EOFException();
            return (ch1 << 8) + (ch2 << 0);
        }

        private final int _readInt32() throws IOException {
            int ch1 = readInt16();
            int ch2 = readInt16();
            return (ch1 << 16) + (ch2 << 0);
        }

        //6119 ms @ 2MB
        private final long readUInt32Native() throws IOException{
            int ch1 = read();
            int ch2 = read();
            int ch3 = read();
            int ch4 = read();
            if ((ch1 | ch2 | ch3 | ch4) < 0)
                throw new EOFException();
            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        }

        private final int read8shift(int n) throws IOException {
            int r = read();
            if (r < 0)
                throw new EOFException();
            return (n << 8) | r ;
        }

        // 6190 ms @ 2MB
        private final long readUInt32Test0() throws IOException {
            long res = read8shift(read8shift(read8shift(0)));
            int last = read();
            if (last >= 0)
                return res | last;
            throw new EOFException();
        }

        // 6095 ms @ 2MB
        private final long readUInt32Test1() throws IOException {
            long res = 0;
            int read = read();
            if (read >= 0){
                res = (res | read) >> 8;
                if ((read = read()) >= 0){
                    res = (res | read) >> 8;
                    if ((read = read()) >= 0){
                        res = (res | read) >> 8;
                        if ((read = read()) >= 0){
                            return res | read;
                        }
                    }
                }
            }
            throw new EOFException();
        }
        //endregion
    }

    public interface JsonByteParser {
        void parse(byte[] bytes, int len, JSONObject object);
    }

    public static class Binary2JsonReader extends BinaryReader {

        private LinkedList<JSONObject> mJsonList = new LinkedList<>();

        public Binary2JsonReader(String fileName) throws FileNotFoundException {
            super(fileName);
            init();
        }

        public Binary2JsonReader(File file) throws FileNotFoundException {
            super(file);
            init();
        }

        public Binary2JsonReader(FileDescriptor fdObj) {
            super(fdObj);
            init();
        }

        private void init(){
            JSONObject o = new JSONObject();
            mJsonList.push(o);
        }

        public Binary2JsonReader readString(String fieldName, int len) throws IOException {
            try {
                mJsonList.getLast().put(fieldName,readString(len));
            } catch (JSONException ignore){}
            return this;
        }

        public Binary2JsonReader readBytes(int len, JsonByteParser parser) throws IOException {
            byte[] b = new byte[len];
            if (read(b) < 0)
                throw new EOFException();
            parser.parse(b,len,mJsonList.getLast());
            return this;
        }

        public Binary2JsonReader readInt64(String fieldName) throws IOException {
            try {
                mJsonList.getLast().put(fieldName,readInt64());
            } catch (JSONException ignore){}
            return this;
        }

        public Binary2JsonReader readInt32(String fieldName) throws IOException {
            try {
                mJsonList.getLast().put(fieldName,readInt32());
            } catch (JSONException ignore){}
            return this;
        }

        public Binary2JsonReader readUInt32(String fieldName) throws IOException {
            try {
                mJsonList.getLast().put(fieldName,readUInt32());
            } catch (JSONException ignore){}
            return this;
        }

        public Binary2JsonReader readInt16(String fieldName) throws IOException {
            try {
                mJsonList.getLast().put(fieldName,readInt16());
            } catch (JSONException ignore){}
            return this;
        }

        public Binary2JsonReader readUInt16(String fieldName) throws IOException {
            try {
                mJsonList.getLast().put(fieldName,readUInt16());
            } catch (JSONException ignore){}
            return this;
        }

        public Binary2JsonReader readInt8(String fieldName) throws IOException {
            try {
                mJsonList.getLast().put(fieldName,readInt8());
            } catch (JSONException ignore){}
            return this;
        }

        public Binary2JsonReader readUInt8(String fieldName) throws IOException {
            try {
                mJsonList.getLast().put(fieldName,readUInt8());
            } catch (JSONException ignore){}
            return this;
        }

        public Binary2JsonReader pushOffset32(long relativePosition) throws IOException {
            pushPosition(relativePosition + readUInt32());
            JSONObject o = new JSONObject();
            mJsonList.add(o);
            return this;
        }

        public Binary2JsonReader pushOffset16(long relativePosition) throws IOException {
            pushPosition(relativePosition + readUInt16());
            JSONObject o = new JSONObject();
            mJsonList.add(o);
            return this;
        }

        public Binary2JsonReader newChild(){
            JSONObject o = new JSONObject();
            mJsonList.add(o);
            return this;
        }

        public Binary2JsonReader saveChild(String childName){
            JSONObject o = mJsonList.pop();
            try {
                mJsonList.getLast().put(childName,o);
            } catch (JSONException ignore) {}
            return this;
        }

        public Binary2JsonReader pop(String fieldName) throws IOException {
            popPosition();
            JSONObject o = mJsonList.pop();
            try {
                mJsonList.getLast().put(fieldName,o);
            } catch (JSONException ignore) {}
            return this;
        }
    }

    public static final class BitOutputStream implements AutoCloseable {

        private OutputStream out;
        private long bitBuffer;
        private int bitBufferLen;
        public int crc8;
        public int crc16;


        public BitOutputStream(OutputStream out) {
            this.out = out;
            bitBuffer = 0;
            bitBufferLen = 0;
            resetCrcs();
        }


        public void resetCrcs() {
            crc8 = 0;
            crc16 = 0;
        }


        public void alignToByte() throws IOException {
            writeInt((64 - bitBufferLen) % 8, 0);
        }


        public void writeInt(int n, int val) throws IOException {
            bitBuffer = (bitBuffer << n) | (val & ((1L << n) - 1));
            bitBufferLen += n;
            while (bitBufferLen >= 8) {
                bitBufferLen -= 8;
                int b = (int) (bitBuffer >>> bitBufferLen) & 0xFF;
                out.write(b);
                crc8 ^= b;
                crc16 ^= b << 8;
                for (int i = 0; i < 8; i++) {
                    crc8 = (crc8 << 1) ^ ((crc8 >>> 7) * 0x107);
                    crc16 = (crc16 << 1) ^ ((crc16 >>> 15) * 0x18005);
                }
            }
        }


        public void close() throws IOException {
            out.close();
        }
    }

    public static final class BitRandomAccessOutputStream implements AutoCloseable {

        private RandomAccessFile raf;
        private long bitBuffer;
        private int bitBufferLen;
        public int crc8;
        public int crc16;


        public BitRandomAccessOutputStream(RandomAccessFile raf) {

            this.raf = this.raf;
            bitBuffer = 0;
            bitBufferLen = 0;
            resetCrcs();
        }


        public void resetCrcs() {
            crc8 = 0;
            crc16 = 0;
        }


        public void alignToByte() throws IOException {
            writeInt((64 - bitBufferLen) % 8, 0);
        }

        public void seek(long pos, int bitOffset) throws IOException{
            raf.seek(pos);
            int v = raf.read();
        }


        public void writeInt(int n, int val) throws IOException {
            bitBuffer = (bitBuffer << n) | (val & ((1L << n) - 1));
            bitBufferLen += n;
            while (bitBufferLen >= 8) {
                bitBufferLen -= 8;
                int b = (int) (bitBuffer >>> bitBufferLen) & 0xFF;
                raf.write(b);
                crc8 ^= b;
                crc16 ^= b << 8;
                for (int i = 0; i < 8; i++) {
                    crc8 = (crc8 << 1) ^ ((crc8 >>> 7) * 0x107);
                    crc16 = (crc16 << 1) ^ ((crc16 >>> 15) * 0x18005);
                }
            }
        }


        public void close() throws IOException {
            raf.close();
        }
    }

    public static final class BitBuffer {

        private ByteBuffer out;
        private long bitBuffer;
        private int bitBufferLen;
        public int crc8;
        public int crc16;

        private int len;


        public BitBuffer(int capacity) {
            this.out = ByteBuffer.allocate(capacity);
            bitBuffer = 0;
            bitBufferLen = 0;
            len = 0;
            resetCrcs();
        }


        public void resetCrcs() {
            crc8 = 0;
            crc16 = 0;
        }


        public void alignToByte() throws IOException {
            writeInt((64 - bitBufferLen) % 8, 0);
        }


        public void writeInt(int n, int val) throws IOException {
            bitBuffer = (bitBuffer << n) | (val & ((1L << n) - 1));
            bitBufferLen += n;
            len += n;
            while (bitBufferLen >= 8) {
                bitBufferLen -= 8;
                int b = (int) (bitBuffer >>> bitBufferLen) & 0xFF;
                out.putInt(b);
                crc8 ^= b;
                crc16 ^= b << 8;
                for (int i = 0; i < 8; i++) {
                    crc8 = (crc8 << 1) ^ ((crc8 >>> 7) * 0x107);
                    crc16 = (crc16 << 1) ^ ((crc16 >>> 15) * 0x18005);
                }
            }
        }

        public void writeByte(int b) throws IOException{
            writeInt(8,b);
        }

        public void writeShort(int int16) throws IOException {
            writeInt(16,int16);
        }

        public void writeInt32(int int32) throws IOException {
            writeInt(32,int32);
        }

        public byte[] getBytes(){
            byte[] res = new byte[len];
            out.get(res,0,len);
            return res;
        }

        public int size(){
            return len;
        }

        public void writeTo(OutputStream outputStream) throws IOException {
            outputStream.write(getBytes());
        }
    }


    public static class TransferThread extends Thread {

        public static void start(InputStream is, OutputStream os, @Nullable ProgressListener listener){
            TransferThread t = new TransferThread(is,os,listener);
            t.start();
        }

        public static void start(InputStream is, OutputStream os){
            TransferThread t = new TransferThread(is,os);
            t.start();
        }

        public static void start(InputStream is, OutputStream os, @Nullable CancellationSignal signal){
            TransferThread t = new TransferThread(is,os,signal);
            t.start();
        }

        InputStream in;
        OutputStream out;
        CancellationSignal cancellationSignal;
        ProgressListener callback;
        String name = null;
        int bufferSize = 4096;

        public TransferThread(InputStream is, OutputStream os){
            in = is;
            out = os;
        }

        public TransferThread(InputStream is, OutputStream os, ProgressListener listener){
            in = is;
            out = os;
            callback = listener;
            //cancellationSignal = cancel == null ? new CancellationSignal() : cancel;
        }

        public TransferThread(InputStream is, OutputStream os, CancellationSignal signal){
            in = is;
            out = os;
            cancellationSignal = signal;
        }

        public TransferThread setStreamName(String name){
            this.name = name;
            return this;
        }

        public TransferThread setBufferSize(int bufferSize){
            this.bufferSize = bufferSize;
            return this;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[bufferSize];
            int len;
            long sum = 0;
            try {
                if (cancellationSignal != null){
                    while (!cancellationSignal.isCanceled() && -1 != (len =in.read(buffer)))
                        out.write(buffer,0,len);
                } else if (callback == null){
                    while (-1 != (len =in.read(buffer)))
                        out.write(buffer,0,len);
                } else {
                    while (-1 != (len = in.read(buffer)) && !callback.isCancelled()) {
                        out.write(buffer, 0, len);
                        sum += len;
                        callback.onProgress(sum, name);
                    }
                }
                closeQuietly(true, in, out);
            } catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public static int readInt8u(ByteBuffer bb) { return 0xff & bb.get(); }

    public static int readInt16u(ByteBuffer bb){
        return 0xffff & bb.getShort();
    }

    public static long readInt32u(ByteBuffer bb){
        return 0xffffffffL & bb.getInt();
    }

    public static int readInt8u(ByteBuffer bb, int offset) { return 0xff & bb.get(offset); }

    public static int readInt16u(ByteBuffer bb, int offset){
        return 0xffff & bb.getShort(offset);
    }

    public static long readInt32u(ByteBuffer bb, int offset){
        return 0xffffffffL & bb.getInt(offset);
    }
}
