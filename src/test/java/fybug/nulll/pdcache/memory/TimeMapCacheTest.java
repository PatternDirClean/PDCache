package fybug.nulll.pdcache.memory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fybug.nulll.pdcache.PDCache;

public
class TimeMapCacheTest {
    TimeMapCache<String, String> cache;

    @Before
    public
    void setUp() {
        cache = PDCache.TimeMapCache(String.class, String.class)
                       .dataTime(1000)
                       .scarrenNum(2)
                       .scarrentime(500)
                       .build();
    }

    @After
    public
    void tearDown() { cache.closeTimeTaskAndClear(); }

    // 获取过期测试
    @Test
    public
    void getTest() throws InterruptedException {
        cache.putData("a", "1", 2000);
        cache.putData("b", "2");
        cache.putData("c", "3");
        cache.putData("d", "4");
        cache.putData("e", "5");
        cache.putData("f", "6");

        Thread.sleep(1100);

        assert "1".equals(cache.getData("a"));
        assert null == cache.getData("b");
        assert null == cache.getData("c");
        assert null == cache.getData("d");
        assert null == cache.getData("e");
        assert null == cache.getData("f");
    }

    // 扫描线程测试
    @Test
    public
    void timeTest() throws InterruptedException {
        cache.putData("a", "1", 2000);
        cache.putData("b", "2");
        cache.putData("c", "3");
        cache.putData("d", "4");
        cache.putData("e", "5");
        cache.putData("f", "6");

        cache.getData("a");
        cache.getData("f");
        Thread.sleep(1500);

        cache.LOCK.read(() -> {
            assert null != cache.map.get("a");
            assert null == cache.map.get("b");
            assert null == cache.map.get("c");
            assert null != cache.map.get("f");
        });
    }
}