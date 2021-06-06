package pt.gu.utils;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SafUtils {

    @NonNull
    public static List<File> listFiles(Context context, Uri treeUri, String documentId) {
        List<File> fileList = new ArrayList<>();
        Uri saf = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId);
        if (saf != null) {
            try {
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(saf, "r");
                File p = new File(String.format(Locale.getDefault(), "/proc/%d/fd/%d", Process.myPid(), pfd.dup().getFd()));
                ArrayUtils.addAll(fileList, p.listFiles());
                pfd.close();
            } catch (IOException e) {
                Log.e("SafUtils", e.toString());
            }
        }
        return fileList;
    }
}
