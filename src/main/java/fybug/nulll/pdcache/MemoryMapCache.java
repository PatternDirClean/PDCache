package fybug.nulll.pdcache;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.lang.ref.Reference;

import fybug.nulll.pdcache.err.CacheError;
import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdconcurrent.fun.tryBiConsumer;
import lombok.Getter;

/**
 * <h2>作用于内存中的数据映射缓存.</h2>
 * <p>
 * 追加的数据获取方法 {@link #get(K)}、{@link #get(K, tryBiConsumer)}<br/>
 * 数据移除方法 {@link #remove(K)}<br/>
 * 缓存工具被关闭的时候会抛出 {@link CacheError}
 *
 * @author fybug
 * @version 0.0.1
 * @since PDCache 0.0.1
 */
public abstract
class MemoryMapCache<K, V> extends MapCacheOb<K, V> implements Closeable {
    public
    MemoryMapCache(@NotNull Class<? extends Reference> refc) { super(refc); }

    public
    MemoryMapCache(@NotNull Class<? extends Reference> refc, @NotNull SyLock syLock)
    { super(refc, syLock); }

    //----------------------------------------------------------------------------------------------

    /**
     * 获取缓存数据
     *
     * @param key 缓存的键
     *
     * @return 缓存数据
     */
    public abstract
    V get(@NotNull K key) throws Exception, CacheError;

    /**
     * 使用缓存的内容运行
     * <p>
     * 可以避免没有给予缓存对象强引用而导致缓存丢失的情况
     *
     * @param key 缓存的键
     * @param run 使用缓存的回调，传入 param key 和 {@link #get(K)}
     *
     * @return 缓存数据
     *
     * @see #get(K)
     */
    public
    V get(@NotNull K key, @NotNull tryBiConsumer<K, V, Exception> run) throws Exception, CacheError
    {
        var cache = get(key);
        run.accept(key, cache);
        return cache;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 移除缓存
     * <p>
     * 强制释放缓存内容<br/>
     * 将缓存内容主动加入回收队列
     *
     * @param key 要释放的键
     */
    public
    void remove(@NotNull K key) { removeData(key); }

    //----------------------------------------------------------------------------------------------

    // 是否被关闭
    @Getter private volatile boolean isClose = false;

    @Override
    public
    void close() {
        if (isClose())
            return;
        isClose = true;
        clear();
    }
}
