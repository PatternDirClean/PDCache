package fybug.nulll.pdconcurrent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import fybug.nulll.pdcache.fun.tryRunnable;
import fybug.nulll.pdcache.fun.trySupplier;

/**
 * 并发管理
 * <p>
 * 通过使用接口运行的方式隐藏内部的并发管理方法<br/>
 * 让开发人员无需管理并发的具体方式
 *
 * @author fybug
 * @version 0.0.1
 */
public
interface SyLock {
    /** 申请并运行于读锁 */
    default
    void read(@NotNull Runnable run) {
        read(() -> {
            run.run();
            return null;
        });
    }

    /** 申请并运行于写锁 */
    default
    void write(@NotNull Runnable run) {
        write(() -> {
            run.run();
            return null;
        });
    }

    //------------------------------------

    /**
     * 申请并运行于读锁
     *
     * @return 接口生成的数据
     */
    <T> T read(@NotNull Supplier<T> run);

    /**
     * 申请并运行于写锁
     *
     * @return 接口生成的数据
     */
    <T> T write(@NotNull Supplier<T> run);

    //----------------------------------------------------------------------------------------------

    /**
     * 尝试运行于读锁
     *
     * @param ecla 异常的类
     */
    default
    <E extends Exception> void tryread(@NotNull Class<E> ecla, @NotNull tryRunnable<E> run) throws E
    {
        tryread(ecla, () -> {
            run.run();
            return null;
        });
    }

    /**
     * 尝试运行于写锁
     *
     * @param ecla 异常的类
     */
    default
    <E extends Exception> void trywrite(@NotNull Class<E> ecla, @NotNull tryRunnable<E> run)
    throws E
    {
        trywrite(ecla, () -> {
            run.run();
            return null;
        });
    }

    //------------------------------------

    /**
     * 尝试运行于读锁
     *
     * @param ecla 异常的类
     *
     * @return 接口生成的数据
     */
    <T, E extends Exception> T tryread(@NotNull Class<E> ecla, @NotNull trySupplier<T, E> run)
    throws E;

    /**
     * 尝试运行于写锁
     *
     * @param ecla 异常的类
     *
     * @return 接口生成的数据
     */
    <T, E extends Exception> T trywrite(@NotNull Class<E> ecla, @NotNull trySupplier<T, E> run)
    throws E;

    /*--------------------------------------------------------------------------------------------*/

    static @NotNull
    ObjLock newObjLock() {return new ObjLock();}

    static @NotNull
    ReLock newReLock() {return new ReLock();}

    static @NotNull
    RWLock newRWLock() {return new RWLock();}
}
