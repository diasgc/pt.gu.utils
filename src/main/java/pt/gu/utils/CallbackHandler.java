package pt.gu.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.CancellationSignal;

public class CallbackHandler extends Handler implements Runnable {

    public interface Callback {

        String ON_START = "ffmpeg.callback.onstart";
        String ON_SUCCESS = "ffmpeg.callback.onsuccess";
        String ON_ERROR = "ffmpeg.callback.onstart";
        String ON_CANCEL = "ffmpeg.callback.oncancel";

        void post(String msg);

        boolean cancel();

    }

    private CancellationSignal signal;
    private Callback callback;
    private String msg;

    public CallbackHandler(Callback callback){
        super(Looper.getMainLooper());
        this.callback = callback;
        signal = new CancellationSignal();
    }

    public CallbackHandler(Looper looper, Callback callback){
        super(looper);
        this.callback = callback;
        signal = new CancellationSignal();
    }

    public CallbackHandler with(CancellationSignal signal){
        this.signal = signal;
        return this;
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

    public void cancel(){
        signal.cancel();
    }

    public boolean isCancelled(){
        return signal.isCanceled();
    }

    public CancellationSignal getCancellationSignal(){
        return signal;
    }
}