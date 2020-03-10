/**
 * 使用数据生产接口填充数据的缓存工具
 * <p>
 * 此类缓存不可直接放入数据，需要通过数据生产方法填充缓存<br/>
 * 生成的数据可以是 {@link fybug.nulll.pdcache.CanClean} 的实现，
 * 该类实现会被调用 {@link fybug.nulll.pdcache.CanClean#getclean()} 获取被回收时的执行。
 * 作用于 {@link java.lang.ref.Cleaner#register(Object, Runnable)} 第二个参数
 *
 * @author fybug
 * @version 0.0.1
 * @since PDCache 0.0.1
 */
package fybug.nulll.pdcache.supplier;
