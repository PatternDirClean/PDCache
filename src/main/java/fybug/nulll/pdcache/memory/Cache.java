package fybug.nulll.pdcache.memory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;

import fybug.nulll.pdcache.CacheOb;
import fybug.nulll.pdcache.MemoryCache;
import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdconcurrent.fun.tryConsumer;

/**
 * <h2>数据缓存工具.</h2>
 * <p>
 * 只缓存一个对象的缓存工具，通过 {@link #set(Object)} 修改缓存内容
 * <br/><br/>
 * 使用示例
 * <pre>使用普通的缓存数据
 *     public static
 *     void main(String[] args) throws Exception {
 *         Cache&lt;Object&gt; cache = Cache.build(Object.class)
 *                                    // 使用弱引用
 *                                    .refernce(WeakReference.class).build();
 *
 *         cache.set(new Object());
 *         cache.get(System.out::println);
 *
 *         // 模拟回收
 *         System.gc();
 *         cache.get(System.out::println);
 *         cache.set(new Object());
 *         cache.get(System.out::println);
 *     }
 * </pre>
 * <pre>使用实现于 CanClean 的缓存数据
 *     public static
 *     void main(String[] args) throws Exception {
 *         Cache&lt;Object&gt; cache = Cache.build(Object.class)
 *                                    // 使用弱引用
 *                                    .refernce(WeakReference.class).build();
 *
 *         // 使用 CanClean
 *         cache.set(new CanClean() {
 *             public @NotNull
 *             Runnable getclean() { return () -> System.out.println("des:"); }
 *         });
 *         cache.get(System.out::println);
 *
 *         // 模拟回收
 *         System.gc();
 *         cache.set(new CanClean() {
 *             public @NotNull
 *             Runnable getclean() { return () -> System.out.println("des:"); }
 *         });
 *         cache.get(System.out::println);
 *     }
 * </pre>
 *
 * @author fybug
 * @version 0.0.1
 * @since memory 0.0.1
 */
public
class Cache<V> extends MemoryCache<V> {

    /** 构造缓存，指定缓存方式 */
    public
    Cache(@NotNull Class<? extends Reference> refc) {super(refc); }

    /** 构造缓存，指定缓存方式和并发管理 */
    public
    Cache(@NotNull Class<? extends Reference> refc, @NotNull SyLock syLock) { super(refc, syLock); }

    //----------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public
    V get(@NotNull tryConsumer<@Nullable V, Exception> run) throws Exception
    { return super.get(run); }

    @Nullable
    @Override
    public
    V get() throws Exception { return super.get(); }

    @Override
    protected @Nullable
    V emptyData()
    { return null; }

    //-----------------------------------

    /**
     * 放入新的缓存
     *
     * @param v 缓存的数据
     *
     * @return this
     */
    @NotNull
    public
    Cache<V> set(@NotNull V v) throws Exception {
        putdata(v);
        return this;
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取缓存构造工具
     *
     * @param <V> 缓存内容的类型
     *
     * @return 构造工具
     */
    @NotNull
    public static
    <V> Build<V> build(Class<V> vc) {return new Build<>();}

    /**
     * <h2> {@link Cache} 构造工具.</h2>
     * <ul>
     * <li>使用 {@link #refernce(Class)} 绑定缓存方式</li>
     * <li>使用 {@link #lockBy(SyLock)} 绑定并发管理</li>
     * <li>使用 {@link #build()} 进行构造</li>
     * </ul>
     *
     * @version 0.0.1
     * @since Cache 0.0.1
     */
    public final static
    class Build<V> extends CacheOb.Build<V, Build<V>> {
        @NotNull
        public
        Cache<V> build() { return new Cache<>(refernce, lockBy); }
    }
}
