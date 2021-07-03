package pt.gu.utils;

import android.util.Printer;

import java.io.IOException;

public class ShellUtils {

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
