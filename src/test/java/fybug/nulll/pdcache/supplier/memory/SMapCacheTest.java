package fybug.nulll.pdcache.supplier.memory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static fybug.nulll.pdcache.RunTest.check;
import static fybug.nulll.pdcache.RunTest.destruction;
import static fybug.nulll.pdcache.RunTest.from;
import static fybug.nulll.pdcache.RunTest.init;
import static fybug.nulll.pdcache.RunTest.to;
import static fybug.nulll.pdcache.supplier.suppilerTest.getNowClean;
import static fybug.nulll.pdcache.supplier.suppilerTest.nextClean;

public
class SMapCacheTest {
    private SMapCache<String, Object> cache;

    @Before
    public
    void setUp() {
        init();
        cache = SMapCache.build(String.class, Object.class)
                         .createdata((k) -> getNowClean())
                         .refernce(WeakReference.class)
                         .build();
    }

    @After
    public
    void tearDown() throws IOException {
        cache.close();
        cache = null;
        destruction();
    }

    @Test
    public
    void cache() throws Exception {

        from.println(nextClean());
        cache.get("asd", (k, v) -> to.println(v));

        // 模拟回收
        from.println("des:" + getNowClean());
        from.println(nextClean());
        System.gc();
        cache.get("asd", (k, v) -> to.println(v));

        check();
    }
}