package pt.gu.utils;

import androidx.core.os.CancellationSignal;
import androidx.core.util.Predicate;

public class Iutils {

    public interface Progress {

        String COMPLETE = "progress.complete";
        String ERROR = "progress.error";
        String CANCEL = "progress.cancel";

        void onUpdate(int progress, String tag);
    }

    public interface Available {
        void isAvailable(boolean result);
    }

    public interface Result<T> {

        void onResult(T result);

    }

    public interface Callback<T> extends Result<T> {

        void onError(Exception e);

    }

    public interface Validator<T> extends Predicate<T>, Callback<T> {

    }
}
