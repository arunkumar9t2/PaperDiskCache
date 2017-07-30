package in.arunkumarsampath.paperdiskcache.model;

import android.support.annotation.NonNull;

public class TestModel {
    public final String name;

    public TestModel(@NonNull String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestModel testModel = (TestModel) o;
        return name.equals(testModel.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "TestModel{" +
                "name='" + name + '\'' +
                '}';
    }
}
