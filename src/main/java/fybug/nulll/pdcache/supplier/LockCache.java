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
 * 基于自填充缓存 {@link SCache} 制作的用于维持一致的并发锁，并在长期不用时释放锁的工具。<br/>
 * 用法与 {@link SCache} 一致，并且内部并发管理指定为 {@link ObjLock}
 *
 * @author fybug
 * @version 0.0.1
 * @see SCache
 * @see SyLockSupp
 * @since supplier 0.0.1
 */
public
class LockCache extends SCache<SyLock> {
    /** 缓存的锁的生成方法 */
    private final Supplier<SyLock> LOCK_SUPP;

    /**
     * 构造缓存，指定缓存方式和缓存的锁类型
     *
     * @see #LockCache(Class, Supplier)
     */
    public
    LockCache(@NotNull Class<? extends Reference> refc, @NotNull Class<? extends SyLock> Locktype) {
        this(refc, new SyLockSupp(Locktype));
    }

    /** 构造缓存，指定缓存方式和缓存的锁的生成方法 */
    public
    LockCache(@NotNull Class<? extends Reference> refc, @NotNull Supplier<SyLock> lockSupplier) {
        super(refc, SyLock.newObjLock());
        LOCK_SUPP = lockSupplier;
    }

    /*--------------------------------------------------------------------------------------------*/

    @NotNull
    @Override
    protected
    SyLock createData() { return LOCK_SUPP.get(); }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取构造工具
     *
     * @return 构造工具
     */
    public static
    Build build() { return new Build(); }

    /**
     * <h2> {@link LockCache} 构造工具.</h2>
     * <ul>
     * <li>使用 {@link #refernce(Class)} 绑定缓存方式</li>
     * <li>使用 {@link #sylock(Class)} 绑定缓存的锁的类型</li>
     * <li>使用 {@link #sylock(Supplier)} 绑定缓存的锁的生成方法</li>
     * <li>使用 {@link #build()} 进行构造</li>
     * </ul>
     *
     * @version 0.0.1
     * @since LockCache 0.0.1
     */
    @Accessors( chain = true, fluent = true )
    public final static
    class Build {
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
        Build sylock(Class<? extends SyLock> sylock) { return sylock(new SyLockSupp(sylock)); }

        /** 设置缓存的锁的生成方法 */
        public
        Build sylock(@NotNull Supplier<SyLock> syLockSupplier) {
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
        LockCache build() { return new LockCache(refernce, sylock); }
    }
}
