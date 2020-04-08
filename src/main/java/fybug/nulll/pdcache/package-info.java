/**
 * <h2>缓存工具包</h2>
 * <p>
 * 用于临时保存数据的缓存工具<br/>
 * 在缓存不被使用的时候有概率会回收<br/>
 * 可保证对应缓存数据的不重复<br/>
 * 缓存可指定缓存方式，既内部使用的 {@link java.lang.ref.Reference} 的实现<br/>
 * 缓存的数据可以是 {@link fybug.nulll.pdcache.CanClean} 的实现，
 * 该类实现会被调用 {@link fybug.nulll.pdcache.CanClean#getclean()} 获取被回收时的执行。
 * 作用于 {@link java.lang.ref.Cleaner#register(Object, Runnable)} 第二个参数
 * <br/><br/>
 * v0.0.1 expander 1 :增加 {@link fybug.nulll.pdcache.PDCache} 类，提供所有缓存工具快速索引
 *
 * @author fybug
 * @version 0.0.1 expander 1
 * @since JDK 13
 */
package fybug.nulll.pdcache;