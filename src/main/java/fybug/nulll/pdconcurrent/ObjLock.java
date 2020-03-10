package fybug.nulll.pdconcurrent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import fybug.nulll.pdcache.fun.trySupplier;
import lombok.Getter;

/**
 * 使用传统对象锁的并发管理
 *
 * @author fybug
 * @version 0.0.1
 */
public
class ObjLock implements SyLock {
    // 对象锁
    @Getter final private Object LOCK;

    public
    ObjLock() {this(new Object());}

    /** 生成并发管理，并指定使用的并发对象锁 */
    public
    ObjLock(@NotNull Object lock) {LOCK = lock;}

    //----------------------------------------------------------------------------------------------

    @Override
    public
    <T> T read(@NotNull Supplier<T> run) { return run(run); }

    @Override
    public
    <T> T write(@NotNull Supplier<T> run) { return run(run); }

    // 读写一致
    private
    <T> T run(Supplier<T> run) {
        synchronized ( LOCK ){
            return run.get();
        }
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public
    <T, E extends Exception> T tryread(@NotNull Class<E> ecla, @NotNull trySupplier<T, E> run)
    throws E
    { return tryrun(ecla, run); }

    @Override
    public
    <T, E extends Exception> T trywrite(@NotNull Class<E> ecla, @NotNull trySupplier<T, E> run)
    throws E
    { return tryrun(ecla, run); }

    // 读写一致
    private
    <T, E extends Exception> T tryrun(Class<E> ecla, trySupplier<T, E> run) throws E {
        synchronized ( LOCK ){
            return run.get();
        }
    }
}
