package fybug.nulll.pdcache.supplier.memory;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import fybug.nulll.pdcache.CacheGcThrea;
import fybug.nulll.pdcache.CanClean;
import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdconcurrent.fun.tryConsumer;
import fybug.nulll.pdconcurrent.fun.trySupplier;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <h2>数据缓存工具.</h2>
 * <p>
 * 只缓存一个对象的缓存工具
 * <br/><br/>
 * 使用示例
 * <pre>使用普通的缓存数据
 *     public static
 *     void main(String[] args) throws Exception {
 *         Cache&lt;Object&gt; cache = Cache.build(Object.class)
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
 *         Cache&lt;CanClean&gt; cache = Cache.build(CanClean.class)
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
 * @version 0.0.3
 * @since memory 0.0.1
 */
public abstract
class Cache<V> {
    // 缓存引用类型
    protected final Class<? extends Reference<V>> refClass;
    // 缓存引用
    protected volatile Reference<V> cache = new WeakReference<>(null);

    // 数据回收接口
    protected volatile Cleaner.Cleanable cleanable = null;

    // 并发管理
    protected final SyLock LOCK;

    //----------------------------------------------------------------------------------------------

    /**
     * 构造缓存，指定缓存方式
     *
     * @since 0.0.2
     */
    public
    Cache(@NotNull Class<? extends Reference> refc) {this(refc, SyLock.newRWLock()); }

    /**
     * 构造缓存，指定缓存方式和并发管理
     *
     * @since 0.0.3
     */
    public
    Cache(@NotNull Class<? extends Reference> refc, @NotNull SyLock syLock) {
        this.refClass = (Class<Reference<V>>) refc;
        LOCK = syLock;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 使用缓存的内容运行
     * <p>
     * 可以避免没有给予缓存对象强引用而导致缓存丢失的情况
     *
     * @param run 使用缓存的回调
     *
     * @return V
     *
     * @see #get()
     */
    @NotNull
    public
    V get(@NotNull tryConsumer<V, Exception> run) throws Exception {
        var i = get();
        run.accept(i);
        return i;
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @NotNull
    public
    V get() throws Exception {
        var ref = new Object() {
            V item;
        };

        // 赋予强引用，防止进入回收队列
        LOCK.tryread(Exception.class, () -> ref.item = cache.get());

        // 校验数据
        if (ref.item == null)
            // 锁定数据
            return LOCK.trywrite(Exception.class, () -> {
                /* 梅开二度 */
                if ((ref.item = cache.get()) == null) {
                    // 等待清理
                    while( cleanable != null )
                        ;
                    // 重新填充数据
                    return cachedata();
                }

                return ref.item;
            });

        return ref.item;
    }

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
    @NotNull
    protected
    V cachedata() throws Exception {
        // 生成新的数据
        var v = createData();

        // 获取对象的回收方法
        if (v instanceof CanClean) {
            var c = ((CanClean) v).getclean();
            // 注册回收方法
            cleanable = CacheGcThrea.binClean(v, () -> {
                c.run();
                cleanable = null;
            });
        }

        // 绑定缓存
        cache = refClass.getConstructor(Object.class).newInstance(v);
        return v;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 移除缓存
     * <p>
     * 强制释放缓存内容<br/>
     * 将缓存内容主动加入回收队列
     */
    public
    void clear() {
        LOCK.write(() -> {
            // 正在被回收
            if (cache.isEnqueued())
                return;
            /* 释放 */
            if (cleanable != null)
                cleanable.clean();
            // GC mark
            cleanable = null;
            cache.clear();
        });
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
     * {@link Cache} 构造工具
     * <p>
     * 使用 {@link #createdata(trySupplier)} 绑定数据生成接口<br/>
     * 使用 {@link #refernce(Class)} 绑定缓存方式<br/>
     * 使用 {@link #lockBy(SyLock)} 绑定并发管理<br/>
     * 使用 {@link #build()} 进行构造
     *
     * @version 0.0.3
     * @since Cache 0.0.2
     */
    @Accessors( chain = true, fluent = true )
    public final static
    class Build<V> {
        @Setter private trySupplier<@NotNull V, Exception> createdata;
        /** @since 0.0.2 */
        @Setter private Class<? extends Reference> refernce = SoftReference.class;
        /** @since 0.0.3 */
        @Setter private SyLock lockBy = SyLock.newRWLock();

        @NotNull
        public
        Cache<V> build() {
            return new Cache<>(refernce, lockBy) {
                protected @NotNull
                V createData() throws Exception { return createdata.get(); }
            };
        }
    }
}
