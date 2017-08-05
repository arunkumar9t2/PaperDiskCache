package in.arunkumarsampath.paperdiskcache;

class CacheEntry<T> {
    final T value;
    public long lastModified;

    CacheEntry(T value) {
        this.value = value;
    }
}
