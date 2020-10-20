package fybug.nulll.pdcache.memory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;

import fybug.nulll.pdcache.MapCacheOb;
import fybug.nulll.pdcache.MemoryMapCache;
import fybug.nulll.pdcache.err.CacheError;
import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdconcurrent.fun.tryBiConsumer;

/**
 * <h2>映射缓存.</h2>
 * <p>
 * 使用键来获取缓存的缓存工具，通过 {@link #put(K, V)} 来放入数据
 * <br/><br/>
 * 使用示例
 * <pre>使用普通的缓存数据
 *     public static
 *     void main(String[] args) throws Exception {
 *         MapCache&lt;String, Object&gt; cache = MapCache.build(String.class, Object.class)
 *                                                  // 使用弱引用
 *                                                  .refernce(WeakReference.class).build();
 *
 *         cache.put("asd", new Object());
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *         // 模拟回收
 *         System.gc();
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *         cache.put("asd", new Object());
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *     }
 * </pre>
 * <pre>使用实现于 CanClean 的缓存数据
 *     public static
 *     void main(String[] args) throws Exception {
 *         MapCache&lt;String, CanClean&gt; cache = MapCache.build(String.class, CanClean.class)
 *                                                    // 使用弱引用
 *                                                    .refernce(WeakReference.class).build();
 *
 *         cache.put("asd", new CanClean() {
 *             public @NotNull
 *             Runnable getclean() {
 *                 return () -> System.out.println("des:");
 *             }
 *         });
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *         // 模拟回收
 *         System.gc();
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *         cache.put("asd", new CanClean() {
 *             public @NotNull
 *             Runnable getclean() {
 *                 return () -> System.out.println("des:");
 *             }
 *         });
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *     }
 * </pre>
 *
 * @author fybug
 * @version 0.0.1
 * @since memory 0.0.1
 */
public
class MapCache<K, V> extends MemoryMapCache<K, V> {

    /** 构造缓存，指定缓存方式 */
    public
    MapCache(@NotNull Class<? extends Reference> refc) { super(refc); }

    /** 构造缓存，指定缓存方式和并发管理 */
    public
    MapCache(@NotNull Class<? extends Reference> refc, @NotNull SyLock syLock)
    { super(refc, syLock); }

    //----------------------------------------------------------------------------------------------

    /**
     * 获取缓存数据
     *
     * @param key 缓存的键
     *
     * @return 缓存数据
     */
    @Override
    @Nullable
    public
    V get(@NotNull K key) throws Exception, CacheError { return super.get(key); }

    @Override
    @Nullable
    public
    V get(@NotNull K key, @NotNull tryBiConsumer<K, V, Exception> run) throws Exception, CacheError
    { return super.get(key, run); }

    //--------------------------------

    /**
     * 放入新的缓存
     *
     * @param key 缓存的键
     * @param val 缓存的数据
     *
     * @return this
     */
    @NotNull
    public
    MapCache<K, V> put(@NotNull K key, @NotNull V val) throws Exception, CacheError {
        if (isClose())
            throw new CacheError();
        putdata(key, val);
        return this;
    }

    @Nullable
    protected
    V emptyData(@NotNull K key) { return null; }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取缓存构造工具
     *
     * @param <K> 键的类型
     * @param <V> 缓存内容的类型
     *
     * @return 构造工具
     */
    @NotNull
    public static
    <K, V> Build<K, V> build(Class<K> kc, Class<V> vc) {return new Build<>();}

    /**
     * <h2> {@link MapCache} 构造工具.</h2>
     * <ul>
     * <li>使用 {@link #refernce(Class)} 绑定缓存方式</li>
     * <li>使用 {@link #lockBy(SyLock)} 绑定并发管理</li>
     * <li>使用 {@link #build()} 进行构造</li>
     * </ul>
     *
     * @version 0.0.1
     * @since MapCache 0.0.1
     */
    public static final
    class Build<K, V> extends MapCacheOb.Build<K, V, Build<K, V>> {
        @NotNull
        public
        MapCache<K, V> build() { return new MapCache<>(refernce, lockBy); }
    }
}
