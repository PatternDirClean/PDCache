/**
 * <h2>缓存工具包</h2>
 * <p>
 * 用于临时保存数据的缓存工具<br/>
 * 在缓存不被使用的时候有概率会回收<br/>
 * 可保证对应缓存数据的不重复<br/>
 * 缓存可指定缓存方式，既内部使用的 {@link java.lang.ref.Reference} 的实现
 * <p>
 *
 * @author fybug
 * @version 0.0.1
 * @since JDK 13
 */
package fybug.nulll.pdcache;