package pt.gu.utils;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.core.os.CancellationSignal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class WebUtils {

    private static final String TAG = WebUtils.class.getSimpleName();

    public static void openPreview(Uri u, int headerSize, Iutils.Validator<byte[]> validator){
        IoUtils.HttpInputStream.openUrl(u, new IoUtils.HttpInputStream.Callback() {
            @Override
            public void onConnectionAvailable(IoUtils.HttpInputStream is) {
                byte[] hdrData;
                try {
                    if (is != null &&
                            is.read((hdrData = new byte[headerSize])) == headerSize &&
                            validator.test(hdrData)){
                        int remain = is.available();
                        byte[] data = new byte[headerSize + remain];
                        System.arraycopy(hdrData,0,data,0,headerSize);
                        boolean err = remain != is.read(data,headerSize, remain);
                        is.close();
                        validator.onResult(err ? null : data);
                    }
                } catch (Exception e) {
                    validator.onError(e);
                }
            }
        });
    }

    public static void join(OutputStream out, List<Uri> uris, boolean seq, CallbackHandler handler){
        if (seq)
            joinSeq(uris,out,handler);
        else
            joinSim(uris,out,handler);
    }

    private static void joinSeq(List<Uri> urls, OutputStream out, CallbackHandler handler){

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Uri u;
                String finalState = Iutils.Progress.COMPLETE;
                byte[] data;
                try {
                    for (int i = 0; i < urls.size() && !handler.isCancelled(); i++) {
                        IoUtils.HttpInputStream is = IoUtils.HttpInputStream.openUrl(u = urls.get(i));
                        // pass CancellationSignal to abort this thread
                        data = IoUtils.toByteArray(is, true, handler.getCancellationSignal());
                        handler.post(u.getLastPathSegment());
                        out.write(data);
                    }
                } catch (IOException e) {
                    // if one chunk is corrupted, we cancel this thread
                    handler.getCancellationSignal().cancel();
                    Log.e(TAG, e.toString());
                    finalState = Iutils.Progress.ERROR;
                }
                IoUtils.closeQuietly(true, out);
                handler.post(finalState);
            }
        });
    }

    /**
     * Downloads asynchronously, simultaneously multiple urls and write them together to outputstream by the given order specified in List<Uri>
     * @param urls List of Uris containing urls to download
     * @param out the OutputStream to be written
     * @param handler Progress callback and cancellation signal
     */
    private static void joinSim(List<Uri> urls, OutputStream out, CallbackHandler handler){

        final SparseArray<byte[]> chunks = new SparseArray<>();

        // this will download each url in one thread simultaneously
        for (int i = 0 ; i < urls.size() ; i++) {

            // break if progress gets cancelled or one of the threads throws an error
            if (handler.isCancelled()){
                handler.post(Iutils.Progress.CANCEL);
                break;
            }

            final Uri u = urls.get(i);
            final int idx = i;

            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    IoUtils.HttpInputStream is = IoUtils.HttpInputStream.openUrl(u);
                    try {
                        // pass CancellationSignal to abort all threads, if cancelled
                        byte[] data = IoUtils.toByteArray(is, true, handler.getCancellationSignal());
                        handler.post(u.getLastPathSegment());

                        synchronized (chunks) {
                            chunks.put(idx, data);

                            // if all chunks are downloaded, we can write them out by order
                            if (chunks.size() == urls.size()){
                                for (int i = 0 ; i < chunks.size(); i++){
                                    out.write(chunks.get(i));
                                }
                                IoUtils.closeQuietly(true,out);
                                handler.post(Iutils.Progress.COMPLETE);
                            }

                        }
                    } catch (IOException e) {
                        // if one chunk is corrupted, we cancel all execution threads
                        handler.getCancellationSignal().cancel();
                        Log.e(TAG, e.toString());
                    }
                }
            });
        }
    }

    @Nullable
    private static byte[] downloadData(String u, CallbackHandler listener){
        IoUtils.HttpInputStream is = IoUtils.HttpInputStream.openUrl(Uri.parse(u));
        try {
            byte[] data = IoUtils.toByteArray(is, true, listener.getCancellationSignal());
            listener.post(u);
            return data;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            listener.post(Iutils.Progress.ERROR);
        }
        return null;
    }
}
