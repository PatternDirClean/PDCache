package fybug.nulll.pdcache.supplier.memory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.ref.WeakReference;

import fybug.nulll.pdcache.supplier.suppilerTest;

import static fybug.nulll.pdcache.RunTest.check;
import static fybug.nulll.pdcache.RunTest.destruction;
import static fybug.nulll.pdcache.RunTest.from;
import static fybug.nulll.pdcache.RunTest.init;
import static fybug.nulll.pdcache.RunTest.to;
import static fybug.nulll.pdcache.supplier.suppilerTest.nextClean;

public
class SCacheTest {
    private SCache<Object> cache;

    @Before
    public
    void setUp() {
        init();
        cache = SCache.build(Object.class)
                      .createdata(suppilerTest::getNowClean)
                      .refernce(WeakReference.class)
                      .build();
    }

    @After
    public
    void tearDown() throws IOException {
        cache.clear();
        destruction();
    }

    @Test
    public
    void cache() throws Exception {
        from.println(nextClean());
        cache.get(to::println);

        from.println("des:");
        from.println(nextClean());
        System.gc();
        cache.get(to::println);

        check();
    }
}