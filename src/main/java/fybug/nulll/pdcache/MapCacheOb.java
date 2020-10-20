package fybug.nulll.pdcache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import fybug.nulll.pdconcurrent.SyLock;

/**
 * <h2>映射缓存通用类.</h2>
 * <p>
 * 使用键来获取缓存的缓存工具，数据对象可实现 {@link CanClean} 接口返回数据回收时的处理<br/>
 * 缓存关联的是映射的值，与键无关，在值对象达到回收条件时对应的键会从缓存中移除<br/>
 * 包含缓存的引用映射，缓存获取方法 {@link #getdata(K key)}，缓存回收接口映射以及并发管理
 *
 * @author fybug
 * @version 0.0.2
 * @since PDCache 0.0.1
 */
public abstract
class MapCacheOb<K, V> {
    /** 缓存引用类型 */
    protected final Class<? extends Reference<V>> refClass;
    /** 数据缓存区 */
    protected final Map<K, Reference<V>> map = new HashMap<>();

    /** 回收接口存放区 */
    protected final Map<K, Cleaner.Cleanable> cleanableMap = new HashMap<>();

    /** 并发管理 */
    protected final SyLock LOCK;

    //----------------------------------------------------------------------------------------------

    /** 构造缓存，指定缓存方式 */
    public
    MapCacheOb(@NotNull Class<? extends Reference> refc) { this(refc, SyLock.newRWLock()); }

    /** 构造缓存，指定缓存方式和并发管理 */
    public
    MapCacheOb(@NotNull Class<? extends Reference> refc, @NotNull SyLock syLock) {
        refClass = (Class<Reference<V>>) refc;
        LOCK = syLock;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 获取缓存数据
     *
     * @param key 缓存的键
     *
     * @return 缓存记录对象
     *
     * @see Enty
     */
    @NotNull
    protected
    MapCacheOb<K, V>.Enty getdata(@NotNull K key) throws Exception {
        // 缓存记录对象
        final var enty = new Enty();

        // get ref
        LOCK.read(() -> {
            enty.ref = map.get(key);
            // 获取内容
            if (enty.ref == null)
                enty.val = null;
            else
                enty.val = enty.ref.get();
        });

        /* 检查获取的数据 */
        if (enty.val == null) {
            var b = true;
            /* 自旋，直到数据完整 */
            while( b ){
                b = LOCK.trywrite(Exception.class, () -> {
                    /* 当前是否正在释放 */
                    if ((enty.ref = map.get(key)) == null || (enty.val = enty.ref.get()) == null) {
                        /* 扫尾接口是否运行完成 */
                        if (cleanableMap.get(key) == null) {
                            // 空数据处理
                            enty.val = emptyData(key);
                            enty.ref = map.get(key);

                            // 被彻底释放，完整的空数据
                            return false;
                        }
                        // 数据不完整，继续等待
                        return true;
                    } else
                        // 数据完整
                        return false;
                });
            }
        }
        return enty;
    }

    /**
     * 无数据时的数据
     *
     * @return 在没有缓存时返回的数据
     */
    @Nullable
    protected abstract
    V emptyData(@NotNull K key) throws Exception;

    //----------------------------------------------------------------------------------------------

    /**
     * 更新缓存内容
     *
     * @param key 缓存键
     * @param v   缓存内容
     */
    protected
    void putdata(@NotNull K key, @NotNull V v) throws Exception {
        final var enty = new Enty();

        /* 检查获取的数据 */
        var b = true;
        /* 自旋，直到数据完整 */
        while( b ){
            b = LOCK.trywrite(Exception.class, () -> {
                /* 正在释放 */
                if (((enty.ref = map.get(key)) != null && (enty.val = enty.ref.get()) == null) &&
                    cleanableMap.get(key) != null)
                    // 等待释放完成
                    return true;

                /* 获取对象的回收方法 */
                Runnable clean;
                if (v instanceof CanClean)
                    clean = ((CanClean) v).getclean();
                else
                    clean = null;

                /* 注册回收方法 */
                cleanableMap.put(key, CacheGcThrea.binClean(v, () -> LOCK.write(() -> {
                    if (clean != null)
                        clean.run();
                    cleanableMap.remove(key);
                    map.remove(key);
                })));

                // 放入缓存
                map.put(key, refClass.getConstructor(Object.class).newInstance(v));
                // 处理完成
                return false;
            });
        }
    }

    /**
     * 移除缓存
     * <p>
     * 强制释放缓存内容<br/>
     * 将缓存内容主动加入回收队列
     *
     * @param key 要释放的键
     *
     * @return 释放的内容
     */
    @Nullable
    protected
    V removeData(@NotNull K key) {
        final var cac = new Object() {
            Reference<V> ref;
            V v;
        };

        return LOCK.write(() -> {
            // 已在释放
            if ((cac.ref = map.get(key)) == null || (cac.v = cac.ref.get()) == null)
                return null;

            // 手动释放
            cac.ref.enqueue();
            return cac.v;
        });
    }

    /** 清空缓存数据 */
    public
    void clear() {
        // 释放
        LOCK.write(() -> {
            map.values().forEach(v -> {
                // 对象被释放
                if (v == null || v.get() == null)
                    return;
                v.enqueue();
            });
            map.clear();
        });
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * <h2> {@link MapCacheOb} 之类通用构造工具.</h2>
     * <ul>
     * <li>使用 {@link #refernce(Class)} 绑定缓存方式</li>
     * <li>使用 {@link #lockBy(SyLock)} 绑定并发管理</li>
     * <li>使用 {@link #build()} 进行构造</li>
     * </ul>
     *
     * @version 0.0.1
     * @since MapCacheOb 0.0.1
     */
    @SuppressWarnings( "unchecked" )
    public static abstract
    class Build<K, V, B extends Build<K, V, B>> {
        /** 缓存引用类型 */
        protected Class<? extends Reference> refernce = SoftReference.class;
        /** 并发管理 */
        protected SyLock lockBy = SyLock.newRWLock();

        /** 设置缓存引用类型 */
        @NotNull
        public
        B refernce(Class<? extends Reference> refernce) {
            this.refernce = refernce;
            return (B) this;
        }

        /** 设置并发管理 */
        @NotNull
        public
        B lockBy(@NotNull SyLock lockBy) {
            this.lockBy = lockBy;
            return (B) this;
        }

        @NotNull
        public abstract
        MapCacheOb<K, V> build();
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * <h2>缓存记录类.</h2>
     * 记录缓存的引用对象 {@link #ref}<br/>
     * 记录缓存的内容 {@link #val}
     *
     * @author fybug
     * @version 0.0.1
     * @since MapCacheOb 0.0.1
     */
    protected final
    class Enty {
        public volatile Reference<V> ref;
        public volatile V val;
    }
}
