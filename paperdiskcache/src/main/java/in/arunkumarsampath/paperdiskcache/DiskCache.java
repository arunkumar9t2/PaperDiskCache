package in.arunkumarsampath.paperdiskcache;

import android.support.annotation.NonNull;

import java.util.List;

public interface DiskCache<T> {
    T put(@NonNull String key, T value);

    T get(@NonNull String key);

    void remove(@NonNull String key);

    List<T> getAll();

    void clear();

    int count();

    boolean exists(@NonNull String key);

    void flush();
}
