package pt.gu.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessUtils {

    public static final String LD_LIBRARY_PATH = "LD_LIBRARY_PATH";

    public static class Builder {

        Object in;
        Object out;

        ProcessBuilder builder;
        List<String> args = new ArrayList<>();

        public Builder(String mainCmd){
            args.add(mainCmd);
            builder = new ProcessBuilder();
        }

        public Builder cmd(String... cmds){
            if (cmds != null)
                args.addAll(Arrays.asList(cmds));
            return this;
        }

        public Builder input(File file) {
            in = file;
            return this;
        }

        public Builder input(InputStream is){
            this.in = is;
            return this;
        }

        public Builder input(Uri uri) {
            this.in = uri;
            return this;
        }

        public Builder output(File file) {
            this.out = file;
            return this;
        }

        public Builder output(InputStream is){
            this.out = is;
            return this;
        }

        public Builder output(Uri uri) {
            this.out = uri;
            return this;
        }

        public Builder setLdPath(String ldPath){
            return setEnv(LD_LIBRARY_PATH,ldPath);
        }

        public Builder setLdPath(Context context){
            return setEnv(LD_LIBRARY_PATH,context.getApplicationInfo().nativeLibraryDir);
        }

        public Builder setEnv(String var, String val){
            builder.environment().put(var, val);
            return this;
        }
    }
}
