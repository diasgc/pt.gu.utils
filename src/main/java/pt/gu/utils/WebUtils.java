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
                            validator.validate(hdrData)){
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

    public static void joinStreams(OutputStream out, Iutils.Progress p, List<Uri> uris, CancellationSignal cancel){

        final CallbackHandler ch = new CallbackHandler(Looper.getMainLooper(), new CallbackHandler.Callback() {
            @Override
            public void post(String msg) {
                p.onUpdate(0,msg);
            }

            @Override
            public boolean cancel() {
                return cancel.isCanceled();
            }
        });

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Iterator<Uri> it = uris.iterator();
                try {
                    String n;
                    byte[] data;
                    while (it.hasNext() && !cancel.isCanceled()) {
                        data = downloadData((n = it.next().toString()), p);
                        out.write(data);
                        ch.post(n);
                    }
                    IoUtils.closeQuietly(out);
                    ch.post(Iutils.Progress.COMPLETE);
                    p.onUpdate(0, Iutils.Progress.COMPLETE);
                } catch (IOException e){
                    Log.e(TAG,e.toString());
                    ch.post(Iutils.Progress.ERROR);
                }
            }
        });
    }

    private static void joinSeq(List<Uri> urls, OutputStream out, Iutils.Progress p, CancellationSignal cancel){

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Uri u;
                String finalState = Iutils.Progress.COMPLETE;
                byte[] data;
                try {
                    for (int i = 0; i < urls.size() && !cancel.isCanceled(); i++) {
                        IoUtils.HttpInputStream is = IoUtils.HttpInputStream.openUrl(u = urls.get(i));
                        // pass CancellationSignal to abort this thread
                        data = IoUtils.toByteArray(is, true, cancel);
                        p.onUpdate(data.length, u.getLastPathSegment());
                        out.write(data);
                    }
                } catch (IOException e) {
                    // if one chunk is corrupted, we cancel this thread
                    cancel.cancel();
                    Log.e(TAG, e.toString());
                    finalState = Iutils.Progress.ERROR;
                }
                IoUtils.closeQuietly(true, out);
                p.onUpdate(0, finalState);
            }
        });
    }

    /**
     * Downloads asynchronously, simultaneously multiple urls and write them together to outputstream by the given order specified in List<Uri>
     * @param urls List of Uris containing urls to download
     * @param out the OutputStream to be written
     * @param p Progress callback and cancellation signal
     */
    private static void joinSim(List<Uri> urls, OutputStream out, Iutils.Progress p, CancellationSignal cancel){

        final SparseArray<byte[]> chunks = new SparseArray<>();

        // this will download each url in one thread simultaneously
        for (int i = 0 ; i < urls.size() ; i++) {

            // break if progress gets cancelled or one of the threads throws an error
            if (cancel.isCanceled()){
                p.onUpdate(0,  Iutils.Progress.CANCEL);
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
                        byte[] data = IoUtils.toByteArray(is, true, cancel);
                        p.onUpdate(data.length, u.getLastPathSegment());

                        synchronized (chunks) {
                            chunks.put(idx, data);

                            // if all chunks are downloaded, we can write them out by order
                            if (chunks.size() == urls.size()){
                                for (int i = 0 ; i < chunks.size(); i++){
                                    out.write(chunks.get(i));
                                }
                                out.flush();
                                out.close();
                                p.onUpdate(0, Iutils.Progress.COMPLETE);
                            }

                        }
                    } catch (IOException e) {
                        // if one chunk is corrupted, we cancel all execution threads
                        cancel.cancel();
                        Log.e(TAG, e.toString());
                    }
                }
            });
        }
    }

    public static void join(List<String> urls, OutputStream out, IoUtils.ProgressListener listener){


        SparseArray<byte[]> segments = new SparseArray<>();
        for (int i = 0 ; i < urls.size() ; i++) {
            final String u = urls.get(i);
            final int j = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        segments.put(j, downloadData(u, listener));
                        if (segments.size() == urls.size()) {
                            for (int k = 0 ; k < segments.size() ; k++)
                                out.write(segments.get(k));
                            IoUtils.closeQuietly(out);
                            listener.onProgress(-1, null);
                        }
                    } catch (IOException e){
                        listener.onProgress(-2,null);
                    }
                }
            }).start();
        }
    }

    public static byte[] download(Uri uri){
        return DownloadTask.<byte[]>download(uri);
    }


    @Nullable
    private static byte[] downloadData(String u, Iutils.Progress listener){
        IoUtils.HttpInputStream is = IoUtils.HttpInputStream.openUrl(Uri.parse(u));
        try {
            byte[] data = IoUtils.toByteArray(is, true);
            listener.onUpdate(data.length, u);
            return data;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            listener.onUpdate(0, Iutils.Progress.ERROR);
        }
        return null;
    }

    @Nullable
    private static byte[] downloadData(String u, IoUtils.ProgressListener listener){
        HttpURLConnection connection;
        try {
            URL url = new URL(u);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            byte[] data = IoUtils.toByteArray(connection.getInputStream(), true);
            listener.onProgress(data.length, u);
            return data;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            listener.onProgress(-2, e.toString());
        }
        return null;
    }


    public interface ResultListener<T> {
        void onSuccess(T result);
        void onError();
    }

    interface DownloadResultListener<T> extends ResultListener<T> {
        T getResult(InputStream us);
    }

    static class DownloadTask<T> extends Thread implements Callable<T> {

        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Uri mUri;

        @Nullable
        public static <T> T download(Uri uri){
            DownloadTask<T> d = new DownloadTask<>(uri);
            Future<T> future = d.executor.submit((Callable<T>) d);
            try {
                d.executor.awaitTermination(5, TimeUnit.SECONDS);
                return future.get();
            } catch (ExecutionException | InterruptedException e){
                Log.e(TAG,e.toString());
                return null;
            } finally {
                d.executor.shutdown();
            }
        }

        public static <T> void downloadAsync(Uri uri, Function<InputStream,T> fn){
            DownloadTask<InputStream> d = new DownloadTask<>(uri);
            Future<InputStream> future = d.executor.submit((Callable<InputStream>) d);
            try {
                d.executor.awaitTermination(5, TimeUnit.SECONDS);
                fn.apply(future.get());
            } catch (ExecutionException | InterruptedException e){
                Log.e(TAG,e.toString());
                fn.apply(null);
            }
            d.executor.shutdown();
        }

        private DownloadTask(Uri uri){
            mUri = uri;
        }

        @Override
        public T call() throws Exception {
            URL url = new URL(mUri.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream is = connection.getInputStream();
            return null;
        }
    }
}
