package fybug.nulll.pdcache;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.lang.ref.Reference;

import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdconcurrent.fun.tryConsumer;

/**
 * <h2>作用于内存中的数据缓存.</h2>
 * <p>
 * 追加的数据获取方法 {@link #get()}、{@link #get(tryConsumer)}
 *
 * @author fybug
 * @version 0.0.1
 * @since PDCache 0.0.1
 */
public abstract
class MemoryCache<V> extends CacheOb<V> implements Closeable {

    /** 构造缓存，指定缓存方式 */
    protected
    MemoryCache(@NotNull Class<? extends Reference> refc) { super(refc); }

    /** 构造缓存，指定缓存方式和并发管理 */
    protected
    MemoryCache(@NotNull Class<? extends Reference> refc, @NotNull SyLock syLock)
    { super(refc, syLock); }

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
    public
    V get() throws Exception { return getdata(); }

    //----------------------------------------------------------------------------------------------

    @Override
    public
    void close() { clear(); }
}
