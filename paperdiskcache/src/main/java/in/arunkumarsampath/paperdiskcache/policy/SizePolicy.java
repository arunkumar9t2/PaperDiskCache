package in.arunkumarsampath.paperdiskcache.policy;

public class SizePolicy implements Policy {
    public long size = Long.MAX_VALUE;

    public SizePolicy() {
    }

    public SizePolicy(long size) {
        this.size = size;
    }
}
