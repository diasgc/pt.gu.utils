package pt.gu.utils;

import android.os.Handler;
import android.os.Looper;

public class CallbackHandler extends Handler implements Runnable {

    public interface Callback {

        String ON_START = "ffmpeg.callback.onstart";
        String ON_SUCCESS = "ffmpeg.callback.onsuccess";
        String ON_ERROR = "ffmpeg.callback.onstart";
        String ON_CANCEL = "ffmpeg.callback.oncancel";

        void post(String msg);

        boolean cancel();

    }

    private Callback callback;
    private String msg;

    public CallbackHandler(Looper looper, Callback callback){
        super(looper);
        this.callback = callback;
    }

    public void post(String message){
        this.msg = message;
        post(this);
    }

    @Override
    public void run() {
        if (callback != null)
            callback.post(msg);
    }
}