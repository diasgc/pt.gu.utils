package pt.gu.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    public interface FilenameBuilder {
        String buildNew(String name, String ext);
    }

    private static final String TAG = FileUtils.class.getSimpleName();

    private static final int ABBREV_DATE_FORMAT = DateUtils.FORMAT_ABBREV_WEEKDAY
            | DateUtils.FORMAT_ABBREV_MONTH
            | DateUtils.FORMAT_ABBREV_RELATIVE;
    private static final long LIMITDATE = new Date(80,1,1).getTime();

    public static final int KB = 1024;
    public static final int MB = 1024 * KB;
    public static final int GB = 1024 * MB;
    public static final long TB = 1024 * GB;

    public static String formatSize(long size, String format) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1000));
        return new DecimalFormat(format).format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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
        return String.format(Locale.US,"%.01f %s",s,SIZE_UNITS[i]);
    }


    public static String formatSize_(long size) {
        return formatSize(size, "#,##0.#");
    }

    public static String formatSize(long size, int flags) {
        String formatSize = formatSize(size,"#,##0.#");
        if (flags == 0 || size < 1024)
            return formatSize;
        if (flags == 2)
            return formatSize + " (0x" + Long.toHexString(size) + " bytes)";
        return formatSize + " ("+size+" bytes)";
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

    public static File getFileWithExtension(File f, String newExt) {
        final String n = f.getName();
        return new File(f.getParentFile(),n.substring(0,n.lastIndexOf('.')) + newExt);
    }

    public static String getExtension(String name) {
        int i = name.lastIndexOf('.');
        return i > 0 && (i + 1) < name.length() ? name.substring(i+1) : "";
    }

    public static boolean writeString(File file, String toString) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(toString);
            bw.flush();
            bw.close();
            return true;
        } catch (Exception ex){
            Log.e(TAG,ex.toString());
        }
        return false;
    }

    @Nullable
    public static String readString(File f){
        try {
            BufferedReader bw = new BufferedReader(new FileReader(f));
            final StringBuilder sb = new StringBuilder();
            String r;
            while (null != (r =bw.readLine()))
                sb.append(r).append("\n");
            bw.close();
            return sb.toString();
        } catch (Exception ex){
            Log.e(TAG,ex.toString());
        }
        return null;
    }

    public static boolean renameTo(File src, String name){
        File newFile = new File(src.getParentFile(),name);
        return src.renameTo(newFile);
    }

    @Nullable
    public static String readString(InputStream is, String encoding) {
        if (is != null) {
            if (encoding == null)
                encoding = "UTF-8";
            try {
                final byte[] bytes = new byte[is.available()];
                final int len = is.read(bytes, 0, is.available());
                return new String(bytes, encoding);
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
        return null;
    }

    public static int getChildCount(File f) {
        final String[] childs = f.list();
        return childs == null ? 0 : childs.length;
    }

    public static String readFile(String filepath) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new FileReader(filepath));
        String line;
        while (null != (line = r.readLine()))
            sb.append(line).append("\n");
        r.close();
        return sb.toString();
    }

    @Nullable
    public static String readFile(String filePath,@Nullable String resultIfError) {
        try {
            return readFile(filePath);
        } catch (Exception e){
            Log.e(TAG,e.toString());
        }
        return resultIfError;
    }

    public static int readFile(String filePath, int resultIfError) {
        final String s = readFile(filePath,null);
        return TypeUtils.parseInt(s,resultIfError);
    }

    public static long readFile(String filePath, long resultIfError) {
        final String s = readFile(filePath,null);
        return TypeUtils.parseLong(s,resultIfError);
    }

    public static boolean extractAsset(Context context, String assetPath, String filePath) {
        File target = new File(filePath);
        if (target.exists() && !target.delete())
            return false;
        try {
            InputStream is = context.getAssets().open(assetPath);
            OutputStream os = new FileOutputStream(target);
            IoUtils.TransferThread.start(is,os);
        } catch (Exception e){
            Log.e(TAG,e.toString());
            return false;
        }
        return true;
    }

    public static boolean extractZipAsset(Context context, String assetPath, File destDir) {
        if (!destDir.exists() && !destDir.mkdirs())
            return false;
        try {
            ZipInputStream is = new ZipInputStream(context.getAssets().open(assetPath));
            ZipEntry ze;
            while (null != (ze = is.getNextEntry())){
                File out = new File(destDir,ze.getName());
                IoUtils.TransferThread.start(is,new FileOutputStream(out));
            }
            return true;
        } catch (Exception e){
            Log.e(TAG,e.toString());
            return false;
        }
    }

    public static String readAsset(Context context, String path, @Nullable String resultIfError){
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(path)));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                sb.append(mLine).append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG,"path="+path+" "+e.toString());
            return resultIfError;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG,"path="+path+" "+e.toString());
                    return resultIfError;
                }
            }
        }
        return sb.toString();
    }

    public static boolean copyFile(File src, File dstParent, @Nullable IoUtils.ProgressListener listener){
        if (!src.canRead() || !dstParent.isDirectory() || !dstParent.canWrite() )
            return false;
        final File newDst = new File(dstParent,src.getName());
        if (src.isDirectory()){
            final File[] childs;
            if (newDst.mkdir() && (childs = src.listFiles()) != null) {
                for (File child : childs){
                    copyFile(child,newDst,listener);
                }
                return true;
            }
            return false;
        } else {
            return IoUtils.streamCopy(src,newDst,listener);
        }
    }

    public static boolean copyFile(ContentResolver resolver, File src, Uri dest, @Nullable IoUtils.ProgressListener listener){
        if (src == null && !src.canRead())
            return false;
        final boolean isDir = src.isDirectory();
        final Uri newDest = createFile(resolver,dest,src.getName(),isDir);
        if (isDir){
            final File[] childs;
            if (newDest != null && (childs = src.listFiles()) != null){
                for (File child : childs){
                    copyFile(resolver,child,newDest,listener);
                }
                return true;
            }
            return false;
        }
        final OutputStream os;
        return newDest != null && (os = getOutputStream(resolver,newDest)) != null && IoUtils.streamCopy(src,os,listener);
    }

    private static Uri createFile(ContentResolver resolver, Uri parentDest, String name, boolean isDir){
        try {
            return DocumentsContract.createDocument(resolver,parentDest, isDir ? DocumentsContract.Document.MIME_TYPE_DIR : "", name);
        } catch (FileNotFoundException ignore) { }
        return null;
    }

    @Nullable
    private static OutputStream getOutputStream(@Nullable ContentResolver resolver, @Nullable Uri dst){
        try {
            return resolver == null || dst == null ? null : resolver.openOutputStream(dst);
        } catch (Exception ignore){ }
        return null;
    }

    public static boolean deleteFile(File src){
        if (!src.canWrite())
            return false;
        if (src.isDirectory()){
            final File[] childs = src.listFiles();
            if (childs != null)
            for (File f : childs){
                deleteFile(f);
            }
        }
        return src.delete();
    }

    public static String getNameWithoutExtension(String path) {
        final int idx = path.lastIndexOf('.');
        final int p = Math.max(0, path.lastIndexOf('/'));
        return idx > 0 && idx > p ? path.substring(p, idx) : path.substring(p);
    }

    public static File newFile(File file, FilenameBuilder filenameBuilder) {
        final String n = file.getName();
        final int i = n.lastIndexOf('.');
        final String name = i > 0 ? n.substring(0,i) : n;
        final String ext = i > 0 ? n.substring(i) : "";
        final String newFilename = filenameBuilder.buildNew(name,ext);
        return new File(file.getParentFile(),newFilename);
    }

    // logtest sz: 24119884507 bytes nio.filewalker java 7: 126 ms
    // logtest sz: 24119884507 bytes filerecursive : 101 ms
    // logtest sz: 24125898752 bytes shell du : 51 ms
    public static long getDirSizeInBytes(File file){
        if (true)
            return getFolderSizeDu(file);
        try {
            if (Build.VERSION.SDK_INT >= 26)
                return Files.walk(file.toPath()).mapToLong( p -> p.toFile().length() ).sum();
            Process p = new ProcessBuilder("du","-c",file.getAbsolutePath()).start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String total = "";
            for (String line; null != (line = r.readLine());){
                if (line.endsWith(" total"))
                    total = line.substring(0,line.indexOf(" total"));
            }
            r.close();
            if (total.length() > 0)
                return Long.parseLong(total);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static long getFolderSizeDu(File folder){
        if (folder != null && folder.exists() && folder.canRead()){
            try {
                Process p = new ProcessBuilder("du","-c",folder.getAbsolutePath()).start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String total = "";
                for (String line; null != (line = r.readLine());)
                    total = line;
                r.close();
                p.waitFor();
                if (total.length() > 0 && total.endsWith("total"))
                    return Long.parseLong(total.split("\\s+")[0]) * 1024;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return -1;
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    @TargetApi(26)
    private static long getDirSizeInBytes26(File file) {
        final AtomicLong size = new AtomicLong(0);
        try {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    if (exc != null)
                        Log.d(TAG,"getDirSize: could not traverse "+dir + " (" + exc + ")");
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }
        return size.get();
    }
}
