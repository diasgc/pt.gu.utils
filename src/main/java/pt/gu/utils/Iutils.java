package pt.gu.utils;

public class Iutils {

    public interface Progress {

        String COMPLETE = "progress.complete";
        String ERROR = "progress.error";
        String CANCEL = "progress.cancel";

        void onUpdate(int len, String tag);
        boolean cancel();
    }
}
