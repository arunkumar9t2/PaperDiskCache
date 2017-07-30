package in.arunkumarsampath.paperdiskcache;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import in.arunkumarsampath.paperdiskcache.policy.SizePolicy;
import io.paperdb.Book;
import io.paperdb.Paper;

import static in.arunkumarsampath.paperdiskcache.internal.Util.requireNonNull;

public class PaperDiskCache<T> implements DiskCache<T> {
    private final Book cacheBook;

    private final SizePolicy sizePolicy;

    public PaperDiskCache(@NonNull Context context, Class<T> clazz, @NonNull SizePolicy sizePolicy) {
        Paper.init(context);
        this.cacheBook = Paper.book(clazz.getName());
        this.sizePolicy = sizePolicy;
    }

    @Override
    public T put(@NonNull String key, T value) {
        requireNonNull(value, "value cannot be null");
        cacheBook.write(key, new CacheEntry<>(value));
        return value;
    }

    @Override
    public T get(@NonNull String key) {
        final CacheEntry<T> cacheEntry = cacheBook.read(key);
        return cacheEntry.value;
    }

    @Override
    public void remove(@NonNull String key) {
        cacheBook.delete(key);
    }

    @Override
    public List<T> getAll() {
        final List<String> keys = cacheBook.getAllKeys();
        final List<T> values = new ArrayList<>(keys.size());
        for (String key : keys) {
            values.add(get(key));
        }
        return values;
    }

    @Override
    public void clear() {
        cacheBook.destroy();
    }

    @Override
    public boolean exists(@NonNull String key) {
        return cacheBook.exist(key);
    }
}
