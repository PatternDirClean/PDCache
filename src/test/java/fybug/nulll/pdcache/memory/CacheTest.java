package fybug.nulll.pdcache.memory;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.ref.WeakReference;

import fybug.nulll.pdcache.CanClean;
import fybug.nulll.pdcache.PDCache;

import static fybug.nulll.pdcache.RunTest.check;
import static fybug.nulll.pdcache.RunTest.destruction;
import static fybug.nulll.pdcache.RunTest.from;
import static fybug.nulll.pdcache.RunTest.init;
import static fybug.nulll.pdcache.RunTest.to;

public
class CacheTest {
    private Cache<Object> cache;

    @Before
    public
    void setUp() {
        init();
        cache = PDCache.Cache(Object.class).refernce(WeakReference.class).build();
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
            Runnable getclean() { return () -> to.println("des:"); }
        };

        from.println(o);
        cache.set(o);
        cache.get(to::println);

        o = null;
        System.gc();
        from.println("des:");
        from.println("null");
        cache.get(to::println);

        o = new CanClean() {
            public @NotNull
            Runnable getclean() { return () -> {}; }
        };

        from.println(o);
        cache.set(o);
        cache.get(to::println);

        check();
    }
}