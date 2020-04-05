package fybug.nulll.pdcache.supplier.memory;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import fybug.nulll.pdcache.CacheGcThrea;
import fybug.nulll.pdcache.CacheOb;
import fybug.nulll.pdcache.CanClean;
import fybug.nulll.pdcache.MemoryCache;
import fybug.nulll.pdcache.memory.Cache;
import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdconcurrent.fun.tryConsumer;
import fybug.nulll.pdconcurrent.fun.trySupplier;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <h2>自填充数据缓存工具.</h2>
 * <p>
 * 只缓存一个对象的缓存工具，需要指定数据填充方法，在没有数据的时候会进行自填充
 * <br/><br/>
 * 使用示例
 * <pre>使用普通的缓存数据
 *     public static
 *     void main(String[] args) throws Exception {
 *         SCache&lt;Object&gt; cache = SCache.build(Object.class)
 *                                    // 数据生产接口
 *                                    .createdata(Object::new)
 *                                    // 使用弱引用
 *                                    .refernce(WeakReference.class).build();
 *
 *         cache.get(System.out::println);
 *         // 模拟回收
 *         System.gc();
 *         cache.get(System.out::println);
 *     }
 * </pre>
 * <pre>使用实现于 CanClean 的缓存数据
 *     public static
 *     void main(String[] args) throws Exception {
 *         SCache&lt;CanClean&gt; cache = SCache.build(CanClean.class)
 *                                    // 数据生产接口，使用 CanClean
 *                                    .createdata(() -> new CanClean() {
 *                                        public @NotNull
 *                                        Runnable getclean() { return () -> System.out.println("des:"); }
 *                                    })
 *                                    // 使用弱引用
 *                                    .refernce(WeakReference.class).build();
 *
 *         cache.get(System.out::println);
 *         // 模拟回收
 *         System.gc();
 *         cache.get(System.out::println);
 *     }
 * </pre>
 *
 * @author fybug
 * @version 0.0.1
 * @see Cache
 * @since memory 0.0.1
 */
public abstract
class SCache<V> extends MemoryCache<V> {

    /**
     * 构造缓存，指定缓存方式
     *
     * @since 0.0.2
     */
    public
    SCache(@NotNull Class<? extends Reference> refc) {super(refc); }

    /**
     * 构造缓存，指定缓存方式和并发管理
     *
     * @since 0.0.3
     */
    public
    SCache(@NotNull Class<? extends Reference> refc, @NotNull SyLock syLock)
    { super(refc, syLock); }

    //----------------------------------------------------------------------------------------------

    @Override
    @NotNull
    public
    V get(@NotNull tryConsumer<V, Exception> run) throws Exception { return super.get(run); }

    @Override
    @NotNull
    public
    V get() throws Exception { return super.get(); }

    //-----------------------------------

    /** 创建新的数据 */
    @NotNull
    protected abstract
    V createData() throws Exception;

    /**
     * 生成新的缓存
     * <p>
     * 使用 {@link #createData()} 创建的数据生成
     */
    @Override
    protected @NotNull
    V emptyData() throws Exception {
        // 生成新的数据
        var v = createData();
        // 绑定缓存
        putdata(v);
        return v;
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
     * {@link SCache} 构造工具
     * <p>
     * 使用 {@link #createdata(trySupplier)} 绑定数据生成接口<br/>
     * 使用 {@link #refernce(Class)} 绑定缓存方式<br/>
     * 使用 {@link #lockBy(SyLock)} 绑定并发管理<br/>
     * 使用 {@link #build()} 进行构造
     *
     * @version 0.0.1
     * @since SCache 0.0.1
     */
    @Accessors( chain = true, fluent = true )
    public final static
    class Build<V> extends CacheOb.Build<V, Build<V>> {
        /** 数据生产接口 */
        @Setter private trySupplier<@NotNull V, Exception> createdata;

        @NotNull
        public
        SCache<V> build() {
            return new SCache<>(refernce, lockBy) {
                protected @NotNull
                V createData() throws Exception { return createdata.get(); }
            };
        }
    }
}
