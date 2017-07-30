package in.arunkumarsampath.paperdiskcache.policy;

public class SizePolicy implements Policy {
    public long size = -1;

    public SizePolicy() {
    }

    public SizePolicy(long size) {
        this.size = size;
    }
}
