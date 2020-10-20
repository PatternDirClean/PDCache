package fybug.nulll.pdcache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import fybug.nulll.pdconcurrent.SyLock;

/**
 * <h2>数据缓存通用类.</h2>
 * <p>
 * 用于缓存单个数据的缓存工具，数据对象可实现 {@link CanClean} 接口返回数据回收时的处理方法<br/>
 * 包含缓存的引用，缓存获取方法 {@link #getdata()}，缓存回收接口以及并发管理
 *
 * @author fybug
 * @version 0.0.2
 * @since PDCache 0.0.1
 */
public abstract
class CacheOb<V> {
    /** 缓存引用类型 */
    protected final Class<? extends Reference<V>> refClass;
    /** 缓存引用 */
    protected volatile Reference<V> cache = new WeakReference<>(null);

    /** 数据回收接口 */
    protected volatile Cleaner.Cleanable cleanable = null;

    /** 并发管理 */
    protected final SyLock LOCK;

    //----------------------------------------------------------------------------------------------

    /** 构造缓存，指定缓存方式 */
    protected
    CacheOb(@NotNull Class<? extends Reference> refc) {this(refc, SyLock.newRWLock()); }

    /** 构造缓存，指定缓存方式和并发管理 */
    protected
    CacheOb(@NotNull Class<? extends Reference> refc, @NotNull SyLock syLock) {
        this.refClass = (Class<Reference<V>>) refc;
        LOCK = syLock;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 获取缓存
     *
     * @return 当前缓存的数据
     */
    @Nullable
    protected
    V getdata() throws Exception {
        var ref = new Object() {
            V item;
        };

        // 赋予强引用，防止进入回收队列
        LOCK.read(() -> ref.item = cache.get());

        // 校验数据
        if (ref.item == null) {
            var b = true;
            /* 自旋，直到数据完整 */
            while( b ){
                b = LOCK.trywrite(Exception.class, () -> {
                    /* 是否在释放 */
                    if ((ref.item = cache.get()) == null) {
                        /* 是否释放完成 */
                        if (cleanable == null) {
                            // 获取空数据处理
                            ref.item = emptyData();
                            // 完整空数据
                            return false;
                        }
                        // 未清理完成
                        return true;
                    }
                    // 数据完整
                    return false;
                });
            }
        }

        return ref.item;
    }

    /**
     * 无数据时的数据
     *
     * @return 在没有缓存时返回的数据
     */
    @Nullable
    protected abstract
    V emptyData() throws Exception;

    //-----------------------------------

    /**
     * 放入数据
     *
     * @param v 数据
     */
    protected
    void putdata(@Nullable V v) throws Exception {
        var ref = new Object() {
            V item;
        };

        var b = true;
        while( b )
            b = LOCK.trywrite(Exception.class, () -> {
                /* 正在释放 */
                if ((ref.item = cache.get()) == null && cleanable != null)
                    // 等待释放完成
                    return true;

                // 获取对象的回收方法
                if (v instanceof CanClean) {
                    var c = ((CanClean) v).getclean();
                    // 注册回收方法
                    cleanable = CacheGcThrea.binClean(v, () -> LOCK.write(() -> {
                        c.run();
                        cleanable = null;
                    }));
                }

                // 绑定缓存
                cache = refClass.getConstructor(Object.class).newInstance(v);
                // 处理完成
                return false;
            });
    }

    /**
     * 移除缓存
     * <p>
     * 强制释放缓存内容<br/>
     * 将缓存内容主动加入回收队列
     */
    public
    void clear() {
        var ref = new Object() {
            V item;
        };
        LOCK.write(() -> {
            // 正在被回收
            if ((ref.item = cache.get()) == null)
                return;
            // 手动释放
            cache.enqueue();
        });
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * <h2> {@link CacheOb} 子类通用构造工具.</h2>
     * <ul>
     * <li>使用 {@link #refernce(Class)} 绑定缓存方式</li>
     * <li>使用 {@link #lockBy(SyLock)} 绑定并发管理</li>
     * <li>使用 {@link #build()} 进行构造</li>
     * </ul>
     *
     * @version 0.0.1
     * @since CacheOb 0.0.1
     */
    @SuppressWarnings( "unchecked" )
    protected static abstract
    class Build<V, B extends Build<V, B>> {
        /** 缓存引用类型 */
        protected Class<? extends Reference> refernce = SoftReference.class;
        /** 并发管理 */
        protected SyLock lockBy = SyLock.newRWLock();

        //------------------------------------------------------------------------------------------

        /** 设置缓存引用类型 */
        @NotNull
        public final
        B refernce(Class<? extends Reference> refernce) {
            this.refernce = refernce;
            return (B) this;
        }

        /** 设置并发管理 */
        @NotNull
        public final
        B lockBy(@NotNull SyLock lockBy) {
            this.lockBy = lockBy;
            return (B) this;
        }

        @NotNull
        public abstract
        CacheOb<V> build();
    }
}
