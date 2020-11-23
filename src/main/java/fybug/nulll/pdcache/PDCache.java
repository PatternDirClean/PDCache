package fybug.nulll.pdcache;
import org.jetbrains.annotations.NotNull;

import fybug.nulll.pdcache.memory.Cache;
import fybug.nulll.pdcache.memory.MapCache;
import fybug.nulll.pdcache.supplier.SCache;
import fybug.nulll.pdcache.supplier.SMapCache;
import lombok.experimental.UtilityClass;

/**
 * <h2>主类.</h2>
 * 可以在这里找到所有的缓存工具。<br/>
 * 提供所有缓存工具的快速构造索引。
 *
 * @author fybug
 * @version 0.0.1
 * @since PDCache 0.0.1 expander 1
 */
@UtilityClass
public
class PDCache {
    /**
     * 缓存在内存中的单个数据缓存
     *
     * @param vClass 数据类型
     *
     * @return CacheBuild
     *
     * @see Cache
     */
    @NotNull
    public
    <V> Cache.Build<V> Cache(Class<V> vClass) { return Cache.build(vClass); }

    /**
     * 缓存在内存中的自填充单个数据缓存
     *
     * @param vClass 数据类型
     *
     * @return SCacheBuild
     *
     * @see SCache
     */
    @NotNull
    public
    <V> SCache.Build<V> SCache(Class<V> vClass) { return SCache.build(vClass); }

    /**
     * 缓存在内存中的映射数据缓存
     *
     * @param kClass 键的类型
     * @param vClass 数据类型
     *
     * @return MapCacheBuild
     *
     * @see MapCache
     */
    @NotNull
    public
    <K, V> MapCache.Build<K, V> MapCache(Class<K> kClass, Class<V> vClass)
    { return MapCache.build(kClass, vClass); }

    /**
     * 缓存在内存中的自填充映射数据缓存
     *
     * @param kClass 键的类型
     * @param vClass 数据类型
     *
     * @return SMapCacheBuild
     *
     * @see SMapCache
     */
    @NotNull
    public
    <K, V> SMapCache.Build<K, V> SMapCache(Class<K> kClass, Class<V> vClass)
    { return SMapCache.build(kClass, vClass); }
}
