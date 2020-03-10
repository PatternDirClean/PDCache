package fybug.nulll.pdconcurrent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import fybug.nulll.pdcache.fun.trySupplier;
import lombok.Getter;

/**
 * 使用 {@link ReentrantLock} 并发管理
 *
 * @author fybug
 * @version 0.0.1
 */
public
class ReLock implements SyLock {
    // 锁
    @Getter private final ReentrantLock LOCK;

    public
    ReLock() {this(false);}

    /** 构造并发处理，并决定使用公平锁还是非公平锁 */
    public
    ReLock(boolean fair) { LOCK = new ReentrantLock(fair); }

    //----------------------------------------------------------------------------------------------

    @Override
    public
    <T> T read(@NotNull Supplier<T> run) { return run(run); }

    @Override
    public
    <T> T write(@NotNull Supplier<T> run) { return run(run); }

    // 读写无区别
    private
    <T> T run(Supplier<T> run) {
        try {
            LOCK.lock();
            return run.get();
        } finally {
            LOCK.lock();
        }
    }

    //------------------------------------------

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

    // 读写无区别
    private
    <T, E extends Exception> T tryrun(Class<E> ecla, trySupplier<T, E> run) throws E {
        try {
            LOCK.lock();
            return run.get();
        } finally {
            LOCK.unlock();
        }
    }

    //-------------------------------------------

    /** 获取 {@link Condition} */
    @NotNull
    public
    Condition newCondition() {return LOCK.newCondition();}
}
