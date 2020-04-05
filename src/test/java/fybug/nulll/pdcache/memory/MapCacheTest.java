package fybug.nulll.pdcache.memory;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.ref.WeakReference;

import fybug.nulll.pdcache.CanClean;

import static fybug.nulll.pdcache.RunTest.check;
import static fybug.nulll.pdcache.RunTest.destruction;
import static fybug.nulll.pdcache.RunTest.from;
import static fybug.nulll.pdcache.RunTest.init;
import static fybug.nulll.pdcache.RunTest.to;

public
class MapCacheTest {
    private MapCache<String, Object> cache;

    @Before
    public
    void setUp() {
        init();
        cache = MapCache.build(String.class, Object.class).refernce(WeakReference.class).build();
    }

    @After
    public
    void tearDown() throws IOException {
        cache.clear();
        cache = null;
        destruction();
    }

    @Test
    public
    void cache() throws Exception {
        CanClean o = new CanClean() {
            public @NotNull
            Runnable getclean() {
                return () -> to.println("des:");
            }
        };

        from.println(o);
        cache.put("asd", o);
        cache.get("asd", (k, v) -> to.println(v));

        // 模拟回收
        o = null;
        System.gc();
        from.println("des:");
        from.println("null");
        cache.get("asd", (k, v) -> to.println(v));

        o = new CanClean() {
            public @NotNull
            Runnable getclean() {
                return () -> {};
            }
        };

        from.println(o);
        cache.put("asd", o);
        cache.get("asd", (k, v) -> to.println(v));

        check();
    }
}