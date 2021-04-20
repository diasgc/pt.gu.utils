package pt.gu.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class WebUtils {

    private static final String TAG = WebUtils.class.getSimpleName();


    private static DownloadResultListener<Bitmap> getBitmapListener(final ResultListener<Bitmap> listener) {
        return new DownloadResultListener<Bitmap>() {
            @Override
            public Bitmap getResult(InputStream input) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                return BitmapFactory.decodeStream(input, null, options);
            }

            @Override
            public void onSuccess(Bitmap result) {
                listener.onSuccess(result);
            }

            @Override
            public void onError() {
                listener.onError();
            }
        };
    }

    private static DownloadResultListener<String> getStringListener(final ResultListener<String> listener) {
        return new DownloadResultListener<String>() {
            @Override
            public String getResult(InputStream input) {
                return StringUtils.readStream(input);
            }

            @Override
            public void onSuccess(String result) {
                listener.onSuccess(result);
            }

            @Override
            public void onError() {
                listener.onError();
            }
        };
    }

    private static DownloadResultListener<byte[]> getBinListener(final ResultListener<byte[]> listener) {
        return new DownloadResultListener<byte[]>() {
            @Override
            public byte[] getResult(InputStream input) {
                try {
                    return IoUtils.toByteArray(input,true);
                } catch (IOException ex){
                  Log.e(TAG, ex.toString());
                }
                return new byte[0];
            }

            @Override
            public void onSuccess(byte[] result) {
                listener.onSuccess(result);
            }

            @Override
            public void onError() {
                listener.onError();
            }
        };
    }

    public static final int DOWNLOAD_SUCCESS = -1;
    public static final int DOWNLOAD_ERROR = -2;

    public static void joinStreams(OutputStream out, Iutils.Progress p, List<Uri> uris){

        final Handler handler = new Handler(Looper.getMainLooper());
        final ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(new Runnable() {
            @Override
            public void run() {
                Iterator<Uri> it = uris.iterator();
                try {
                    while (it.hasNext() && !p.cancel()) {
                        byte[] data = downloadData(it.next().toString(), p);
                        out.write(data);
                    }
                    IoUtils.closeQuietly(out);
                    p.onUpdate(0, Iutils.Progress.COMPLETE);
                } catch (IOException e){
                    Log.e(TAG,e.toString());
                    p.onUpdate(0, Iutils.Progress.ERROR);
                }
            }
        });


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
                        listener.onProgress(WebUtils.DOWNLOAD_ERROR,null);
                    }
                }
            }).start();
        }
    }

    @Nullable
    private static byte[] downloadData(String u, Iutils.Progress listener){
        HttpURLConnection connection;
        try {
            URL url = new URL(u);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            byte[] data = IoUtils.toByteArray(connection.getInputStream(), true);
            listener.onUpdate(data.length, u);
            return data;
        } catch (Exception e) {
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


    public static void getUrlPage(String url,final ResultListener<String> listener){
        Downloader<String> d = new Downloader<>(url, getStringListener(listener));
        d.execute();
    }

    public static void getUrlData(String url,final ResultListener<byte[]> listener){
        Downloader<byte[]> d = new Downloader<>(url, getBinListener(listener));
        d.execute();
    }

    public static void getFavIcon(String url,final ResultListener<Bitmap> listener){
        Downloader<Bitmap> d = new Downloader<>(url+"/favicon.ico",getBitmapListener(listener));
        d.execute();
    }

    public interface ResultListener<T> {
        void onSuccess(T result);
        void onError();
    }

    interface DownloadResultListener<T> extends ResultListener<T> {
        T getResult(InputStream us);
    }

    public static Bitmap downloadBitmap(Uri uri){
        final InputStream is = DownloadTask.download(uri);
        return BitmapFactory.decodeStream(is);
    }

    public static void downloadBitmap(Uri uri, ResultListener<Bitmap> listener){
        DownloadTask.downloadAsync(uri, new Function<InputStream, Bitmap>() {
            @Override
            public Bitmap apply(InputStream inputStream) {
                listener.onSuccess(BitmapFactory.decodeStream(inputStream));
                return null;
            }
        });
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

    static class Downloader<T> extends AsyncTask<Void,Void,T>{

        private String src;
        private DownloadResultListener<T> listener;

        public Downloader(String url, DownloadResultListener<T> listener){
            this.src = url;
            this.listener = listener;
        }

        @Override
        protected T doInBackground(Void... voids) {
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream is = connection.getInputStream();
                return listener.getResult(is);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable T result) {
            if (result != null)
                listener.onSuccess(result);
            else
                listener.onError();
        }
    }
}
