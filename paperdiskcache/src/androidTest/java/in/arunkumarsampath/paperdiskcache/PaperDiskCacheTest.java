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

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PaperDiskCacheTest {
    private PaperDiskCache<TestModel> cache;

    @Before
    public void setup() {
        cache = new PaperDiskCache<>(InstrumentationRegistry.getContext(), TestModel.class, new SizePolicy());
    }

    @After
    public void cleanup() {
        cache.clear();
    }

    @Test
    public void testPaperCacheAdd() {
        final TestModel testModel = new TestModel("Something");
        cache.put(testModel.name, testModel);
        assertEquals(cache.get(testModel.name), testModel);
    }
}
