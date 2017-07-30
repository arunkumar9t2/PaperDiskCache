package in.arunkumarsampath.paperdiskcache;

class CacheEntry<T> {
    final T value;
    private long lastModified;

    CacheEntry(T value) {
        this.value = value;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
