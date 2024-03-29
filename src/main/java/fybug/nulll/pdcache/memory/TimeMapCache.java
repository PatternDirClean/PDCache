package fybug.nulll.pdcache.memory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import fybug.nulll.pdconcurrent.ObjLock;
import fybug.nulll.pdconcurrent.SyLock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

/**
 * <h2>基于时间控制的缓存.</h2>
 * <p>
 * 内部使用 {@link LinkedHashMap} 实现的缓存工具，可指定数据过期的时间（毫秒）。<br/>
 * 数据不会因为当前被持有而不会过期，因为数据基于时间控制，内部不使用 {@link java.lang.ref.Reference} 维护。<br/>
 * 数据过期或因为任何原因被移除都会触发回收事件，在 {@link #putData(Object, Object, long, Consumer)} 最后一个参数指定，接口传入当前数据。<br/>
 * 回收事件执行时在一个线程池内执行，可使用 {@link #setGcThread(ExecutorService)} 指定，默认为 {@link Executors#newCachedThreadPool()}
 * <br/><br/>
 * <b>注意：{@link #clear()} 不会触发回收事件，因为这是缓存清空操作而不是单独回收数据。</b>
 * <br/><br/>
 * 内部会使用线程检查当前在热度排序中最后的几个数据，过期则会被主动移除。<br/>
 * 可通过 {@link #closeTimeTask()} 关闭扫描线程，效果等同于构造时传入的扫描间隔为 0，在不需要该工具时也可以使用 {@link #closeTimeTaskAndClear()} 清空数据并关闭扫描线程。<br/>
 * 在每次读取数据的时候都会检查数据是否过期，避免已过期的高热度数据残留。<br/>
 * 获取数据和放入数据的时候可以重新指定数据的存活时间，以当前时间为基准重新设定。
 * <br/><br/>
 * 包含下列参数：
 * <ul>
 *      <li>syLock，{@link SyLock} 并发管理工具实例。</li>
 *      <li>dataTime，默认数据过期时间，可构造时传入，可使用 {@link #setDataTime(long)} 修改。默认为 24 小时</li>
 *      <li>scarrentime，线程扫描间隔，仅可构造时传入，如果为 0 则关闭扫描。默认为 5 分钟</li>
 *      <li>scarrenNum，每次线程扫描的数量，仅通过 {@link #setScarrenNum(int)} 设置。默认为 20 个</li>
 * </ul>
 *
 * @author fybug
 * @version 0.0.3
 * @since memory 0.0.2
 */
public
class TimeMapCache<K, V> {
    /** 数据缓存区 */
    protected final Map<K, Enty<V>> map = new LinkedHashMap<>();
    /** 并发管理 */
    protected final SyLock LOCK;

    /** 数据过期时间（毫秒） */
    @Setter protected volatile long dataTime;

    /** 定时器 */
    @Nullable protected final Timer timerRun;
    /** 从尾部扫描的数量 */
    @Setter protected volatile int scarrenNum;

    /**
     * 回收事件执行线程池
     *
     * @since 0.0.2
     */
    @NonNull
    @Setter
    @Getter
    protected ExecutorService GcThread = Executors.newCachedThreadPool();

    //----------------------------------------------------------------------------------------------

    /** 构造缓存，使用默认参数 */
    public
    TimeMapCache() { this(SyLock.newObjLock()); }

    /** 构造缓存，指定并发管理 */
    public
    TimeMapCache(@NotNull SyLock syLock) { this(syLock, 24 * 60 * 60000); }

    /** 构造缓存，指定并发管理和默认过期时间 */
    public
    TimeMapCache(@NotNull SyLock syLock, long datatime) { this(syLock, datatime, 5 * 60000); }

    /**
     * 构造缓存，指定参数
     *
     * @param syLock      并发管理
     * @param datatime    默认数据过期时间
     * @param scarrentime 检查线程扫描间隔，为 0 则不运行线程检查
     */
    public
    TimeMapCache(@NotNull SyLock syLock, long datatime, long scarrentime) {
        LOCK = syLock;
        dataTime = datatime;
        scarrenNum = 20;

        // 是否运行线程检查
        if (scarrentime > 0) {
            timerRun = new Timer();
            timerRun.schedule(new TimeTask(), 0, scarrentime);
        } else
            timerRun = null;
    }

    /**
     * 缓存检查任务
     *
     * @author fybug
     * @version 0.0.2
     * @since TimeMapCache 0.0.1
     */
    protected final
    class TimeTask extends TimerTask {
        @Override
        public
        void run() {
            LOCK.write(() -> {
                var now = System.currentTimeMillis();
                var i = 0;
                var iter = map.entrySet().iterator();
                // 检查
                while( iter.hasNext() && i++ < scarrenNum )
                    CheckVal(iter, now);
            });
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 放入数据
     *
     * @param k 数据的键
     * @param v 数据内容
     *
     * @see #putData(Object, Object, long)
     */
    public
    void putData(@NotNull K k, V v) { this.putData(k, v, dataTime); }

    /**
     * 放入数据
     *
     * @param k        数据的键
     * @param v        数据内容
     * @param datatime 数据的过期时间，相对于当前
     */
    public
    void putData(@NotNull K k, @Nullable V v, long datatime)
    { putData(k, v, datatime, null); }

    /**
     * 放入数据
     *
     * @param k        数据的键
     * @param v        数据内容
     * @param datatime 数据的过期时间，相对于当前
     * @param endrun   数据被回收事件回调，接口传入当前数据，为空则不触发
     *
     * @since 0.0.2
     */
    public
    void putData(@NotNull K k, @Nullable V v, long datatime, @Nullable Consumer<V> endrun)
    { LOCK.write(() -> map.put(k, new Enty<>(System.currentTimeMillis() + datatime, v, endrun))); }

    //-------------------------------------

    /**
     * 移除数据
     *
     * @param k 数据的键
     */
    public
    void removeData(@NotNull K k) { LOCK.write(() -> GcVal(map.remove(k))); }

    /**
     * 清除所有数据
     * <p>
     * <b>不会触发回收事件</b>
     */
    public
    void clear() { LOCK.write(map::clear); }

    //-------------------------------------

    /**
     * 使用数据
     * <p>
     * 使用读锁进行
     *
     * @param k 数据的键
     *
     * @return 返回之前保存的数据，如果数据过期返回 null
     *
     * @see #getData(Object, long)
     */
    @Nullable
    public
    V getData(@NotNull K k) { return getData(k, 0); }

    /**
     * 使用数据
     * <p>
     * 如果刷新时间为 0，则使用 {@link SyLock#read(Supplier)} 读锁进行<br/>
     * 如果需要刷新数据存活时间，则使用 {@link SyLock#write(Supplier)} 写锁进行
     *
     * @param k           数据的键
     * @param newDatatime 数据的新存活时间，从现在开始记录
     *
     * @return 返回之前保存的数据，如果数据过期返回 null
     *
     * @since TimeMapCache v0.0.3
     */
    @Nullable
    public
    V getData(@NotNull K k, long newDatatime) {
        // 运行函数
        Supplier<V> r = () -> {
            if (check(k)) {
                var v = map.remove(k);

                // 刷新时间
                if (newDatatime > 0)
                    v.setMaxtime(System.currentTimeMillis() + newDatatime);

                map.put(k, v);
                return v.val;
            }
            GcVal(map.remove(k));
            return null;
        };

        // 刷新时间用写锁，不刷新用读锁
        if (newDatatime <= 0)
            return LOCK.read(r);
        else
            return LOCK.write(r);
    }

    /**
     * 检查数据是否可用
     *
     * @param k 数据的键
     *
     * @return 可用为 true
     */
    public
    boolean checkData(@NotNull K k) { return LOCK.read(() -> check(k)); }

    /** 检查数据是否可用 */
    protected
    boolean check(@NotNull K k)
    { return map.getOrDefault(k, new Enty<>(0, null, null)).maxtime > System.currentTimeMillis(); }

    //-------------------------------------

    /**
     * 获取数据剩余的存活时间
     *
     * @param k 数据的键
     *
     * @return 剩余时间（毫秒），过期为 0
     */
    public
    long dataHasTime(@NotNull K k) {
        return LOCK.read(() -> Math.max(map.getOrDefault(k, new Enty<>(0, null, null)).maxtime -
                                        System.currentTimeMillis(), 0));
    }

    /**
     * 统一检查并移除过期数据
     * <p>
     * 通过占用锁并扫描全部数据来彻底剔除已经过期的数据，时间点以函数开始运行的时间点为准。
     */
    public
    void trimData() {
        LOCK.write(() -> {
            long nowtime = System.currentTimeMillis();
            var iter = map.entrySet().iterator();
            // 检查
            while( iter.hasNext() )
                CheckVal(iter, nowtime);
        });
    }

    /** 关闭检查线程 */
    public
    void closeTimeTask() {
        if (timerRun != null)
            timerRun.cancel();
    }

    /**
     * 关闭检查线程并清除数据
     *
     * @see #closeTimeTask()
     * @see #clear()
     */
    public
    void closeTimeTaskAndClear() {
        closeTimeTask();
        clear();
        getGcThread().shutdown();
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 检查并移除已经过期的数据
     *
     * @since 0.0.2
     */
    private
    void CheckVal(Iterator<Map.Entry<K, Enty<V>>> iter, long nowtime) {
        // 当前节点
        var v = iter.next();
        var val = v.getValue();
        // 检查是否最大时间
        if (val.maxtime <= nowtime) {
            // 移除
            iter.remove();
            // 异步执行回收事件
            GcVal(val);
        }
    }

    /**
     * 申请执行当前数据的回收事件
     *
     * @since 0.0.2
     */
    private
    void GcVal(Enty<V> v) {
        if (v == null)
            return;
        // 当前回收事件
        val endrun = v.getEndrun();
        // 线程池执行
        getGcThread().submit(() -> {
            if (endrun != null)
                endrun.accept(v.getVal());
        });
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * 获取缓存构造工具
     *
     * @param <K> 键的类型
     * @param <V> 缓存内容的类型
     *
     * @return 构造工具
     */
    @NotNull
    public static
    <K, V> Build<K, V> build(Class<K> kClass, Class<V> vClass) { return new Build<>(); }

    /**
     * <h2> {@link TimeMapCache} 构造工具.</h2>
     * <ul>
     * <li>使用 {@link #lockBy(SyLock)} 绑定并发管理</li>
     * <li>使用 {@link #dataTime(long)} 指定数据存活时间</li>
     * <li>使用 {@link #scarrentime(long)} 指定扫描间隔时间</li>
     * <li>使用 {@link #scarrenNum(int)} 指定扫描数量</li>
     * <li>使用 {@link #build()} 进行构造</li>
     * </ul>
     *
     * @version 0.0.1
     * @since TimeMapCache 0.0.1
     */
    @Accessors( chain = true, fluent = true )
    public static final
    class Build<K, V> {
        /** 并发工具 */
        @Setter protected SyLock lockBy = new ObjLock();
        /** 默认数据存活时间 */
        @Setter protected long dataTime = 5 * 60000;
        /** 扫描时检查的数量 */
        @Setter protected int scarrenNum = 20;
        /** 扫描间隔时间 */
        @Setter protected long scarrentime = 24 * 60 * 600000;

        /** 构造 */
        @NotNull
        public
        TimeMapCache<K, V> build() {
            var c = new TimeMapCache<K, V>(lockBy, dataTime, scarrentime);
            c.setScarrenNum(scarrenNum);
            return c;
        }
    }

    //----------------------------------------------------------------------------------------------

    /**
     * <h2>数据记录对象.</h2>
     *
     * @author fybug
     * @version 0.0.2
     * @since TimeMapCache 0.0.1
     */
    @AllArgsConstructor
    @Setter
    @Getter
    public static
    class Enty<V> {
        /** 过期的时间 */
        public long maxtime;
        /** 数据 */
        @Nullable public V val;
        /**
         * 回收时的处理
         * <p>
         * 接口传入当前数据
         *
         * @since 0.0.2
         */
        @Nullable public Consumer<V> endrun;
    }
}
