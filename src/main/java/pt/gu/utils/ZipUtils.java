package pt.gu.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.os.CancellationSignal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("unused")
public class ZipUtils {

    private static final String TAG = ZipUtils.class.getSimpleName();
    private static final boolean DBG = false;


    public static void exampleZipFileSys(File zip, File add){
        /* Define ZIP File System Properies in HashMap */
        Map<String, String> zip_properties = new HashMap<>();
        /* set create to true if you want to create a new ZIP file */
        zip_properties.put("create", "true");
        /* specify encoding to UTF-8 */
        zip_properties.put("encoding", "UTF-8");
        /* Locate File on disk for creation */
        URI zip_disk = URI.create("jar:file:/"+zip.getAbsolutePath());
        /* Create ZIP file System */
        try (FileSystem zipfs = FileSystems.newFileSystem(zip_disk, zip_properties)) {
            /* Path to source file */
            Path file_to_zip = add.toPath();
            /* Path inside ZIP File */
            Path pathInZipfile = zipfs.getPath(add.getName());
            /* Add file to archive */
            Files.copy(file_to_zip,pathInZipfile);
        } catch (IOException e) {
            if (DBG) Log.e(TAG,e.toString());
        }
    }

    public static boolean extract(File zip, File dirDest){
        ZipFile z = null;
        try {
            z = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = z.entries();
            final CancellationSignal signal = new CancellationSignal();
            File fileDest;
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                fileDest = new File(dirDest,ze.getName());
                if (ze.isDirectory()){
                    if (fileDest.mkdirs())
                        continue;
                    throw new IOException("Could not create dir "+fileDest.getAbsolutePath());
                }
                FileOutputStream fos = new FileOutputStream(fileDest);
                IoUtils.streamCopy(z.getInputStream(ze),fos,signal);
                IoUtils.closeQuietly(true,fos);
            }
            return true;
        } catch (IOException e){
            return false;
        } finally {
            IoUtils.closeQuietly(z);
        }
    }

    private static boolean addDir(@NonNull ZipOutputStream zos, @NonNull String rootPath, @NonNull File dir, CancellationSignal signal){
        final File[] lst = dir.listFiles();
        if (lst != null){
            for (File f : lst){
                if (f.isDirectory())
                    addDir(zos, rootPath, f, signal);
                else {
                    String fname = f.getAbsolutePath().substring(rootPath.length() + 1);
                    try {
                        zos.putNextEntry(new ZipEntry(fname));
                        IoUtils.streamCopy(new FileInputStream(f), zos, signal);
                        zos.closeEntry();
                        return true;
                    } catch (IOException e){
                        if (DBG) Log.e(TAG,e.toString());
                    }
                }
            }
        }
        return false;
    }

    public static boolean archive(File dirDest, File zip){
        try {
            final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
            final CancellationSignal signal = new CancellationSignal();
            boolean ret = addDir(zos,dirDest.getAbsolutePath(), dirDest, signal);
            zos.close();
            return ret;
        } catch (IOException e){
            return false;
        }
    }
}
