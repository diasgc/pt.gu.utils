package pt.gu.utils;

import androidx.core.os.CancellationSignal;

public class Iutils {

    public interface Progress {

        String COMPLETE = "progress.complete";
        String ERROR = "progress.error";
        String CANCEL = "progress.cancel";

        void onUpdate(int len, String tag);
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

    public interface Validator<T> extends Callback<T>{

        boolean validate(T input);

    }
}
