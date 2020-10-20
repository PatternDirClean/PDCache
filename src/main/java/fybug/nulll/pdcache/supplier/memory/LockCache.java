package fybug.nulll.pdcache.supplier.memory;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import fybug.nulll.pdconcurrent.ObjLock;
import fybug.nulll.pdconcurrent.SyLock;
import lombok.experimental.Accessors;

/**
 * @author fybug
 * @version 0.0.1
 * @see SCache
 * @since memory 0.0.2
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
        this(refc, () -> {
            try {
                return Locktype.getConstructor().newInstance();
            } catch ( InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
                e.printStackTrace();
            }
            return null;
        });
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
     * {@link LockCache} 构造工具
     * <p>
     * 使用 {@link #refernce(Class)} 绑定缓存方式<br/>
     * 使用 {@link #sylock(Class)} 绑定缓存的锁的类型<br/>
     * 使用 {@link #sylock(Supplier)} 绑定缓存的锁的生成方法<br/>
     * 使用 {@link #build()} 进行构造
     *
     * @version 0.0.1
     * @since LockCache 0.0.1
     */
    @Accessors( chain = true, fluent = true )
    public final static
    class Build {
        /** 缓存引用类型 */
        private Class<? extends Reference> refernce = SoftReference.class;
        /** 缓存的锁的生成方法 */
        private Supplier<SyLock> sylock = ObjLock::new;

        //------------------------------------------------------------------------------------------

        /** 设置缓存引用类型 */
        @NotNull
        public
        Build refernce(Class<? extends Reference> refernce) {
            this.refernce = refernce;
            return this;
        }

        /**
         * 设置缓存的锁类型
         *
         * @see #sylock(Supplier)
         */
        @NotNull
        public
        Build sylock(Class<? extends SyLock> sylock) {
            this.sylock = () -> {
                try {
                    return sylock.getConstructor().newInstance();
                } catch ( InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
                    e.printStackTrace();
                }
                return null;
            };
            return this;
        }

        /** 设置缓存的锁的生成方法 */
        @NotNull
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
