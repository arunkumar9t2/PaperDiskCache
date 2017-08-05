package in.arunkumarsampath.paperdiskcache;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import in.arunkumarsampath.paperdiskcache.model.TestModel;
import in.arunkumarsampath.paperdiskcache.policy.SizePolicy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class PaperDiskCacheTest {
    private PaperDiskCache<TestModel> cache;

    @Before
    public void setup() {
        cache = new PaperDiskCache<>(InstrumentationRegistry.getContext(), TestModel.class, new SizePolicy(300));
    }

    @After
    public void cleanup() {
        cache.clear();
    }

    @Test
    public void testPaperCacheAdd() throws Exception {
        cache.clear();

        final TestModel testModel = new TestModel("Something");
        cache.put(testModel.name, testModel);
        assertEquals(cache.get(testModel.name), testModel);
    }

    @Test
    public void testPaperCacheRemove() throws Exception {
        cache.clear();

        final TestModel testModel = new TestModel("Something");
        cache.put(testModel.name, testModel);
        cache.remove(testModel.name);
        assertEquals(0, cache.count());
        assertFalse(cache.exists(testModel.name));
    }
}
