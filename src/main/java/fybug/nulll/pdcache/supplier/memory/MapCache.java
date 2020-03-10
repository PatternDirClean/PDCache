package fybug.nulll.pdcache.supplier.memory;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import fybug.nulll.pdcache.CacheGcThrea;
import fybug.nulll.pdcache.CanClean;
import fybug.nulll.pdcache.err.CacheError;
import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdconcurrent.fun.tryBiConsumer;
import fybug.nulll.pdconcurrent.fun.tryFunction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <h2>映射缓存.</h2>
 * <p>
 * 使用键来获取缓存的缓存工具<br/>
 * 缓存关联的是映射的值，与键无关，在值对象达到回收条件时对应的键会从缓存中移除<br/>
 * 缓存工具被关闭的时候会抛出 {@link CacheError}
 * <br/><br/>
 * 使用示例
 * <pre>使用普通的缓存数据
 *     public static
 *     void main(String[] args) throws Exception {
 *         MapCache&lt;String, Object&gt; cache = MapCache.build(String.class, Object.class)
 *                                                  // 数据生产接口
 *                                                  .createdata(k -> new Object())
 *                                                  // 使用弱引用
 *                                                  .refernce(WeakReference.class).build();
 *
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *         // 模拟回收
 *         System.gc();
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *     }
 * </pre>
 * <pre>使用实现于 CanClean 的缓存数据
 *     public static
 *     void main(String[] args) throws Exception {
 *         MapCache<String, CanClean> cache = MapCache.build(String.class, CanClean.class)
 *                                                  // 数据生产接口
 *                                                  .createdata(k -> new CanClean() {
 *                                                      public @NotNull
 *                                                      Runnable getclean() { return () -> System.out.println("des:"); }
 *                                                  })
 *                                                  // 使用弱引用
 *                                                  .refernce(WeakReference.class).build();
 *
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *         // 模拟回收
 *         System.gc();
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *     }
 * </pre>
 *
 * @author fybug
 * @version 0.0.3
 * @since memory 0.0.1
 */
public abstract
class MapCache<K, V> implements Closeable {
    // 缓存引用类型
    protected final Class<? extends Reference<V>> refClass;
    // 数据缓存区
    protected final Map<K, Reference<V>> map = new HashMap<>();

    // 回收接口存放区
    protected final Map<K, Cleaner.Cleanable> cleanableMap = new HashMap<>();

    // 并发管理
    protected final SyLock LOCK;

    //----------------------------------------------------------------------------------------------

    /**
     * 构造缓存，指定缓存方式
     *
     * @since 0.0.2
     */
    public
    MapCache(@NotNull Class<? extends Reference> refc) { this(refc, SyLock.newRWLock()); }

    /**
     * 构造缓存，指定缓存方式和并发管理
     *
     * @since 0.0.3
     */
    public
    MapCache(@NotNull Class<? extends Reference> refc, @NotNull SyLock syLock) {
        refClass = (Class<Reference<V>>) refc;
        LOCK = syLock;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 获取缓存数据
     *
     * @param key 缓存的键
     *
     * @return 缓存数据
     */
    @NotNull
    public
    V get(@NotNull K key) throws Exception, CacheError {
        if (isClose())
            throw new CacheError();

        V item;
        final Reference<V>[] ref = new Reference[1];

        // get ref
        LOCK.tryread(Exception.class, () -> ref[0] = map.get(key));

        // 校验缓存
        if (ref[0] == null)
            // 锁定缓存，尝试重建
            item = LOCK.trywrite(Exception.class, () -> {

                /* 影逝二度 */
                if ((ref[0] = map.get(key)) == null) {
                    // 被彻底释放，重建缓存
                    var it = cachedata(key);
                    ref[0] = map.get(key);
                    return it;
                }

                // 缓存已重建
                return ref[0].get();
            });
        else
            item = ref[0].get();

        // 正在被回收，等待释放
        while( (item == null || ref[0].isEnqueued()) && map.get(key) == ref[0] )
            ;
        if (item == null)
            // 递归获取新的缓存
            return get(key);

        return item;
    }

    /**
     * 使用缓存的内容运行
     * <p>
     * 可以避免没有给予缓存对象强引用而导致缓存丢失的情况
     *
     * @param key 缓存的键
     * @param run 使用缓存的回调，传入 param key 和 {@link #get(K)}
     *
     * @return 缓存数据
     *
     * @see #get(K)
     */
    @NotNull
    public final
    V get(@NotNull K key, @NotNull tryBiConsumer<K, V, Exception> run) throws Exception, CacheError
    {
        var cache = get(key);
        run.accept(key, cache);
        return cache;
    }

    //--------------------------------

    /** 创建新的数据 */
    @NotNull
    protected abstract
    V createData(@NotNull K key) throws Exception;

    /**
     * 生成新的缓存
     * <p>
     * 使用 {@link #createData(K)} 创建的数据生成
     *
     * @return 当前缓存的数据
     */
    @NotNull
    protected final
    V cachedata(@NotNull K key) throws Exception, CacheError {
        if (isClose())
            throw new CacheError();

        // 生成新的数据
        var v = createData(key);

        /* 获取对象的回收方法 */
        Runnable clean;
        if (v instanceof CanClean)
            clean = ((CanClean) v).getclean();
        else
            clean = null;

        /* 注册回收方法 */
        cleanableMap.put(key, CacheGcThrea.binClean(v, () -> LOCK.write(() -> {
            if (clean != null)
                clean.run();
            cleanableMap.remove(key);
            map.remove(key);
        })));

        // 放入缓存
        map.put(key, refClass.getConstructor(Object.class).newInstance(v));
        return v;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 移除缓存
     * <p>
     * 强制释放缓存内容<br/>
     * 将缓存内容主动加入回收队列
     *
     * @param key 要释放的键
     */
    public
    void removeData(@NotNull K key) {
        LOCK.write(() -> {
            var ref = map.remove(key);
            var c = cleanableMap.get(key);

            if (ref == null || ref.isEnqueued())
                return;
            if (c != null)
                c.clean();
            ref.clear();
        });
    }

    //----------------------------------------------------------------------------------------------

    // 是否被关闭
    @Getter private volatile boolean isClose = false;

    @Override
    public
    void close() {
        if (isClose())
            return;
        isClose = true;

        // 释放
        LOCK.write(() -> {
            map.values().forEach(v -> {
                // 对象被释放
                if (v.isEnqueued() || v.get() == null)
                    return;
                v.enqueue();
            });
            map.clear();
        });
    }

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
     * {@link MapCache} 构造工具
     * <p>
     * 使用 {@link #createdata(tryFunction)} 方法绑定数据生成接口<br/>
     * 使用 {@link #refernce(Class)} 绑定缓存方式<br/>
     * 使用 {@link #lockBy(SyLock)} 绑定并发管理<br/>
     * 使用 {@link #build()} 进行构造
     *
     * @version 0.0.3
     * @since MapCache 0.0.2
     */
    @Accessors( chain = true, fluent = true )
    public static final
    class Build<K, V> {
        @Setter private tryFunction<@NotNull K, @NotNull V, Exception> createdata;
        /** @since 0.0.2 */
        @Setter private Class<? extends Reference> refernce = SoftReference.class;
        /** @since 0.0.3 */
        @Setter private SyLock lockBy = SyLock.newRWLock();

        @NotNull
        public
        MapCache<K, V> build() {
            return new MapCache<>(refernce, lockBy) {
                protected @NotNull
                V createData(@NotNull K key) throws Exception { return createdata.apply(key); }
            };
        }
    }
}
