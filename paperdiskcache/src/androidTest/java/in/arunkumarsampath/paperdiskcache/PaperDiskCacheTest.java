package in.arunkumarsampath.paperdiskcache;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import in.arunkumarsampath.paperdiskcache.model.TestModel;
import in.arunkumarsampath.paperdiskcache.policy.SizePolicy;

import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    private TestModel newTestModel() {
        return new TestModel(valueOf(currentTimeMillis()));
    }

    @Test
    public void testPaperCacheAdd() throws Exception {
        cache.clear();

        final TestModel testModel = newTestModel();
        cache.put(testModel.name, testModel);
        assertEquals(cache.get(testModel.name), testModel);
    }

    @Test
    public void testPaperCacheRemove() throws Exception {
        cache.clear();

        final TestModel testModel = newTestModel();
        cache.put(testModel.name, testModel);
        cache.remove(testModel.name);
        assertEquals(0, cache.count());
        assertFalse(cache.exists(testModel.name));
    }

    @Test
    public void testPaperCacheClear() throws Exception {
        cache.clear();

        final TestModel testModel = newTestModel();
        cache.put(testModel.name, testModel);
        cache.clear();
        assertEquals(0, cache.count());
        assertFalse(cache.exists(testModel.name));
    }

    @Test
    public void testEvictionAttemptedOnCachePut() throws Exception {
        cache.clear();
        cache.setAutoCleanupEnabled(true);

        final TestModel testModel = newTestModel();
        cache.put(testModel.name, testModel);
        assertEquals(1, cache.executorService.getQueue().size());
    }

    @Test
    public void testManualEvictionOnSizeExceeded() throws Exception {
        cache.clear();
        cache.setAutoCleanupEnabled(false);

        final TestModel testModel = newTestModel();
        cache.put(testModel.name, testModel);
        final TestModel testMode2 = newTestModel();
        cache.put(testMode2.name, testMode2);
        final TestModel testMode3 = newTestModel();
        cache.put(testMode3.name, testMode3);
        final TestModel testMode4 = newTestModel();
        cache.put(testMode4.name, testMode4);
        final TestModel testMode5 = newTestModel();
        cache.put(testMode5.name, testMode5);
        final TestModel testMode6 = newTestModel();
        cache.put(testMode6.name, testMode6);

        cache.flush();

        assertEquals(2, cache.count());
        assertTrue(cache.exists(testMode6.name));
        assertTrue(cache.exists(testMode5.name));
        assertTrue(!cache.exists(testMode4.name));
        assertTrue(!cache.exists(testMode3.name));
        assertTrue(!cache.exists(testMode2.name));
        assertTrue(!cache.exists(testModel.name));
    }

    @Test
    public void testAutoEvictionOnSizeExceeded() throws Exception {
        cache.clear();
        cache.setAutoCleanupEnabled(true);

        final TestModel testModel = newTestModel();
        cache.put(testModel.name, testModel);
        final TestModel testMode2 = newTestModel();
        cache.put(testMode2.name, testMode2);
        final TestModel testMode3 = newTestModel();
        cache.put(testMode3.name, testMode3);

        Thread.sleep(2000);
        assertTrue(cache.exists(testMode3.name));
        assertTrue(cache.exists(testMode2.name));
        assertTrue(!cache.exists(testModel.name));
    }
}

