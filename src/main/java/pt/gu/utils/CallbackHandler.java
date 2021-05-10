package pt.gu.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Printer;

import androidx.core.os.CancellationSignal;

public class CallbackHandler extends Handler implements Runnable, Printer {

    public interface Callback extends Printer, CancellationSignal.OnCancelListener {

        String ON_START = "CallbackHandler.onStart";
        String ON_SUCCESS = "CallbackHandler.onSuccess";
        String ON_ERROR = "CallbackHandler.onError";
        String ON_CANCEL = "CallbackHandler.onCancel";

    }

    private CancellationSignal signal;
    private Callback callback;
    private String msg;

    public CallbackHandler(Callback callback){
        super(Looper.getMainLooper());
        this.callback = callback;
        signal = new CancellationSignal();
        signal.setOnCancelListener(callback);
    }

    public CallbackHandler(Looper looper, Callback callback){
        super(looper);
        this.callback = callback;
        signal = new CancellationSignal();
    }

    public CallbackHandler with(CancellationSignal signal){
        this.signal = signal;
        this.signal.setOnCancelListener(callback);
        return this;
    }

    @Override
    public void println(String message){
        this.msg = message;
        post(this);
    }

    public Callback getCallback(){
        return callback;
    }

    @Override
    public void run() {
        if (callback != null)
            callback.println(msg);
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