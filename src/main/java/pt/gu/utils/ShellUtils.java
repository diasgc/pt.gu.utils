package pt.gu.utils;

import android.util.Printer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class ShellUtils {

    public static abstract class ProcessPrinter extends PrintWriter {

        public static final String EOS = ProcessPrinter.class.getSimpleName() + ".EOS";
        public static final String ERR = ProcessPrinter.class.getSimpleName() + ".ERR";

        public ProcessPrinter(Process b, boolean stdErr, PrintWriter out){
            super(b.getOutputStream(),true);
            final BufferedReader br = new BufferedReader(new InputStreamReader(stdErr ? b.getErrorStream() : b.getInputStream()));
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    for (String s; (s = br.readLine()) != null;)
                        out.println(ERR + ":" +s);
                    b.waitFor();
                } catch (InterruptedException | IOException e){
                    out.println(ERR);
                    out.println(e.toString());
                }
                IoUtils.closeQuietly(br);
                out.println(EOS);
                b.destroy();
            });
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
    }
}
