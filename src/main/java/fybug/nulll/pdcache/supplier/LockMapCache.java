package fybug.nulll.pdcache.supplier;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.function.Supplier;

import fybug.nulll.pdconcurrent.ObjLock;
import fybug.nulll.pdconcurrent.SyLock;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <h2>锁缓存工具.</h2>
 * <p>
 * 基于自填充映射缓存 {@link SMapCache} 制作的用于维持多个锁的一致性，并在长期不用的时候释放锁的工具<br/>
 * 用法与 {@link SMapCache} 一致，并且内部并发管理指定为 {@link ObjLock}
 *
 * @param <K> 锁缓存的键类型
 *
 * @author fybug
 * @version 0.0.1
 * @see SMapCache
 * @see SyLockSupp
 * @since supplier 0.0.2
 */
public
class LockMapCache<K> extends SMapCache<K, SyLock> {
    /** 缓存的锁的生成方法 */
    private final Supplier<SyLock> LOCK_SUPP;

    /**
     * 构造缓存，指定缓存方式和缓存的锁类型
     *
     * @see #LockMapCache(Class, Supplier)
     */
    public
    LockMapCache(@NotNull Class<? extends Reference> refc, @NotNull Class<? extends SyLock> syLock)
    { this(refc, new SyLockSupp(syLock)); }

    /** 构造缓存，指定缓存方式和缓存的锁的生成方法 */
    public
    LockMapCache(@NotNull Class<? extends Reference> refc, @NotNull Supplier<SyLock> syLockSupplier)
    {
        super(refc, SyLock.newObjLock());
        LOCK_SUPP = syLockSupplier;
    }

    /*--------------------------------------------------------------------------------------------*/

    @NotNull
    @Override
    protected
    SyLock createData(@NotNull K key) { return LOCK_SUPP.get(); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取构造工具
     *
     * @param <K> 缓存的键类型
     *
     * @return 构造工具
     */
    public static
    <K> Build<K> build(Class<K> kcla) { return new Build<>(); }

    /**
     * <h2> {@link LockMapCache} 构造工具.</h2>
     * <ul>
     * <li>使用 {@link #refernce(Class)} 绑定缓存方式</li>
     * <li>使用 {@link #sylock(Class)} 绑定缓存的锁的类型</li>
     * <li>使用 {@link #sylock(Supplier)} 绑定缓存的锁的生成方法</li>
     * <li>使用 {@link #build()} 进行构造</li>
     * </ul>
     *
     * @param <K> 缓存键类型
     *
     * @version 0.0.1
     * @since LockMapCache 0.0.1
     */
    @Accessors( chain = true, fluent = true )
    public final static
    class Build<K> {
        /** 缓存引用类型 */
        @Setter private Class<? extends Reference> refernce = SoftReference.class;
        /** 缓存的锁的生成方法 */
        private Supplier<SyLock> sylock = ObjLock::new;

        //------------------------------------------------------------------------------------------

        /**
         * 设置缓存的锁类型
         *
         * @see #sylock(Supplier)
         */
        @NotNull
        public
        Build<K> sylock(Class<? extends SyLock> sylock)
        { return sylock(new SyLockSupp(sylock)); }

        /** 设置缓存的锁的生成方法 */
        public
        Build<K> sylock(@NotNull Supplier<SyLock> syLockSupplier) {
            this.sylock = syLockSupplier;
            return this;
        }

        /**
         * 构造锁缓存
         *
         * @return 锁缓存工具
         */
        @NotNull
        public
        LockMapCache<K> build() { return new LockMapCache<>(refernce, sylock); }
    }
}
