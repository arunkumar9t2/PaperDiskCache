package in.arunkumarsampath.paperdiskcache.internal;

import android.support.annotation.NonNull;

public class Util {
    public static <T> T requireNonNull(T obj, @NonNull String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }
}
