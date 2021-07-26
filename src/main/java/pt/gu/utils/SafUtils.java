package pt.gu.utils;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class SafUtils {

    public static final String AUTHORITY = "com.android.externalstorage.documents";
    private static final String TAG = SafUtils.class.getSimpleName();
    private static final boolean DBG = true;

    @Nullable
    public static File resolveFile(Context context, Uri uri){
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            File p = new File(getProcFileFd(pfd.dup().getFd()));
            Path p2 = p.toPath().toRealPath();
            pfd.close();
            return p2.toFile();
        } catch (IOException e) {
            if (DBG) Log.e(TAG,e.toString());
        }
        return null;
    }

    public static String getProcFileFd(int fd){
        return String.format(Locale.getDefault(), "/proc/%d/fd/%d", Process.myPid(),fd);
    }

    @NonNull
    public static List<File> listFiles(Context context, Uri treeUri, File parent, String documentId) {
        List<File> fileList = new ArrayList<>();
        Uri saf = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId);
        if (saf != null) {
            try {
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(saf, "r");
                File p = new File(getProcFileFd(pfd.dup().getFd()));
                String[] flist = p.list();
                if (flist != null && flist.length > 0) {
                    for (String f : flist)
                        fileList.add(new File(parent, f));
                }
                pfd.close();
            } catch (IOException e) {
                Log.e("SafUtils", e.toString());
            }
        }
        return fileList;
    }
}
