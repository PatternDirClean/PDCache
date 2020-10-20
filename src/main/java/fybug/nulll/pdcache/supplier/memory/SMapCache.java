package fybug.nulll.pdcache.supplier.memory;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;

import fybug.nulll.pdcache.MapCacheOb;
import fybug.nulll.pdcache.MemoryMapCache;
import fybug.nulll.pdcache.err.CacheError;
import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdconcurrent.fun.tryBiConsumer;
import fybug.nulll.pdconcurrent.fun.tryFunction;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <h2>映射缓存.</h2>
 * <p>
 * 使用键来获取缓存的缓存工具，需要指定数据填充方法，在没有数据的时候会进行自填充
 * <br/><br/>
 * 使用示例
 * <pre>使用普通的缓存数据
 *     public static
 *     void main(String[] args) throws Exception {
 *         SMapCache&lt;String, Object&gt; cache = SMapCache.build(String.class, Object.class)
 *                                                    // 数据生产接口
 *                                                    .createdata(k -> new Object())
 *                                                    // 使用弱引用
 *                                                    .refernce(WeakReference.class).build();
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
 *         SMapCache&lt;String, Object&gt; cache = SMapCache.build(String.class, Object.class)
 *                                                    // 数据生产接口
 *                                                    .createdata(k -> new CanClean() {
 *                                                        public @NotNull
 *                                                        Runnable getclean() {
 *                                                            return () -> System.out.println("des:");
 *                                                        }
 *                                                    })
 *                                                    // 使用弱引用
 *                                                    .refernce(WeakReference.class).build();
 *
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *         // 模拟回收
 *         System.gc();
 *         cache.get("asd", (k, v) -> System.out.println(v));
 *     }
 * </pre>
 *
 * @author fybug
 * @version 0.0.1
 * @since memory 0.0.1
 */
public abstract
class SMapCache<K, V> extends MemoryMapCache<K, V> {

    /** 构造缓存，指定缓存方式 */
    public
    SMapCache(@NotNull Class<? extends Reference> refc) { super(refc); }

    /** 构造缓存，指定缓存方式和并发管理 */
    public
    SMapCache(@NotNull Class<? extends Reference> refc, @NotNull SyLock syLock)
    { super(refc, syLock); }

    //----------------------------------------------------------------------------------------------

    @Override
    @NotNull
    public
    V get(@NotNull K key) throws Exception, CacheError { return super.get(key); }

    @Override
    @NotNull
    public
    V get(@NotNull K key, @NotNull tryBiConsumer<K, V, Exception> run) throws Exception, CacheError
    { return super.get(key, run); }

    //--------------------------------

    /** 创建新的数据 */
    @NotNull
    protected abstract
    V createData(@NotNull K key) throws Exception;

    @NotNull
    protected final
    V emptyData(@NotNull K key) throws Exception, CacheError {
        if (isClose())
            throw new CacheError();

        // 生成新的数据
        var v = createData(key);
        putdata(key, v);
        return v;
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
     * <h2> {@link SMapCache} 构造工具.</h2>
     * <ul>
     * <li>使用 {@link #createdata(tryFunction)} 方法绑定数据生成接口</li>
     * <li>使用 {@link #refernce(Class)} 绑定缓存方式</li>
     * <li>使用 {@link #lockBy(SyLock)} 绑定并发管理</li>
     * <li>使用 {@link #build()} 进行构造</li>
     * </ul>
     *
     * @version 0.0.1
     * @since SMapCache 0.0.1
     */
    @Accessors( chain = true, fluent = true )
    public static final
    class Build<K, V> extends MapCacheOb.Build<K, V, Build<K, V>> {
        /** 数据生成接口 */
        @Setter private tryFunction<@NotNull K, @NotNull V, Exception> createdata;

        @NotNull
        public
        SMapCache<K, V> build() {
            return new SMapCache<>(refernce, lockBy) {
                protected @NotNull
                V createData(@NotNull K key) throws Exception { return createdata.apply(key); }
            };
        }
    }
}
