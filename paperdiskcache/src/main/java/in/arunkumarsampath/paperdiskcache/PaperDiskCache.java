package in.arunkumarsampath.paperdiskcache;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import in.arunkumarsampath.paperdiskcache.policy.SizePolicy;
import io.paperdb.Book;
import io.paperdb.Paper;

import static in.arunkumarsampath.paperdiskcache.internal.Util.requireNonNull;
import static java.lang.System.currentTimeMillis;

public class PaperDiskCache<T> implements DiskCache<T> {
    public static final String TAG = PaperDiskCache.class.getSimpleName();

    private final Book cacheBook;
    private final File paperDbFile;

    private final SizePolicy sizePolicy;

    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private final Object lock = new Object();
    private final Runnable cleanUpTask = new Runnable() {
        @Override
        public void run() {
            synchronized (lock) {
                final long maxSize = sizePolicy.size;
                final long currentSize = dirSize(paperDbFile);
                Log.d(TAG, String.format("max: %d, current %d", maxSize, currentSize));
                if (currentSize > maxSize) {
                    Log.d(TAG, "Attempting clean up");
                }
            }
        }
    };

    public PaperDiskCache(@NonNull Context context, @NonNull Class<T> clazz, @NonNull SizePolicy sizePolicy) {
        Paper.init(context);
        this.cacheBook = Paper.book(clazz.getName());
        this.paperDbFile = new File(context.getFilesDir() + File.separator + clazz.getName());
        this.sizePolicy = sizePolicy;
    }

    @Override
    public T put(@NonNull String key, T value) {
        requireNonNull(value, "value cannot be null");
        T writtenValue = putCacheEntry(key, new CacheEntry<>(value)).value;
        scheduleCleanUp();
        return writtenValue;
    }

    @NonNull
    private CacheEntry<T> putCacheEntry(@NonNull String key, @NonNull CacheEntry<T> cacheEntry) {
        cacheEntry.setLastModified(currentTimeMillis());
        cacheBook.write(key, cacheEntry);
        return cacheEntry;
    }

    @Override
    public T get(@NonNull String key) {
        final CacheEntry<T> cacheEntry = cacheBook.read(key);
        T readValue = putCacheEntry(key, cacheEntry).value;
        return readValue;
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
    public int count() {
        return cacheBook.getAllKeys().size();
    }

    @Override
    public boolean exists(@NonNull String key) {
        return cacheBook.exist(key);
    }

    private long dirSize(@NonNull File directory) {
        if (directory.exists()) {
            long size = 0;
            for (final File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    size += dirSize(file);
                } else {
                    size += file.length();
                }
            }
            return size;
        }
        return 0;
    }

    private void scheduleCleanUp() {
        executorService.submit(cleanUpTask);
    }
}
