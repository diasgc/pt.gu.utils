package pt.gu.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {

    private static final String TAG = BitmapUtils.class.getSimpleName();
    private static final boolean DBG = false;

    public static boolean recompress(File src, File dst, Bitmap.CompressFormat dstFormat, int qual, float resize){
        Bitmap b = BitmapFactory.decodeFile(src.getAbsolutePath());
        if (b != null) {
            if (resize > 0)
                b = Bitmap.createScaledBitmap(b, (int) (b.getWidth() * resize), (int) (b.getHeight() * resize), true);
            try {
                b.compress(dstFormat, qual, new FileOutputStream(dst));
                return true;
            } catch (IOException e){
                if (DBG) Log.e(TAG,e.toString());
            }
        }
        return false;
    }
}
