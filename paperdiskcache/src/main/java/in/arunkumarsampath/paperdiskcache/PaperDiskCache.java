package in.arunkumarsampath.paperdiskcache;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import in.arunkumarsampath.paperdiskcache.policy.SizePolicy;
import io.paperdb.Book;
import io.paperdb.Paper;

import static in.arunkumarsampath.paperdiskcache.internal.Util.requireNonNull;
import static java.lang.System.currentTimeMillis;

public class PaperDiskCache<T> implements DiskCache<T> {
    public static final String TAG = PaperDiskCache.class.getSimpleName();

    private final Book cacheBook;
    private final File cacheDirectory;

    private final SizePolicy sizePolicy;

    private final Object lock = new Object();

    protected final ThreadPoolExecutor executorService = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private AtomicBoolean autoCleanupEnabled = new AtomicBoolean(true);

    private final Runnable cleanUpTask = () -> {
        if (autoCleanupEnabled.get()) {
            synchronized (lock) {
                trimToSize();
            }
        }
    };

    public PaperDiskCache(@NonNull Context context, @NonNull Class<T> clazz, @NonNull SizePolicy sizePolicy) {
        Paper.init(context);
        this.cacheBook = Paper.book(clazz.getName());
        this.cacheDirectory = new File(context.getFilesDir() + File.separator + clazz.getName());
        this.sizePolicy = sizePolicy;
    }

    public boolean isAutoCleanupEnabled() {
        return autoCleanupEnabled.get();
    }

    public void setAutoCleanupEnabled(boolean enabled) {
        if (autoCleanupEnabled.compareAndSet(!enabled, enabled)) {
            scheduleCleanUp();
        }
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
        cacheEntry.lastModified = currentTimeMillis();
        cacheBook.write(key, cacheEntry);
        return cacheEntry;
    }

    private CacheEntry<T> readCacheEntry(@NonNull String key) {
        return cacheBook.read(key);
    }

    @Override
    public T get(@NonNull String key) {
        final CacheEntry<T> cacheEntry = readCacheEntry(key);
        return putCacheEntry(key, cacheEntry).value;
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
    public boolean contains(@NonNull String key) {
        return cacheBook.contains(key);
    }

    @Override
    public void flush() {
        trimToSize();
    }

    private void trimToSize() {
        final long maxSize = sizePolicy.size;
        final long currentSize = size();
        Log.d(TAG, String.format("max: %d, current %d", maxSize, currentSize));
        if (currentSize > maxSize) {
            Log.d(TAG, "Attempting clean up");
            final List<String> keys = cacheBook.getAllKeys();
            final TreeMap<Long, String> sortedKeyMap = new TreeMap<>();
            for (final String key : keys) {
                sortedKeyMap.put(readCacheEntry(key).lastModified, key);
            }
            while (size() > maxSize) {
                final Map.Entry<Long, String> firstEntry = sortedKeyMap.pollFirstEntry();
                final String keyToEvict = firstEntry.getValue();
                Log.d(TAG, "Evicting : " + keyToEvict);
                remove(keyToEvict);
            }
        }
    }

    private long size() {
        return dirSize(cacheDirectory);
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
