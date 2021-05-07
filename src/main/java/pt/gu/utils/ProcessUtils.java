package pt.gu.utils;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessUtils {

    public static final String LD_LIBRARY_PATH = "LD_LIBRARY_PATH";

    public static class Builder {

        private Context context;
        Object in;
        Object out;
        Object err;

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

        public Builder stdIn(File file) {
            in = file;
            return this;
        }

        public Builder stdIn(InputStream is){
            this.in = is;
            return this;
        }

        public Builder stdIn(@Nullable Context context, Uri uri) {
            this.context = context;
            this.in = uri;
            return this;
        }

        public Builder stdOut(File file) {
            this.out = file;
            return this;
        }

        public Builder stdOut(InputStream is){
            this.out = is;
            return this;
        }

        public Builder stdErr(OutputStream err){
            this.err = err;
            return this;
        }

        public Builder output(@Nullable Context context, Uri uri) {
            this.context = context;
            this.out = uri;
            return this;
        }

        public Builder setLdPath(String ldPath){
            return setEnv(LD_LIBRARY_PATH,ldPath);
        }

        public Builder setLdPath(Context context){
            this.context = context;
            return setEnv(LD_LIBRARY_PATH,context.getApplicationInfo().nativeLibraryDir);
        }

        public Builder setEnv(String var, String val){
            builder.environment().put(var, val);
            return this;
        }

        public Process start() throws IOException {
            if (in != null){
                if (in instanceof File){
                    builder.redirectInput((File) in);
                } else if (in instanceof Uri){
                    if ("content".equals(((Uri) in).getScheme())){
                        if (context == null)
                            throw new IOException("Context not defined for "+in.toString());
                        in = context.getContentResolver().openInputStream((Uri) in);
                    } else if (StringUtils.equalsAny(((Uri) in).getScheme(),"http","https","ftp")){
                        in = IoUtils.HttpInputStream.openUrl((Uri) in);
                    }
                }
            }
            if (out != null){
                if (out instanceof File){
                    builder.redirectOutput((File) out);
                } else if (in instanceof Uri){
                    if ("content".equals(((Uri) out).getScheme())){
                        if (context == null)
                            throw new IOException("Context not defined for "+out.toString());
                        out = context.getContentResolver().openOutputStream((Uri) out);
                    } else if (StringUtils.equalsAny(((Uri) in).getScheme(),"http","https","ftp")){
                        out = IoUtils.HttpOutputStream.openUrl((Uri) out);
                    }
                }
            }
            Process p = builder.start();
            if (in instanceof InputStream)
                IoUtils.TransferThread.start((InputStream)in, p.getOutputStream());
            if (out instanceof OutputStream)
                IoUtils.TransferThread.start(p.getInputStream(), (OutputStream)out);
            if (err instanceof OutputStream)
                IoUtils.TransferThread.start(p.getErrorStream(), (OutputStream)err);
            return p;
        }
    }


}
