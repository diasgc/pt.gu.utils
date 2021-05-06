package pt.gu.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {

    public static class Builder {

        Object is;
        Object os;

        ProcessBuilder builder;
        List<String> args = new ArrayList<>();

        public Builder(String mainCmd){
            args.add(mainCmd);
            builder = new ProcessBuilder();
        }

        public Builder redirectInput(File file) throws FileNotFoundException {
            is = file;
            return this;
        }

        public Builder redirectInput(InputStream is){
            this.is = is;
            return this;
        }

        public Builder redirectInput(Context context, Uri uri) throws FileNotFoundException {
            if ("content".equals(uri.getScheme()))
                this.is = context.getContentResolver().openInputStream(uri);
            else if (StringUtils.equalsAny(uri.getScheme(),"http","https","ftp")){
                this.is = IoUtils.HttpInputStream.openUrl(uri);
            }
            return this;
        }

        public Builder setLdPath(String ldPath){
            builder.environment().put("LD_LIBRARY_PATH",ldPath);
            return this;
        }

        public Builder setLdPath(Context context){
            builder.environment().put("LD_LIBRARY_PATH",context.getApplicationInfo().nativeLibraryDir);
            return this;
        }

        public Builder setEnv(String var, String val){
            builder.environment().put(var, val);
            return this;
        }
    }
}
