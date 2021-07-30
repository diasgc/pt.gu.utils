package pt.gu.utils;

import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.core.os.CancellationSignal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class WebUtils {

    private static final String TAG = WebUtils.class.getSimpleName();
    private static final boolean DBG = false;

    public static void download(Uri uri, int probeSize, Function<byte[],Boolean> filter, OutputStream out, @Nullable CancellationSignal signal){
        IoUtils.HttpInputStream.openUrl(uri, (Consumer<IoUtils.HttpInputStream>) is -> {
            byte[] hdrData;
            try {
                if (is != null && is.read((hdrData = new byte[probeSize])) == probeSize &&
                        filter.apply(hdrData)){
                    out.write(hdrData);
                    IoUtils.streamCopy(Executors.newSingleThreadExecutor(), is, out, signal);
                    out.flush();
                    IoUtils.closeQuietly(is);
                }
            } catch (IOException e) {
                if (DBG) Log.e(TAG,e.toString());
            }
        });
    }

    @Deprecated
    public static void openPreview(Uri u, int headerSize, Iutils.Validator<byte[]> validator){
        IoUtils.HttpInputStream.openUrl(u, (Consumer<IoUtils.HttpInputStream>) is -> {
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
        });
    }

    public static void join(OutputStream out, List<Uri> uris, boolean seq, CallbackHandler handler){
        if (seq)
            joinSeq(uris,out,handler);
        else
            joinSim(uris,out,handler);
    }

    private static void joinSeq(List<Uri> urls, OutputStream out, CallbackHandler handler){

        Executors.newSingleThreadExecutor().execute(() -> {
            Uri u;
            String finalState = Iutils.Progress.COMPLETE;
            byte[] data;
            try {
                IoUtils.HttpInputStream is;
                for (int i = 0; i < urls.size() && !handler.isCancelled(); i++) {
                    is = IoUtils.HttpInputStream.openUrl(u = urls.get(i));
                    if (is != null) {
                        // pass CancellationSignal to abort this thread
                        data = IoUtils.toByteArray(is, true, handler.getCancellationSignal());
                        handler.println(u.getLastPathSegment());
                        out.write(data);
                        is.close();
                    }
                }
            } catch (IOException e) {
                // if one chunk is corrupted, we cancel this thread
                handler.getCancellationSignal().cancel();
                Log.e(TAG, e.toString());
                finalState = Iutils.Progress.ERROR;
            }
            IoUtils.closeQuietly(true, out);
            handler.println(finalState);
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
                handler.println(Iutils.Progress.CANCEL);
                break;
            }

            final Uri u = urls.get(i);
            final int idx = i;

            Executors.newSingleThreadExecutor().execute(() -> {
                IoUtils.HttpInputStream is = IoUtils.HttpInputStream.openUrl(u);
                if (is != null) {
                    try {
                        // pass CancellationSignal to abort all threads, if cancelled
                        byte[] data = IoUtils.toByteArray(is, true, handler.getCancellationSignal());
                        handler.println(u.getLastPathSegment());

                        synchronized (chunks) {
                            chunks.put(idx, data);

                            // if all chunks are downloaded, we can write them out by order
                            if (chunks.size() == urls.size()) {
                                for (int i1 = 0; i1 < chunks.size(); i1++) {
                                    out.write(chunks.get(i1));
                                }
                                IoUtils.closeQuietly(true, out);
                                handler.println(Iutils.Progress.COMPLETE);
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
    public static byte[] downloadData(String u, @Nullable CallbackHandler listener){
        IoUtils.HttpInputStream is = IoUtils.HttpInputStream.openUrl(Uri.parse(u));
        if (is != null) {
            try {
                if (listener != null) {
                    byte[] data = IoUtils.toByteArray(is, true, listener.getCancellationSignal());
                    listener.println(Iutils.Progress.COMPLETE);
                    return data;
                } else {
                    return IoUtils.toByteArray(is, true);
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                if (listener != null)
                    listener.println(Iutils.Progress.ERROR);
            }
        }
        return null;
    }
}
