package pt.gu.utils;

import android.util.Log;
import android.util.Printer;

import androidx.annotation.Nullable;
import androidx.core.os.CancellationSignal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class ShellUtils {

    private static final String TAG = ShellUtils.class.getSimpleName();
    private static final boolean DBG = false;

    public static class ProcessPrinter extends PrintWriter {

        public static final String EOS = ProcessPrinter.class.getSimpleName() + ".EOS";
        public static final String ERR = ProcessPrinter.class.getSimpleName() + ".ERR";

        public ProcessPrinter(Process b, @Nullable Printer stdOut, @Nullable Printer stdErr, @Nullable CancellationSignal signal){
            super(b.getOutputStream(),true);
            if (stdOut != null) {
                Executors.newSingleThreadExecutor().execute(() -> IoUtils.streamPrintSafe(b.getInputStream(), stdOut, signal, false));
            }
            if (stdErr != null){
                Executors.newSingleThreadExecutor().execute(() -> IoUtils.streamPrintSafe(b.getErrorStream(), stdErr, signal, false));
            }
        }
    }

    public static void exec(String cmd, boolean redirErr, Printer printer){
        ProcessBuilder b = new ProcessBuilder(cmd.split("\\s+")).redirectErrorStream(redirErr);
        try {
            Process p = b.start();
            StringUtils.streamPrint(p.getInputStream(), true, printer);
            p.waitFor();
        } catch (InterruptedException | IOException ignore){}
    }

    public static void exec(boolean redirErr, Printer printer, String format, Object... args){
        ProcessBuilder b = new ProcessBuilder(String.format(format, args).split("\\s+")).redirectErrorStream(redirErr);
        try {
            Process p = b.start();
            StringUtils.streamPrint(p.getInputStream(), true, printer);
            p.waitFor();
        } catch (InterruptedException | IOException ignore){}
    };

    public static void closeQuietly(Process p){
        if (p != null){
            try {
                p.waitFor();
            } catch (InterruptedException ignore){}
            p.destroy();
        }
    }
}
